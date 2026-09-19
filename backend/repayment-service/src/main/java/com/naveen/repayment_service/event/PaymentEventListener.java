package com.naveen.repayment_service.event;

import com.naveen.repayment_service.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final RepaymentService repaymentService;

    @KafkaListener(
            topics = "payment-success",
            groupId = "repayment-service"
    )
    public void handlePaymentSuccess(
            PaymentSuccessEvent event
    ) {

        log.info(
                "Received PaymentSuccessEvent | paymentId={} | repaymentId={} | amount={}",
                event.paymentId(),
                event.repaymentId(),
                event.amount()
        );

        repaymentService.processPaymentSuccess(event);
    }
}