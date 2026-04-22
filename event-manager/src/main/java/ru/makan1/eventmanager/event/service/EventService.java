package ru.makan1.eventmanager.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventcommon.model.ChangeItem;
import ru.makan1.eventmanager.event.dto.EventRequest;
import ru.makan1.eventmanager.event.dto.EventResponse;
import ru.makan1.eventmanager.event.dto.EventSearchRequest;
import ru.makan1.eventmanager.event.dto.EventUpdateRequest;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.event.enums.EventStatus;
import ru.makan1.eventmanager.event.mapper.EventMapper;
import ru.makan1.eventmanager.event.repository.EventRepository;
import ru.makan1.eventmanager.location.entity.LocationEntity;
import ru.makan1.eventmanager.location.repository.LocationsRepository;
import ru.makan1.eventmanager.users.entity.UsersEntity;
import ru.makan1.eventmanager.users.enums.UserRole;
import ru.makan1.eventmanager.users.repository.UsersRepository;
import ru.makan1.eventmanager.utils.ChangesBuilder;
import ru.makan1.eventmanager.utils.EventUtils;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final LocationsRepository locationsRepository;
    private final UsersRepository usersRepository;
    private final EventSender eventSender;
    private final TxService txService;

    public EventResponse addEvent(EventRequest eventRequest) {
        LocationEntity location = locationsRepository.findById(eventRequest.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Локации для мероприятия не существует"));

        if (location.getCapacity() < eventRequest.maxPlaces()) {
            throw new IllegalArgumentException("Вместимость локации меньше запрошенного количества мест");
        }

        UsersEntity currentUser = getCurrentUser();

        EventEntity eventEntity = EventMapper.mapToEventEntity(eventRequest);
        eventEntity.setOwnerId(currentUser.getUserId());
        eventEntity.setOccupiedPlaces(0);
        eventEntity.setLocation(location);
        eventEntity.setStatus(EventStatus.WAIT_START);

        EventResponse createdEvent = EventMapper.mapToEventResponse(txService.saveToEventDb(eventEntity));

        var kafkaMessage = EventMapper.toKafkaMessageCreated(
                createdEvent,
                currentUser.getUserId(),
                List.of(currentUser.getUserId())
        );
        eventSender.sendEvent(kafkaMessage);

        return createdEvent;
    }

    public List<EventResponse> searchEvents(EventSearchRequest eventSearchRequest) {
        String name = EventUtils.normalizeBlankToNull(eventSearchRequest.name());
        EventStatus status;
        try {
            status = EventUtils.parseEventStatusOrNull(eventSearchRequest.eventStatus());
        } catch (IllegalArgumentException ex) {
            return List.of();
        }

        return eventRepository.search(
                        name,
                        eventSearchRequest.placesMin(),
                        eventSearchRequest.placesMax(),
                        eventSearchRequest.dateStartAfter(),
                        eventSearchRequest.dateStartBefore(),
                        eventSearchRequest.costMin(),
                        eventSearchRequest.costMax(),
                        eventSearchRequest.durationMin(),
                        eventSearchRequest.durationMax(),
                        eventSearchRequest.locationId(),
                        status
                )
                .stream()
                .map(EventMapper::mapToEventResponse)
                .toList();
    }

    public EventResponse registerUser(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));
        UsersEntity user = getCurrentUser();

        if (event.getStatus() != EventStatus.WAIT_START) {
            throw new IllegalStateException("Регистрация доступна только на мероприятия со статусом WAIT_START");
        }

        if (event.getOccupiedPlaces() >= event.getMaxPlaces()) {
            throw new IllegalStateException("Все места на мероприятии уже заняты");
        }

        boolean alreadyRegistered = user.getEvents().stream()
                .anyMatch(userEvent -> userEvent.getId().equals(eventId));
        if (alreadyRegistered) {
            throw new IllegalStateException("Вы уже зарегистрированы на это мероприятие");
        }

        user.getEvents().add(event);
        event.getUsers().add(user);
        event.setOccupiedPlaces(event.getOccupiedPlaces() + 1);

        txService.saveToUsersDb(user);
        return EventMapper.mapToEventResponse(txService.saveToEventDb(event));
    }

    public EventResponse getEvent(Long eventId) {
        return EventMapper.mapToEventResponse(eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует")));
    }

    public List<EventResponse> getEventsByCurrentUser() {
        UsersEntity user = getCurrentUser();
        return eventRepository.findByOwnerId(user.getUserId())
                .stream()
                .map(EventMapper::mapToEventResponse)
                .toList();
    }

    public List<EventResponse> getRegistrationsByCurrentUser() {
        UsersEntity user = getCurrentUser();
        return user.getEvents()
                .stream()
                .map(EventMapper::mapToEventResponse)
                .toList();
    }

    public void deleteEvent(Long eventId) {
        EventEntity deletedEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));
        UsersEntity currentUser = getCurrentUser();

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = currentUser.getUserId().equals(deletedEvent.getOwnerId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Недостаточно прав для отмены этого мероприятия");
        }

        if (deletedEvent.getStatus() != EventStatus.WAIT_START) {
            throw new IllegalStateException("Можно отменить только мероприятие со статусом WAIT_START");
        }

        EventResponse deletedEventResponse = EventMapper.mapToEventResponse(deletedEvent);
        List<Long> subscribers = collectSubscriberIds(deletedEvent);
        var changeItem = ChangesBuilder.change("status", deletedEvent.getStatus().name(), EventStatus.CANCELLED.name());

        var kafkaMessage = EventMapper.toKafkaMessageCanceled(
                deletedEventResponse,
                currentUser.getUserId(),
                subscribers,
                List.of(changeItem)
        );
        eventSender.sendEvent(kafkaMessage);

        deletedEvent.setStatus(EventStatus.CANCELLED);
        txService.saveToEventDb(deletedEvent);
    }

    public void cancelRegistration(Long eventId) {
        UsersEntity user = getCurrentUser();
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));

        if (event.getStatus() == EventStatus.STARTED || event.getStatus() == EventStatus.FINISHED) {
            throw new IllegalStateException("Нельзя отменить регистрацию на начавшееся или завершённое мероприятие");
        }

        boolean wasRegistered = user.getEvents().removeIf(e -> e.getId().equals(eventId));
        if (!wasRegistered) {
            throw new EntityNotFoundException("Вы не зарегистрированы на это мероприятие");
        }

        event.getUsers().removeIf(u -> u.getUserId().equals(user.getUserId()));
        event.setOccupiedPlaces(Math.max(0, event.getOccupiedPlaces() - 1));

        txService.saveToUsersDb(user);
        txService.saveToEventDb(event);
    }

    public EventResponse updateEvent(Long eventId, EventUpdateRequest updateRequest) {
        EventEntity eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));

        UsersEntity currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = currentUser.getUserId().equals(eventToUpdate.getOwnerId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Недостаточно прав для редактирования этого мероприятия");
        }

        EventResponse oldValue = EventMapper.mapToEventResponse(eventToUpdate);

        if (updateRequest.maxPlaces() != null) {
            if (updateRequest.maxPlaces() < eventToUpdate.getOccupiedPlaces()) {
                throw new IllegalArgumentException(
                        "Новое количество мест не может быть меньше числа уже записанных участников: "
                                + eventToUpdate.getOccupiedPlaces());
            }
            if (updateRequest.locationId() == null) {
                int currentCapacity = eventToUpdate.getLocation().getCapacity();
                if (updateRequest.maxPlaces() > currentCapacity) {
                    throw new IllegalArgumentException(
                            "Новое количество мест превышает вместимость текущей локации: " + currentCapacity);
                }
            }
            eventToUpdate.setMaxPlaces(updateRequest.maxPlaces());
        }

        if (updateRequest.locationId() != null) {
            LocationEntity location = locationsRepository.findById(updateRequest.locationId())
                    .orElseThrow(() -> new EntityNotFoundException("Такой локации не существует"));
            int effectiveMaxPlaces = updateRequest.maxPlaces() != null
                    ? updateRequest.maxPlaces()
                    : eventToUpdate.getMaxPlaces();
            if (location.getCapacity() < effectiveMaxPlaces) {
                throw new IllegalArgumentException("Вместимость новой локации меньше максимального числа мест мероприятия");
            }
            eventToUpdate.setLocation(location);
        }

        eventToUpdate.setName(updateRequest.name());
        eventToUpdate.setDate(updateRequest.date());
        eventToUpdate.setDuration(updateRequest.duration());
        eventToUpdate.setCost(updateRequest.cost());

        var updatedEvent = EventMapper.mapToEventResponse(txService.saveToEventDb(eventToUpdate));
        List<ChangeItem> changeItemList = ChangesBuilder.collectChanges(oldValue, updatedEvent);
        List<Long> subscribers = collectSubscriberIds(eventToUpdate);

        var kafkaMessage = EventMapper.toKafkaMessageUpdated(
                updatedEvent,
                currentUser.getUserId(),
                subscribers,
                changeItemList
        );
        eventSender.sendEvent(kafkaMessage);

        return updatedEvent;
    }

    private static List<Long> collectSubscriberIds(EventEntity event) {
        return Stream.concat(
                        event.getUsers().stream().map(UsersEntity::getUserId),
                        Stream.of(event.getOwnerId())
                )
                .distinct()
                .toList();
    }

    private UsersEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}
