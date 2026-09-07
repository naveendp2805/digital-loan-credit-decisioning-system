package com.naveen.notification_service.service;

import com.naveen.notification_service.entity.NotificationType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationTemplateService {

    public String generateSubject(NotificationType type) {
        return switch (type) {
            case LOAN_APPROVED -> "Loan Application Approved";
            case LOAN_REJECTED -> "Loan Application Rejected";
            case LOAN_DISBURSED -> "Loan Amount Disbursed";
            case PAYMENT_SUCCESS -> "Payment Successful";
            case PAYMENT_FAILED -> "Payment Failed";
            case REPAYMENT_DUE -> "Repayment Due";
            case REPAYMENT_OVERDUE -> "Repayment Overdue";
        };
    }

    public String generateMessage(
            NotificationType type,
            Map<String, Object> data) {

        String customerName = getValue(data, "customerName", "Customer");

        return switch (type) {

            case LOAN_APPROVED -> """
                    Dear %s,

                    Your loan application has been approved successfully.

                    Loan ID: %s
                    Loan Amount: ₹%s

                    Thank you for choosing our service.
                    """.formatted(
                    customerName,
                    getValue(data, "loanId", ""),
                    getValue(data, "amount", "")
            );

            case LOAN_REJECTED -> """
                    Dear %s,

                    We regret to inform you that your loan application
                    has been rejected.

                    Loan ID: %s

                    Please contact our support team for more information.
                    """.formatted(
                    customerName,
                    getValue(data, "loanId", "")
            );

            case LOAN_DISBURSED -> """
                    Dear %s,

                    Your loan amount has been successfully disbursed.

                    Loan ID: %s
                    Amount: ₹%s

                    Thank you for choosing our service.
                    """.formatted(
                    customerName,
                    getValue(data, "loanId", ""),
                    getValue(data, "amount", "")
            );

            case PAYMENT_SUCCESS -> """
                    Dear %s,

                    Your payment has been successfully processed.

                    Payment ID: %s
                    Amount: ₹%s

                    Thank you.
                    """.formatted(
                    customerName,
                    getValue(data, "paymentId", ""),
                    getValue(data, "amount", "")
            );

            case PAYMENT_FAILED -> """
                    Dear %s,

                    Unfortunately, your payment could not be processed.

                    Payment ID: %s
                    Amount: ₹%s

                    Please try again or contact support.
                    """.formatted(
                    customerName,
                    getValue(data, "paymentId", ""),
                    getValue(data, "amount", "")
            );

            case REPAYMENT_DUE -> """
                    Dear %s,

                    Your loan repayment is due soon.

                    Loan ID: %s
                    Installment: %s
                    Due Amount: ₹%s
                    Due Date: %s

                    Please make the payment before the due date.
                    """.formatted(
                    customerName,
                    getValue(data, "loanId", ""),
                    getValue(data, "installmentNumber", ""),
                    getValue(data, "amount", ""),
                    getValue(data, "dueDate", "")
            );

            case REPAYMENT_OVERDUE -> """
                    Dear %s,

                    Your loan repayment is overdue.

                    Loan ID: %s
                    Installment: %s
                    Outstanding Amount: ₹%s

                    Please make the payment as soon as possible.
                    """.formatted(
                    customerName,
                    getValue(data, "loanId", ""),
                    getValue(data, "installmentNumber", ""),
                    getValue(data, "amount", "")
            );
        };
    }

    private String getValue(Map<String, Object> data, String key, String defaultValue) {

        if (data == null)
            return defaultValue;

        Object value = data.get(key);

        return value != null ? value.toString() : defaultValue;
    }
}
