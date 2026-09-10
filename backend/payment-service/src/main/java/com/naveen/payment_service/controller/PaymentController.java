package com.naveen.payment_service.controller;

import com.naveen.payment_service.dto.*;
import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Payment> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) throws RazorpayException {
        return ResponseEntity.ok(paymentService.verifyPayment(request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature()));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable @Positive(message = "Payment ID must be positive") Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByLoan(@PathVariable @Positive(message = "Loan ID must be positive") Long loanId) {
        return ResponseEntity.ok(paymentService.getPaymentsByLoan(loanId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomer(@PathVariable @Positive(message = "Customer ID must be positive") Long customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId));
    }
}