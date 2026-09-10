package com.naveen.payment_service.controller;

import com.naveen.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(

            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            )
            String signature,

            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            )
            String eventId,

            @RequestBody String payload
    ) {

        paymentService.processWebhook(
                payload,
                signature,
                eventId
        );

        return ResponseEntity.ok().build();
    }
}