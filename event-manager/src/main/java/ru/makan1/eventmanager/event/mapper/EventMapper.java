package ru.makan1.eventmanager.event.mapper;

import ru.makan1.eventcommon.enums.EventType;
import ru.makan1.eventcommon.messages.EventKafkaMessage;
import ru.makan1.eventcommon.model.ChangeItem;
import ru.makan1.eventmanager.event.dto.EventRequest;
import ru.makan1.eventmanager.event.dto.EventResponse;
import ru.makan1.eventmanager.event.entity.EventEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EventMapper {
    public static EventEntity mapToEventEntity(EventRequest eventRequest) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setDate(eventRequest.date());
        eventEntity.setDuration(eventRequest.duration());
        eventEntity.setCost(eventRequest.cost());
        eventEntity.setMaxPlaces(eventRequest.maxPlaces());
        eventEntity.setName(eventRequest.name());
        return eventEntity;
    }

    public static EventResponse mapToEventResponse(EventEntity eventEntity) {
        return new EventResponse(
                eventEntity.getId(),
                eventEntity.getLocation().getId(),
                eventEntity.getOwnerId(),
                eventEntity.getName(),
                eventEntity.getDate(),
                eventEntity.getOccupiedPlaces(),
                eventEntity.getDuration(),
                eventEntity.getCost(),
                eventEntity.getMaxPlaces(),
                eventEntity.getStatus().name()
        );
    }

    public static EventKafkaMessage toKafkaMessageCreated(
            EventResponse created,
            Long changedById,
            List<Long> subscriberIds
    ) {
        return toKafkaMessage(EventType.EVENT_CREATED, created, changedById, subscriberIds, List.of());
    }

    public static EventKafkaMessage toKafkaMessageUpdated(
            EventResponse updated,
            Long changedById,
            List<Long> subscriberIds,
            List<ChangeItem> changes
    ) {
        return toKafkaMessage(EventType.EVENT_UPDATED, updated, changedById, subscriberIds, changes);
    }

    public static EventKafkaMessage toKafkaMessageCanceled(
            EventResponse canceled,
            Long changedById,
            List<Long> subscriberIds,
            List<ChangeItem> changes
    ) {
        return toKafkaMessage(EventType.EVENT_CANCELED, canceled, changedById, subscriberIds, changes);
    }

    private static EventKafkaMessage toKafkaMessage(
            EventType eventType,
            EventResponse event,
            Long changedById,
            List<Long> subscriberIds,
            List<ChangeItem> changes
    ) {
        List<Long> subscribers = subscriberIds != null ? subscriberIds : List.of();
        List<ChangeItem> safeChanges = changes != null ? changes : List.of();
        return new EventKafkaMessage(
                UUID.randomUUID(),
                eventType,
                event.id(),
                Instant.now(),
                event.ownerId(),
                changedById,
                subscribers,
                safeChanges
        );
    }
}
