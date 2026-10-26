
package com.naveen.notification_service.service;

import com.naveen.notification_service.dto.NotificationCreateRequest;
import com.naveen.notification_service.dto.NotificationMapper;
import com.naveen.notification_service.dto.NotificationResponse;
import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationChannel;
import com.naveen.notification_service.entity.NotificationStatus;
import com.naveen.notification_service.entity.NotificationType;
import com.naveen.notification_service.event.LoanApprovedEvent;
import com.naveen.notification_service.event.PaymentSuccessEvent;
import com.naveen.notification_service.event.RepaymentDueEvent;
import com.naveen.notification_service.exception.NotificationNotFoundException;
import com.naveen.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;
    private final EmailService emailService;
    private final ExternalValidationService externalValidationService;
    private final NotificationTemplateService notificationTemplateService;
    private final NotificationFailureRecorder notificationFailureRecorder;
    private final NotificationPersistenceService notificationPersistenceService;

    public NotificationResponse createNotification(
            NotificationCreateRequest request) {
        return createNotification(request, null);
    }

    private NotificationResponse createNotification(
            NotificationCreateRequest request,
            String eventKey) {

        externalValidationService.validateCustomer(
                request.getCustomerId());

        externalValidationService.validateLoan(
                request.getLoanId());

        externalValidationService.validateLoanOwnership(
                request.getCustomerId(),
                request.getLoanId());

        String subject =
                notificationTemplateService.generateSubject(
                        request.getNotificationType());

        String message =
                notificationTemplateService.generateMessage(
                        request.getNotificationType(),
                        request.getData());

        Notification notification = mapper.toEntity(
                request,
                subject,
                message,
                eventKey);

        notification.setStatus(NotificationStatus.PENDING);

        Notification savedNotification =
                notificationRepository.saveAndFlush(notification);

        return mapper.toResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found with id: " + id));

        return mapper.toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCustomer(
            Long customerId) {

        return notificationRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByLoan(
            Long loanId) {

        return notificationRepository.findByLoanId(loanId)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse sendNotification(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + id
                ));

        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Notification has already been sent");
        }

        try {
            if (notification.getChannel() == NotificationChannel.EMAIL) {
                emailService.sendEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );
            } else {
                throw new UnsupportedOperationException(
                        "Channel not supported yet: " + notification.getChannel()
                );
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

            return mapper.toResponse(notificationRepository.save(notification));

        } catch (Exception e) {
            notificationFailureRecorder.recordFailure(id);

            throw new IllegalStateException(
                    "Failed to send notification id=" + id,
                    e
            );
        }
    }


    public void handleLoanApproved(LoanApprovedEvent event) {

        Map<String, Object> data = Map.of(
                "customerName", event.getCustomerName(),
                "loanId", event.getLoanId(),
                "amount", event.getAmount()
        );

        NotificationCreateRequest request =
                NotificationCreateRequest.builder()
                        .customerId(event.getCustomerId())
                        .loanId(event.getLoanId())
                        .notificationType(NotificationType.LOAN_APPROVED)
                        .channel(NotificationChannel.EMAIL)
                        .recipient(event.getEmail())
                        .data(data)
                        .build();

        NotificationResponse notification =
                notificationPersistenceService.createNotification(request, null);

        sendNotification(notification.getId());
    }

    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        if (event.getPaymentId() == null) {
            throw new IllegalArgumentException(
                    "Payment success event must contain a paymentId");
        }

        if (event.getCustomerId() == null
                || event.getLoanId() == null) {
            throw new IllegalArgumentException(
                    "Payment success event must contain customerId and loanId");
        }

        if (event.getEmail() == null
                || event.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment success event has no customer email; paymentId="
                            + event.getPaymentId());
        }

        String eventKey =
                "PAYMENT_SUCCESS:" + event.getPaymentId();

        // Do not create or send a duplicate for an event already recorded.
        var existingNotification =
                notificationRepository.findByEventKey(eventKey);

        if (existingNotification.isPresent()) {
            Notification existing = existingNotification.get();

            if (existing.getStatus() == NotificationStatus.SENT
                    || existing.getStatus() == NotificationStatus.PENDING) {
                return;
            }

            if (existing.getStatus() == NotificationStatus.FAILED) {
                sendNotification(existing.getId());
                return;
            }

            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put(
                "customerName",
                event.getCustomerName() == null
                        ? "" : event.getCustomerName());
        data.put("paymentId", event.getPaymentId().toString());
        data.put(
                "amount",
                event.getAmount() == null
                        ? "" : event.getAmount().toPlainString());
        data.put(
                "currency",
                event.getCurrency() == null
                        ? "" : event.getCurrency());

        NotificationCreateRequest request =
                NotificationCreateRequest.builder()
                        .customerId(event.getCustomerId())
                        .loanId(event.getLoanId())
                        .notificationType(NotificationType.PAYMENT_SUCCESS)
                        .channel(NotificationChannel.EMAIL)
                        .recipient(event.getEmail())
                        .data(data)
                        .build();

        NotificationResponse notification;

        try {
            // Save the idempotency key in the initial INSERT.
            notification = notificationPersistenceService.createNotification(request, eventKey);
        } catch (DataIntegrityViolationException e) {
            /*
             * A concurrent duplicate may have inserted the same key
             * after our initial lookup. Do not send another email.
             *
             * The exception must escape this transactional method
             * so the failed transaction can roll back. A retry can
             * then find the committed notification.
             */
            throw e;
        }

        sendNotification(notification.getId());
    }

    public void handleRepaymentDue(RepaymentDueEvent event) {

        Map<String, Object> data = Map.of(
                "customerName", event.getCustomerName(),
                "loanId", event.getLoanId(),
                "installmentNumber", event.getInstallmentNumber(),
                "amount", event.getAmount(),
                "dueDate", event.getDueDate()
        );

        NotificationCreateRequest request =
                NotificationCreateRequest.builder()
                        .customerId(event.getCustomerId())
                        .loanId(event.getLoanId())
                        .notificationType(NotificationType.REPAYMENT_DUE)
                        .channel(NotificationChannel.EMAIL)
                        .recipient(event.getEmail())
                        .data(data)
                        .build();

        NotificationResponse notification =
                notificationPersistenceService.createNotification(request, null);

        sendNotification(notification.getId());
    }
}
