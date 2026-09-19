package com.naveen.document_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;
}
