package com.naveen.customer_service.dto;

import com.naveen.customer_service.entity.EmploymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private BigDecimal monthlyIncome;
    private EmploymentType employmentType;
}
