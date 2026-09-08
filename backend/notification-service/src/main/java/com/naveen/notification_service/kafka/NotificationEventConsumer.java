package com.naveen.notification_service.kafka;

import com.naveen.notification_service.event.LoanApprovedEvent;
import com.naveen.notification_service.event.PaymentSuccessEvent;
import com.naveen.notification_service.event.RepaymentDueEvent;
import com.naveen.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "loan-approved", groupId = "notification-service", containerFactory = "loanApprovedKafkaListenerContainerFactory")
    public void consumeLoanApproved(LoanApprovedEvent event) {
        notificationService.handleLoanApproved(event);
    }

    @KafkaListener(topics = "payment-success", groupId = "notification-service", containerFactory = "paymentSuccessKafkaListenerContainerFactory")
    public void consumePaymentSuccess(PaymentSuccessEvent event) {
        notificationService.handlePaymentSuccess(event);
    }

    @KafkaListener(topics = "repayment-due", groupId = "notification-service", containerFactory = "repaymentDueKafkaListenerContainerFactory")
    public void consumeRepaymentDue(RepaymentDueEvent event) {
        notificationService.handleRepaymentDue(event);
    }
}
