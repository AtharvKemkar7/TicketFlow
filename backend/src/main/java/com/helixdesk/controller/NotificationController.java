package com.helixdesk.controller;

import com.helixdesk.entity.Notification;
import com.helixdesk.security.CurrentUser;
import com.helixdesk.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> mine() {
        Long userId = currentUser.requireUser().getId();
        return ResponseEntity.ok(notificationService.forUser(userId).stream().map(this::toMap).toList());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unread() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(currentUser.requireUser().getId())));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id) {
        notificationService.markRead(id, currentUser.requireUser().getId());
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toMap(Notification notification) {
        return Map.of(
                "id", notification.getId(),
                "type", notification.getType(),
                "message", notification.getMessage(),
                "read", notification.isReadFlag(),
                "ticketId", notification.getTicket() == null ? 0 : notification.getTicket().getId(),
                "createdAt", notification.getCreatedAt()
        );
    }
}
