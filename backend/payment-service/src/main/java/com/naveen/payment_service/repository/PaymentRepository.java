package com.naveen.payment_service.repository;

import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByRepaymentId(Long repaymentId);

    List<Payment> findByLoanId(Long loanId);

    List<Payment> findByCustomerId(Long customerId);

    boolean existsByRepaymentIdAndStatus(Long repaymentId, PaymentStatus status);

}
