package com.naveen.loan_service.service;

import com.naveen.loan_service.client.CreditDecisionClient;
import com.naveen.loan_service.client.CustomerClient;
import com.naveen.loan_service.dto.*;
import com.naveen.loan_service.entity.Loan;
import com.naveen.loan_service.entity.LoanStatus;
import com.naveen.loan_service.entity.LoanType;
import com.naveen.loan_service.exception.CreditDecisionException;
import com.naveen.loan_service.exception.CustomerNotFoundException;
import com.naveen.loan_service.exception.CustomerServiceException;
import com.naveen.loan_service.exception.LoanNotFoundException;
import com.naveen.loan_service.repository.LoanRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final CustomerClient customerClient;
    private final CreditDecisionClient creditDecisionClient;

    public LoanResponse createLoan(LoanRequest request) {

        validateCustomer(request.getCustomerId());

        BigDecimal existingDebt = loanRepository.calculateExistingMonthlyDebt(request.getCustomerId());

        Loan loan = loanMapper.toEntity(request);

        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setApplicationDate(LocalDateTime.now());

        BigDecimal interestRate = calculateInterestRate(request.getLoanType());
        loan.setInterestRate(interestRate);

        BigDecimal emi = calculateEMI(request.getLoanAmount(), interestRate, request.getTenureMonths());
        loan.setEmi(emi);

        Loan savedLoan = loanRepository.save(loan);

        CreditDecisionRequest creditRequest = CreditDecisionRequest.builder()
                                        .loanId(savedLoan.getId())
                                        .creditScore(request.getCreditScore())
                                        .monthlyIncome(request.getMonthlyIncome())
                                        .existingDebt(existingDebt)
                                        .loanAmount(request.getLoanAmount())
                                        .build();

        CreditDecisionResponse creditResponse;

        try {
            creditResponse = creditDecisionClient.makeDecision(creditRequest);
        } catch (FeignException e) {
            throw new CreditDecisionException("Credit Decision Service is unavailable");
        }

        applyCreditDecision(savedLoan, creditResponse);

        Loan updatedLoan = loanRepository.save(savedLoan);

        return loanMapper.toResponse(updatedLoan);
    }

    private void validateCustomer(Long customerId) {

        try {
            customerClient.getCustomerById(customerId);
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException(customerId);
        } catch (FeignException e) {
            throw new CustomerServiceException("Customer service is unavailable");
        }
    }

    private BigDecimal calculateInterestRate(LoanType type) {
        return switch (type) {
            case PERSONAL -> BigDecimal.valueOf(12.00);
            case HOME -> BigDecimal.valueOf(8.50);
            case EDUCATION -> BigDecimal.valueOf(7.00);
            case VEHICLE -> BigDecimal.valueOf(9.00);
        };
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        double p = principal.doubleValue();
        double monthlyRate = annualRate.doubleValue() / 12 / 100;
        int n = tenureMonths;

        double emi;

        if (monthlyRate == 0) {
            emi = p / n;
        } else {
            emi = p * monthlyRate * Math.pow(1 + monthlyRate, n) / (Math.pow(1 + monthlyRate, n) - 1);
        }

        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    private void applyCreditDecision(Loan loan, CreditDecisionResponse creditResponse) {
        loan.setCreditScore(creditResponse.getCreditScore());

        String decision = creditResponse.getDecisionStatus();

        switch (decision) {
            case "APPROVED" -> {
                loan.setLoanStatus(LoanStatus.APPROVED);
                loan.setRejectionReason(null);
                loan.setApprovedDate(LocalDateTime.now());
            }

            case "REJECTED" -> {
                loan.setLoanStatus(LoanStatus.REJECTED);
                loan.setRejectionReason(creditResponse.getDecisionReason());
            }

            case "REVIEW" -> {
                loan.setLoanStatus(LoanStatus.PENDING);
                loan.setRejectionReason(null);
            }

            default -> throw new CreditDecisionException("Invalid Credit decision received: " + decision);
        }
    }

    @Transactional
    public LoanResponse getLoanById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        return loanMapper.toResponse(loan);
    }

    @Transactional
    public List<LoanResponse> getAllLoans() {

        return loanRepository.findAll()
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<LoanResponse> getLoansByCustomer(Long customerId) {

        validateCustomer(customerId);

        return loanRepository
                .findByCustomerId(customerId)
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<LoanResponse> getLoansByStatus(LoanStatus loanStatus) {

        return loanRepository
                .findByLoanStatus(loanStatus)
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    public LoanResponse updateLoanStatus(Long id, LoanStatusUpdateRequest request) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        loan.setLoanStatus(request.getStatus());

        if(request.getStatus() == LoanStatus.APPROVED)
            loan.setApprovedDate(LocalDateTime.now());

        return loanMapper.toResponse(loanRepository.save(loan));
    }

    public void deleteLoan(Long id) {

        if(!loanRepository.existsById(id))
            throw new LoanNotFoundException(id);

        loanRepository.deleteById(id);
    }

}
