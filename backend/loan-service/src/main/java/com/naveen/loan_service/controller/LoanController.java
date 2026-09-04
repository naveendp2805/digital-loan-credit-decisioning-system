package com.naveen.loan_service.controller;

import com.naveen.loan_service.dto.LoanRequest;
import com.naveen.loan_service.dto.LoanResponse;
import com.naveen.loan_service.dto.LoanStatusUpdateRequest;
import com.naveen.loan_service.entity.LoanStatus;
import com.naveen.loan_service.service.LoanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Validated
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;


    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(request));
    }


    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable @Positive(message = "Loan ID must be positive") Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }


    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanResponse>> getLoansByCustomer(@PathVariable @Positive(message = "Customer ID must be positive") Long customerId) {
        return ResponseEntity.ok(loanService.getLoansByCustomer(customerId));
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable("status") LoanStatus loanStatus) {
        return ResponseEntity.ok(loanService.getLoansByStatus(loanStatus));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<LoanResponse> updateLoanStatus(@PathVariable @Positive(message = "Loan ID must be positive") Long id, @Valid @RequestBody LoanStatusUpdateRequest request) {
        return ResponseEntity.ok(loanService.updateLoanStatus(id, request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable @Positive(message = "Loan ID must be positive") Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}