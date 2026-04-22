package ru.makan1.eventmanager.location.mapper;

import ru.makan1.eventmanager.location.dto.LocationRequest;
import ru.makan1.eventmanager.location.dto.LocationResponse;
import ru.makan1.eventmanager.location.entity.LocationEntity;

public class LocationMapper {
    public static LocationEntity mapToLocationEntity(LocationRequest locationRequest) {
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(locationRequest.name());
        locationEntity.setAddress(locationRequest.address());
        locationEntity.setCapacity(locationRequest.capacity());
        locationEntity.setDescription(locationRequest.description());
        return locationEntity;
    }

    public static LocationResponse mapToLocationResponse(LocationEntity locationEntity) {
        return new LocationResponse(
                locationEntity.getId(),
                locationEntity.getName(),
                locationEntity.getAddress(),
                locationEntity.getCapacity(),
                locationEntity.getDescription()
        );
    }
}
