package com.naveen.payment_service.service;

import com.naveen.payment_service.client.CustomerClient;
import com.naveen.payment_service.client.LoanClient;
import com.naveen.payment_service.entity.LoanResponse;
import com.naveen.payment_service.exception.CustomerNotFoundException;
import com.naveen.payment_service.exception.LoanNotFoundException;
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
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        } catch (FeignException e) {
            throw new CustomerNotFoundException("Unable to validate Customer with ID: " + customerId);
        }
    }


    public void validateLoanOwnership(Long customerId, Long loanId) {
        try {
            LoanResponse loan = loanClient.getLoanById(loanId);
            if (loan.customerId() == null)
                throw new CustomerNotFoundException("Loan " + loanId + " does not contain customer information");

            if (!customerId.equals(loan.customerId()))
                throw new LoanNotFoundException("Loan with ID " + loanId + " does not belong to Customer with ID " + customerId);

        } catch (FeignException.NotFound e) {
            throw new LoanNotFoundException("Loan not found with ID: " + loanId);
        } catch (FeignException e) {
            throw new CustomerNotFoundException("Unable to validate Loan with ID: " + loanId);
        }
    }
}