package ru.makan1.eventmanager.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.makan1.eventmanager.location.dto.LocationRequest;
import ru.makan1.eventmanager.location.dto.LocationResponse;
import ru.makan1.eventmanager.location.service.LocationsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/locations")
public class LocationsController {

    private final LocationsService locationsService;

    public LocationsController(LocationsService locationsService) {
        this.locationsService = locationsService;
    }

    @GetMapping()
    public ResponseEntity<List<LocationResponse>> getLocations() {
        return ResponseEntity.ok(locationsService.getLocationsList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@Valid @NotNull @PathVariable("id") Long id) {
        return ResponseEntity.ok(locationsService.getLocationById(id));
    }

    @PostMapping()
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest locationRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationsService.createLocation(locationRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable("id") Long id,
                                                           @Valid @RequestBody LocationRequest locationRequest
    ) {
        return ResponseEntity.ok(locationsService.updateLocation(id, locationRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LocationResponse> deleteLocationById(@Valid @NotNull @PathVariable("id") Long id) {
        locationsService.deleteLocationById(id);
        return ResponseEntity.noContent().build();
    }
}
