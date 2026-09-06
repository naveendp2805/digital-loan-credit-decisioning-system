package com.naveen.repayment_service.controller;

import com.naveen.repayment_service.dto.RepaymentRequest;
import com.naveen.repayment_service.dto.RepaymentResponse;
import com.naveen.repayment_service.service.RepaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
@RequiredArgsConstructor
@Validated
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping("/schedule/{loanId}")
    public ResponseEntity<List<RepaymentResponse>> generateSchedule(@PathVariable @Positive(message = "Loan ID must be positive") Long loanId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repaymentService.generateSchedule(loanId));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<RepaymentResponse>> getLoanRepayments(@PathVariable @Positive(message = "Loan ID must be positive") Long loanId) {
        return ResponseEntity.ok(repaymentService.getLoanRepayments(loanId));
    }

    @GetMapping("/{repaymentId}")
    public ResponseEntity<RepaymentResponse> getRepayment(@PathVariable @Positive(message = "Repayment ID must be positive") Long repaymentId) {
        return ResponseEntity.ok(repaymentService.getRepaymentById(repaymentId));
    }

    @PostMapping
    public ResponseEntity<RepaymentResponse> makeRepayment(@Valid @RequestBody RepaymentRequest request) {
        return ResponseEntity.ok(repaymentService.makeRepayment(request));
    }

    @GetMapping("/loan/{loanId}/overdue")
    public ResponseEntity<List<RepaymentResponse>> getOverdueRepayments(@PathVariable @Positive(message = "Loan ID must be positive") Long loanId) {
        return ResponseEntity.ok(repaymentService.getOverdueRepayments(loanId));
    }
}
