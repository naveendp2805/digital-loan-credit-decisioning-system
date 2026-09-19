package com.naveen.repayment_service.service;

import com.naveen.repayment_service.client.LoanClient;
import com.naveen.repayment_service.client.PaymentClient;
import com.naveen.repayment_service.dto.*;
import com.naveen.repayment_service.entity.Repayment;
import com.naveen.repayment_service.entity.RepaymentStatus;
import com.naveen.repayment_service.event.PaymentSuccessEvent;
import com.naveen.repayment_service.exception.InvalidRepaymentException;
import com.naveen.repayment_service.exception.ResourceNotFoundException;
import com.naveen.repayment_service.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanClient loanClient;
    private final RepaymentMapper mapper;
    private final PaymentClient paymentClient;


    public List<RepaymentResponse> generateSchedule(Long loanId) {

        validateLoanId(loanId);

        LoanResponse loan = getLoan(loanId);

        validateLoanForRepayment(loan);

        if(repaymentRepository.existsByLoanIdAndInstallmentNumber(loanId, 1))
            throw new InvalidRepaymentException("Repayment schedule already exists for loan ID: " + loanId);

        if(loan.emi() == null || loan.emi().compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidRepaymentException("Loan EMI must be greater than zero");

        if(loan.tenureMonths() == null || loan.tenureMonths() <= 0)
            throw new InvalidRepaymentException("Loan tenure must be greater than zero");

        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        List<Repayment> repayments = new ArrayList<>();
        for(int i=0; i<loan.tenureMonths(); i++)
        {
            LocalDate dueDate = firstDueDate.plusMonths(i);
            BigDecimal emi = loan.emi().setScale(2, RoundingMode.HALF_UP);

            Repayment repayment = Repayment.builder()
                        .loanId(loan.id())
                        .customerId(loan.customerId())
                        .installmentNumber(i+1)
                        .dueDate(dueDate)
                        .installmentAmount(emi)
                        .paidAmount(BigDecimal.ZERO)
                        .remainingAmount(emi)
                        .status(RepaymentStatus.PENDING)
                        .build();

            repayments.add(repayment);
        }

        return repaymentRepository.saveAll(repayments)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getLoanRepayments(Long loanId) {
        validateLoanId(loanId);

        getLoan(loanId);

        return repaymentRepository
                .findByLoanIdOrderByInstallmentNumberAsc(loanId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepaymentResponse getRepaymentById(Long repaymentId) {
        if (repaymentId == null || repaymentId <= 0)
            throw new InvalidRepaymentException("Repayment ID must be positive");

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found with ID: " + repaymentId));

        return mapper.toResponse(repayment);
    }

    public PaymentOrderResponse makeRepayment(
            RepaymentRequest request
    ) {

        Repayment repayment = repaymentRepository.findByLoanIdAndInstallmentNumber(request.loanId(), request.installmentNumber())
                        .orElseThrow(() -> new ResourceNotFoundException("Installment not found"));

        if (repayment.getStatus() == RepaymentStatus.PAID) {
            throw new InvalidRepaymentException("Installment is already paid");
        }

        BigDecimal payment = request.paymentAmount().setScale(2, RoundingMode.HALF_UP);

        if (payment.compareTo(repayment.getRemainingAmount()) > 0) {
            throw new InvalidRepaymentException("Payment amount cannot exceed remaining amount");
        }

        PaymentOrderRequest paymentRequest =
                new PaymentOrderRequest(
                        repayment.getLoanId(),
                        repayment.getCustomerId(),
                        repayment.getId(),
                        payment,
                        "INR",
                        "REPAYMENT",
                        request.idempotencyKey()
                );

        return paymentClient.createPayment(paymentRequest);
    }

    public void processPaymentSuccess(
            PaymentSuccessEvent event
    ) {

        if (event.repaymentId() == null) {

            throw new InvalidRepaymentException(
                    "Payment event does not contain repayment ID"
            );
        }

        Repayment repayment =
                repaymentRepository
                        .findById(event.repaymentId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Repayment not found with ID: "
                                                + event.repaymentId()
                                )
                        );

        if (!repayment.getLoanId()
                .equals(event.loanId())) {

            throw new InvalidRepaymentException(
                    "Payment loan does not match repayment loan"
            );
        }

        if (!repayment.getCustomerId()
                .equals(event.customerId())) {

            throw new InvalidRepaymentException(
                    "Payment customer does not match repayment customer"
            );
        }

        BigDecimal payment =
                event.amount()
                        .setScale(2, RoundingMode.HALF_UP);

        if (repayment.getStatus() == RepaymentStatus.PAID) {

            log.info(
                    "Repayment {} already paid. Ignoring duplicate payment event.",
                    repayment.getId()
            );

            return;
        }

        if (payment.compareTo(
                repayment.getRemainingAmount()
        ) > 0) {

            throw new InvalidRepaymentException(
                    "Payment amount exceeds repayment remaining amount"
            );
        }

        BigDecimal newPaidAmount =
                repayment.getPaidAmount()
                        .add(payment);

        BigDecimal newRemainingAmount =
                repayment.getInstallmentAmount()
                        .subtract(newPaidAmount);

        repayment.setPaidAmount(newPaidAmount);
        repayment.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            repayment.setStatus(
                    RepaymentStatus.PAID
            );

            repayment.setPaidDate(
                    LocalDate.now()
            );

        } else {

            repayment.setStatus(
                    RepaymentStatus.PARTIALLY_PAID
            );
        }

        repaymentRepository.save(repayment);

        log.info(
                "Repayment updated successfully | repaymentId={} | paid={} | remaining={} | status={}",
                repayment.getId(),
                repayment.getPaidAmount(),
                repayment.getRemainingAmount(),
                repayment.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getOverdueRepayments(Long loanId) {
        validateLoanId(loanId);

        getLoan(loanId);

        LocalDate today = LocalDate.now();

        return repaymentRepository.findByLoanIdAndStatus(loanId, RepaymentStatus.PENDING)
                .stream()
                .filter(r -> r.getDueDate().isBefore(today))
                .map(r -> {
                    r.setStatus(RepaymentStatus.OVERDUE);
                    return mapper.toResponse(r);
                })
                .toList();
    }

    private LoanResponse getLoan(Long loanId) {
        try {
            LoanResponse loan = loanClient.getLoanById(loanId);

            if (loan == null)
                throw new ResourceNotFoundException("Loan not found with ID: " + loanId);

            return loan;
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Unable to retrieve loan with ID: " + loanId);
        }
    }

    private void validateLoanId(Long loanId) {
        if (loanId == null || loanId <= 0)
            throw new InvalidRepaymentException("Loan ID must be positive");
    }

    private void validateLoanForRepayment(LoanResponse loan) {
        if (loan.id() == null || loan.customerId() == null)
            throw new InvalidRepaymentException("Loan contains invalid customer information");

        if (loan.loanAmount() == null || loan.loanAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidRepaymentException("Loan amount must be greater than zero");

        if (loan.status() == null)
            throw new InvalidRepaymentException("Loan status is required");

        if (!loan.status().equalsIgnoreCase("APPROVED") &&
                !loan.status().equalsIgnoreCase("DISBURSED"))
            throw new InvalidRepaymentException("Repayment schedule can only be generated " + "for an approved/disbursed loan");

    }

}
