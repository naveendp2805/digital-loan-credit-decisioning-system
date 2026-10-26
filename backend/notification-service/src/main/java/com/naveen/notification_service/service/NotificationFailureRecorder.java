
package com.naveen.notification_service.service;

import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationStatus;
import com.naveen.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationFailureRecorder {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Notification not found: " + notificationId
                ));

        notification.setStatus(NotificationStatus.FAILED);
        notification.setSentAt(null);
        notification.setUpdatedAt(LocalDateTime.now());

        notificationRepository.saveAndFlush(notification);
    }
}
