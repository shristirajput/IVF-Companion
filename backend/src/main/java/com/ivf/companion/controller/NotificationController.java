package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.Notification;
import com.ivf.companion.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Notification> notifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userPrincipal.getId());
        return ResponseEntity.ok(notifs);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Security validation
        if (!notification.getUser().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody NotificationSettingsRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // In a real app, we would save these preferences to the User entity.
        // For this phase, we acknowledge the preferences and send a test notification if enabled.
        if (request.isEmailEnabled()) {
            System.out.println("================ EMAIL NOTIFICATION ================");
            System.out.println("TO: " + userPrincipal.getUser().getEmail());
            System.out.println("SUBJECT: IVF Companion - Notifications Enabled");
            System.out.println("BODY: Hello, you have enabled email notifications.");
            System.out.println("====================================================");
        }

        if (request.isSmsEnabled() && request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            System.out.println("================= SMS NOTIFICATION =================");
            System.out.println("PHONE: " + request.getPhoneNumber());
            System.out.println("MESSAGE: IVF Companion: SMS notifications are now enabled.");
            System.out.println("====================================================");
        }

        return ResponseEntity.ok(new com.ivf.companion.dto.ApiResponse(true, "Notification settings updated successfully"));
    }
}

class NotificationSettingsRequest {
    private boolean emailEnabled;
    private boolean smsEnabled;
    private String phoneNumber;

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public boolean isSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
