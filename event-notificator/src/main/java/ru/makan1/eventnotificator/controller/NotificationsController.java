package ru.makan1.eventnotificator.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.makan1.eventnotificator.dto.MarkNotificationsReadRequest;
import ru.makan1.eventnotificator.dto.NotificationResponse;
import ru.makan1.eventnotificator.service.NotificationsInboxService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    private final NotificationsInboxService inboxService;

    public NotificationsController(NotificationsInboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inboxService.getUnreadNotifications(userId));
    }

    @PostMapping
    public ResponseEntity<Void> markNotificationsRead(
            Authentication authentication,
            @Valid @RequestBody MarkNotificationsReadRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        inboxService.markAsRead(userId, request.notificationIds());
        return ResponseEntity.ok().build();
    }
}

