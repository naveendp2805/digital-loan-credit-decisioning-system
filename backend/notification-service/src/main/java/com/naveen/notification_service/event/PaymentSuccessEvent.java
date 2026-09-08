package com.naveen.notification_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long customerId;

    private Long loanId;

    private String paymentId;

    private String customerName;

    private String email;

    private Double amount;
}
