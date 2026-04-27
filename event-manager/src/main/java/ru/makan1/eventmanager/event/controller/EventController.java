package ru.makan1.eventmanager.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.makan1.eventmanager.event.dto.EventRequest;
import ru.makan1.eventmanager.event.dto.EventResponse;
import ru.makan1.eventmanager.event.dto.EventSearchRequest;
import ru.makan1.eventmanager.event.dto.EventUpdateRequest;
import ru.makan1.eventmanager.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        EventResponse eventResponse = eventService.addEvent(eventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEventsByFilter(@RequestBody EventSearchRequest eventSearchRequest) {
        List<EventResponse> eventResponse = eventService.searchEvents(eventSearchRequest);
        return ResponseEntity.ok(eventResponse);
    }

    @PostMapping("/registrations/{eventId}")
    public ResponseEntity<EventResponse> registerUser(@PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(eventService.registerUser(eventId));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable("eventId") Long eventId) {
        EventResponse eventResponse = eventService.getEvent(eventId);
        return ResponseEntity.ok(eventResponse);
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventResponse>> getEventsByCurrentUser() {
        List<EventResponse> eventResponse = eventService.getEventsByCurrentUser();
        return ResponseEntity.ok(eventResponse);
    }

    @GetMapping("/registrations/my")
    public ResponseEntity<List<EventResponse>> getRegistrationsByCurrentUser() {
        List<EventResponse> eventResponse = eventService.getRegistrationsByCurrentUser();
        return ResponseEntity.ok(eventResponse);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventId") Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/registrations/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable("eventId") Long eventId) {
        eventService.cancelRegistration(eventId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable("eventId") Long eventId,
                                                     @Valid @RequestBody EventUpdateRequest updateRequest) {
        EventResponse eventResponse = eventService.updateEvent(eventId, updateRequest);
        return ResponseEntity.ok(eventResponse);
    }
}
