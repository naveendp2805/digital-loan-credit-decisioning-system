package com.naveen.notification_service.service;

import com.naveen.notification_service.dto.NotificationCreateRequest;
import com.naveen.notification_service.dto.NotificationMapper;
import com.naveen.notification_service.dto.NotificationResponse;
import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationChannel;
import com.naveen.notification_service.entity.NotificationStatus;
import com.naveen.notification_service.exception.NotificationNotFoundException;
import com.naveen.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;
    private final EmailService emailService;
    private final ExternalValidationService externalValidationService;
    private final NotificationTemplateService notificationTemplateService;

    public NotificationResponse createNotification(NotificationCreateRequest request) {

        externalValidationService.validateCustomer(request.getCustomerId());

        externalValidationService.validateLoan(request.getLoanId());

        externalValidationService.validateLoanOwnership(request.getCustomerId(), request.getLoanId());

        String subject = notificationTemplateService.generateSubject(request.getNotificationType());

        String message = notificationTemplateService.generateMessage(request.getNotificationType(), request.getData());

        Notification notification = mapper.toEntity(request, subject, message);

        notification.setStatus(NotificationStatus.PENDING);

        Notification savedNotification = notificationRepository.save(notification);

        return mapper.toResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {

        Notification notification = notificationRepository.findById(id)
                        .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));

        return mapper.toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCustomer(Long customerId) {
        return notificationRepository
                .findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByLoan(Long loanId) {
        return notificationRepository
                .findByLoanId(loanId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public NotificationResponse sendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));

        if(notification.getStatus() == NotificationStatus.SENT)
            throw new IllegalStateException( "Notification has already been sent");

        try {
            if(notification.getChannel() == NotificationChannel.EMAIL)
                emailService.sendEmail(notification.getRecipient(), notification.getSubject(), notification.getMessage());
            else
                throw new UnsupportedOperationException( "Channel not supported yet: " + notification.getChannel());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch(Exception e) {
            notification.setStatus(NotificationStatus.FAILED);

            throw new RuntimeException("Failed to send notification", e);
        }

        Notification updatedNotification = notificationRepository.save(notification);

        return mapper.toResponse(updatedNotification);
    }
}
