package com.naveen.notification_service.dto;

import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationStatus;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {


    public Notification toEntity(
            NotificationCreateRequest request,
            String subject,
            String message
    ) {
        return toEntity(request, subject, message, null);
    }

    public Notification toEntity(
            NotificationCreateRequest request,
            String subject,
            String message,
            String eventKey
    ) {
        return Notification.builder()
                .customerId(request.getCustomerId())
                .loanId(request.getLoanId())
                .eventKey(eventKey)
                .notificationType(request.getNotificationType())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();
    }


    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomerId())
                .loanId(notification.getLoanId())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
