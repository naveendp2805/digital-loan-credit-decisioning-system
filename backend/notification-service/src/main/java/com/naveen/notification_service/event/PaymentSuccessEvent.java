
package com.naveen.notification_service.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long paymentId;
    private Long loanId;
    private Long customerId;
    private Long repaymentId;
    private BigDecimal amount;
    private String currency;
    private String transactionReference;
    private String customerName;
    private String email;
}
