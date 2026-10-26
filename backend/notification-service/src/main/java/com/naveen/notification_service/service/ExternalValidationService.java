package com.naveen.notification_service.service;

import com.naveen.notification_service.client.CustomerClient;
import com.naveen.notification_service.client.LoanClient;
import com.naveen.notification_service.exception.CustomerNotFoundException;
import com.naveen.notification_service.exception.LoanNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalValidationService {

    private final CustomerClient customerClient;
    private final LoanClient loanClient;

    public void validateCustomer(Long customerId) {
        try {
            customerClient.getCustomerById(customerId);
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        } catch (FeignException.Unauthorized e) {
            throw new IllegalStateException(
                    "Customer Service authentication failed (HTTP 401)",
                    e
            );
        } catch (FeignException.Forbidden e) {
            throw new IllegalStateException(
                    "Notification Service is not authorized to access Customer Service (HTTP 403)",
                    e
            );
        } catch (FeignException e) {
            throw new IllegalStateException(
                    "Customer Service request failed with HTTP status: " + e.status(),
                    e
            );
        }
    }

    public void validateLoan(Long loanId) {
        try {
            loanClient.getLoanById(loanId);
        } catch(FeignException.NotFound e) {
            throw new LoanNotFoundException("Loan not found with ID: " + loanId);
        } catch(FeignException e) {
            throw new LoanNotFoundException("Unable to validate Loan with ID: " + loanId);
        }
    }

    public void validateLoanOwnership(Long customerId, Long loanId) {
        try {
            com.naveen.notification_service.dto.LoanResponse response = loanClient.getLoanById(loanId);

            if(!customerId.equals(response.customerId()))
                throw new LoanNotFoundException("Loan with ID: " + loanId + " doesn't belongs to Customer with ID: " + customerId);
        } catch(FeignException.NotFound e) {
            throw new LoanNotFoundException("Loan not found with ID: " + loanId);
        } catch(FeignException e) {
            throw new LoanNotFoundException("Unable to validate Loan with ID: " + loanId);
        }
    }
}
