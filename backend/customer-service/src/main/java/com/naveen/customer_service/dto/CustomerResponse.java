package com.naveen.customer_service.dto;

import com.naveen.customer_service.entity.EmploymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private BigDecimal monthlyIncome;
    private EmploymentType employmentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
