package com.naveen.notification_service.controller;

import com.naveen.notification_service.dto.NotificationCreateRequest;
import com.naveen.notification_service.dto.NotificationResponse;
import com.naveen.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotificationsByCustomer(customerId));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(notificationService.getNotificationsByLoan(loanId));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<NotificationResponse> sendNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.sendNotification(id));
    }
}
