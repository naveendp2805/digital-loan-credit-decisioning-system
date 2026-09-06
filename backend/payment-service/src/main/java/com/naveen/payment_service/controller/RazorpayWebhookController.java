package com.naveen.payment_service.controller;

import com.naveen.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(@RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
                                                @RequestBody String payload
    ) {

        if (signature == null || signature.isBlank())
            return ResponseEntity.badRequest().body("Missing Razorpay webhook signature");

        paymentService.processWebhook(payload, signature);

        return ResponseEntity.ok("Webhook processed successfully");
    }
}
