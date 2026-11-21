package be.ucll.controller;

import be.ucll.model.*;
import be.ucll.repository.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationController(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications; this.users = users;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public List<Map<String,Object>> all(Principal principal) {
        User user = users.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        List<Notification> source = user.getRole() == User.Role.ADMIN
                ? notifications.findAllByOrderByCreatedAtDesc()
                : notifications.findByTrainerOrderByCreatedAtDesc(user);
        List<Map<String,Object>> result = new ArrayList<>();
        for (Notification n : source) result.add(Map.of("id", n.getId(), "message", n.getMessage(),
                "read", n.isReadStatus(), "createdAt", n.getCreatedAt().toString()));
        return result;
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<?> read(@PathVariable Long id) {
        return notifications.findById(id).map(n -> { n.setReadStatus(true); return ResponseEntity.ok(notifications.save(n)); })
                .orElse(ResponseEntity.notFound().build());
    }
}
