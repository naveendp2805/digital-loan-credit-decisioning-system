package com.naveen.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReconcilePaymentRequest(
        @NotNull
        @Positive
        Long localPaymentId,

        @NotBlank
        String razorpayPaymentId
) {}