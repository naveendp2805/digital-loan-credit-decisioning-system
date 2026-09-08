package com.naveen.notification_service.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovedEvent {

    private Long customerId;

    private Long loanId;

    private String customerName;

    private String email;

    private Double amount;
}
