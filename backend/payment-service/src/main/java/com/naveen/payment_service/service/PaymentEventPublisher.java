
package com.naveen.payment_service.service;

import com.naveen.payment_service.client.CustomerClient;
import com.naveen.payment_service.dto.CustomerResponse;
import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String TOPIC = "payment-success";

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;
    private final CustomerClient customerClient;

    public void publishPaymentSuccess(Payment payment) {

        CustomerResponse customer =
                customerClient.getCustomerById(payment.getCustomerId());

        String customerName =
                (customer.getFirstName() == null ? "" : customer.getFirstName())
                        + " "
                        + (customer.getLastName() == null ? "" : customer.getLastName());

        PaymentSuccessEvent event =
                new PaymentSuccessEvent(
                        payment.getId(),
                        payment.getLoanId(),
                        payment.getCustomerId(),
                        payment.getRepaymentId(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getTransactionReference(),
                        customerName.trim(),
                        customer.getEmail()
                );

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(payment.getId()),
                event
        );
    }
}
