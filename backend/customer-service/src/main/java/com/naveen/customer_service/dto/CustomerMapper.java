package com.naveen.customer_service.dto;

import com.naveen.customer_service.entity.Customer;

public class CustomerMapper {

    public static CustomerResponse toDto(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .monthlyIncome(customer.getMonthlyIncome())
                .employmentType(customer.getEmploymentType())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

}
