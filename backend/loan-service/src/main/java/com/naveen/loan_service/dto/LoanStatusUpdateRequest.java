package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private LoanStatus status;

}
