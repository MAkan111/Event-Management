package ru.makan1.eventmanagement.event.mapper;

import ru.makan1.eventmanagement.event.dto.EventRequest;
import ru.makan1.eventmanagement.event.dto.EventResponse;
import ru.makan1.eventmanagement.event.entity.EventEntity;

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
}
