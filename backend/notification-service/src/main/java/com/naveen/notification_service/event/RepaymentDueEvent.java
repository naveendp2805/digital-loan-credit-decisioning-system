package com.naveen.notification_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentDueEvent {

    private Long customerId;

    private Long loanId;

    private String customerName;

    private String email;

    private Integer installmentNumber;

    private Double amount;

    private String dueDate;
}
