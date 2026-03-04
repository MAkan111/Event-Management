package ru.makan1.eventmanagement.location.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventmanagement.location.dto.LocationRequest;
import ru.makan1.eventmanagement.location.dto.LocationResponse;
import ru.makan1.eventmanagement.location.entity.LocationEntity;
import ru.makan1.eventmanagement.location.mapper.LocationMapper;
import ru.makan1.eventmanagement.location.repository.LocationsRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LocationsService {

    private final LocationsRepository locationsRepository;

    public LocationsService(LocationsRepository locationsRepository) {
        this.locationsRepository = locationsRepository;
    }

    public List<LocationResponse> getLocationsList() {
        List<LocationEntity> locationEntities = locationsRepository.findAll();

        if(locationEntities.isEmpty()) {
            return new ArrayList<>();
        }

        return locationEntities.stream()
                .map(LocationMapper::mapToLocationResponse)
                .toList();
    }

    public LocationResponse getLocationById(Long id) {
        LocationEntity locationEntity = locationsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Локации с таким id не существует"));

        return LocationMapper.mapToLocationResponse(locationEntity);
    }

    @Transactional
    public @Nullable LocationResponse createLocation(@Valid LocationRequest locationRequest) {
        LocationEntity locationEntity = LocationMapper.mapToLocationEntity(locationRequest);

        return LocationMapper.mapToLocationResponse(locationsRepository.save(locationEntity));
    }

    @Transactional
    public @Nullable LocationResponse updateLocation(Long id, LocationRequest locationRequest) {

        LocationEntity locationToUpdate = locationsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Локации с таким id не существует"));

        locationToUpdate.setName(locationRequest.name());
        locationToUpdate.setAddress(locationRequest.address());
        locationToUpdate.setCapacity(locationRequest.capacity());
        locationToUpdate.setDescription(locationRequest.description());

        return LocationMapper.mapToLocationResponse(locationsRepository.save(locationToUpdate));
    }

    @Transactional
    public void deleteLocationById(Long id) {
        LocationEntity locationEntity = locationsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Локации с таким id не существует"));

        locationsRepository.delete(locationEntity);
    }
}
