
package com.naveen.notification_service.service;

import com.naveen.notification_service.dto.NotificationCreateRequest;
import com.naveen.notification_service.dto.NotificationMapper;
import com.naveen.notification_service.dto.NotificationResponse;
import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationStatus;
import com.naveen.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;
    private final NotificationTemplateService templateService;
    private final ExternalValidationService externalValidationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse createNotification(
            NotificationCreateRequest request,
            String eventKey) {

        externalValidationService.validateCustomer(request.getCustomerId());
        externalValidationService.validateLoan(request.getLoanId());
        externalValidationService.validateLoanOwnership(
                request.getCustomerId(),
                request.getLoanId());

        String subject =
                templateService.generateSubject(request.getNotificationType());

        String message =
                templateService.generateMessage(
                        request.getNotificationType(),
                        request.getData());

        Notification notification = mapper.toEntity(
                request, subject, message, eventKey);

        notification.setStatus(NotificationStatus.PENDING);

        Notification saved = notificationRepository.saveAndFlush(notification);

        return mapper.toResponse(saved);
    }
}
