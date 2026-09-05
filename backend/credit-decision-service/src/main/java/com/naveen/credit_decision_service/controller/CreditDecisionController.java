package com.naveen.credit_decision_service.controller;

import com.naveen.credit_decision_service.dto.CreditDecisionRequest;
import com.naveen.credit_decision_service.dto.CreditDecisionResponse;
import com.naveen.credit_decision_service.service.CreditDecisionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-decisions")
@RequiredArgsConstructor
@Validated
public class CreditDecisionController {

    private final CreditDecisionService service;

    @PostMapping
    public ResponseEntity<CreditDecisionResponse> makeDecision(@Valid @RequestBody CreditDecisionRequest request) {
        CreditDecisionResponse response = service.makeDecision(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditDecisionResponse> getDecisionById(@PathVariable @Positive(message = "ID must be positive") Long id) {
        return ResponseEntity.ok(service.getDecisionById(id));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<CreditDecisionResponse> getDecisionByLoanId(@PathVariable @Positive(message = "Loan ID must be positive") Long loanId) {
        return ResponseEntity.ok(service.getDecisionByLoanId(loanId));
    }

    @GetMapping
    public ResponseEntity<List<CreditDecisionResponse>> getAllDecisions() {
        return ResponseEntity.ok(service.getAllDecisions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable @Positive(message = "ID must be positive") Long id) {
        return ResponseEntity.ok(service.deleteDecision(id));
    }
}