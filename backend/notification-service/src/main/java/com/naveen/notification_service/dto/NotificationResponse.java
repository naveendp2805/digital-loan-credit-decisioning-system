package com.naveen.notification_service.dto;

import com.naveen.notification_service.entity.NotificationChannel;
import com.naveen.notification_service.entity.NotificationStatus;
import com.naveen.notification_service.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long customerId;

    private Long loanId;

    private NotificationType notificationType;

    private NotificationChannel channel;

    private String recipient;

    private String subject;

    private String message;

    private NotificationStatus status;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
