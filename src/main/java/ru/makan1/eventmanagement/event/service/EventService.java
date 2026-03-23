package ru.makan1.eventmanagement.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventmanagement.event.dto.EventRequest;
import ru.makan1.eventmanagement.event.dto.EventResponse;
import ru.makan1.eventmanagement.event.dto.EventSearchRequest;
import ru.makan1.eventmanagement.event.dto.EventUpdateRequest;
import ru.makan1.eventmanagement.event.entity.EventEntity;
import ru.makan1.eventmanagement.event.enums.EventStatus;
import ru.makan1.eventmanagement.event.mapper.EventMapper;
import ru.makan1.eventmanagement.event.repository.EventRepository;
import ru.makan1.eventmanagement.location.entity.LocationEntity;
import ru.makan1.eventmanagement.location.repository.LocationsRepository;
import ru.makan1.eventmanagement.users.entity.UsersEntity;
import ru.makan1.eventmanagement.users.enums.UserRole;
import ru.makan1.eventmanagement.users.repository.UsersRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final LocationsRepository locationsRepository;
    private final UsersRepository usersRepository;

    @Transactional
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

        return EventMapper.mapToEventResponse(eventRepository.save(eventEntity));
    }

    public List<EventResponse> searchEvents(EventSearchRequest eventSearchRequest) {
        return eventRepository.findAll()
                .stream()
                .filter(event -> {
                    if (eventSearchRequest.name() != null && !eventSearchRequest.name().isBlank()) {
                        if (event.getName() == null || !event.getName().equals(eventSearchRequest.name())) {
                            return false;
                        }
                    }

                    if (eventSearchRequest.placesMin() != null && eventSearchRequest.placesMin() > 0
                            && event.getMaxPlaces() < eventSearchRequest.placesMin()) {
                        return false;
                    }
                    if (eventSearchRequest.placesMax() != null && eventSearchRequest.placesMax() > 0
                            && event.getMaxPlaces() > eventSearchRequest.placesMax()) {
                        return false;
                    }

                    if (eventSearchRequest.dateStartAfter() != null &&
                            (event.getDate() == null || event.getDate().isBefore(eventSearchRequest.dateStartAfter()))) {
                        return false;
                    }
                    if (eventSearchRequest.dateStartBefore() != null &&
                            (event.getDate() == null || event.getDate().isAfter(eventSearchRequest.dateStartBefore()))) {
                        return false;
                    }

                    if (eventSearchRequest.costMin() != null &&
                            (event.getCost() == null || event.getCost().compareTo(eventSearchRequest.costMin()) < 0)) {
                        return false;
                    }
                    if (eventSearchRequest.costMax() != null &&
                            (event.getCost() == null || event.getCost().compareTo(eventSearchRequest.costMax()) > 0)) {
                        return false;
                    }

                    if (eventSearchRequest.durationMin() != null && eventSearchRequest.durationMin() > 0
                            && event.getDuration() < eventSearchRequest.durationMin()) {
                        return false;
                    }
                    if (eventSearchRequest.durationMax() != null && eventSearchRequest.durationMax() > 0
                            && event.getDuration() > eventSearchRequest.durationMax()) {
                        return false;
                    }

                    if (eventSearchRequest.locationId() != null) {
                        if (event.getLocation() == null ||
                                event.getLocation().getId() == null ||
                                !event.getLocation().getId().equals(eventSearchRequest.locationId())) {
                            return false;
                        }
                    }

                    if (eventSearchRequest.eventStatus() != null && !eventSearchRequest.eventStatus().isBlank()) {
                        if (event.getStatus() == null ||
                                !event.getStatus().name().equals(eventSearchRequest.eventStatus())) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(EventMapper::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional
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

        usersRepository.save(user);
        return EventMapper.mapToEventResponse(eventRepository.save(event));
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

    @Transactional
    public void deleteEvent(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));
        UsersEntity currentUser = getCurrentUser();

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = currentUser.getUserId().equals(event.getOwnerId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Недостаточно прав для отмены этого мероприятия");
        }

        if (event.getStatus() != EventStatus.WAIT_START) {
            throw new IllegalStateException("Можно отменить только мероприятие со статусом WAIT_START");
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Transactional
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

        usersRepository.save(user);
        eventRepository.save(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventUpdateRequest updateRequest) {
        EventEntity eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Такого мероприятия не существует"));

        UsersEntity currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = currentUser.getUserId().equals(eventToUpdate.getOwnerId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Недостаточно прав для редактирования этого мероприятия");
        }

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

        if (updateRequest.name() != null) {
            eventToUpdate.setName(updateRequest.name());
        }
        if (updateRequest.date() != null) {
            eventToUpdate.setDate(updateRequest.date());
        }
        if (updateRequest.duration() != null) {
            eventToUpdate.setDuration(updateRequest.duration());
        }
        if (updateRequest.cost() != null) {
            eventToUpdate.setCost(updateRequest.cost());
        }

        return EventMapper.mapToEventResponse(eventRepository.save(eventToUpdate));
    }

    private UsersEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}
