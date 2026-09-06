package com.naveen.document_service.service;

import com.naveen.document_service.client.CustomerClient;
import com.naveen.document_service.client.LoanClient;
import com.naveen.document_service.exception.CustomerNotFoundException;
import com.naveen.document_service.exception.LoanNotFoundException;
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
        } catch(FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        } catch(FeignException e) {
            throw new CustomerNotFoundException("Unable to validate Customer with ID: " + customerId);
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
            com.naveen.document_service.entity.LoanResponse response = loanClient.getLoanById(loanId);

            if(!customerId.equals(response.customerId()))
                throw new LoanNotFoundException("Loan with ID: " + loanId + " doesn't belongs to Customer with ID: " + customerId);
        } catch(FeignException.NotFound e) {
            throw new LoanNotFoundException("Loan not found with ID: " + loanId);
        } catch(FeignException e) {
            throw new LoanNotFoundException("Unable to validate Loan with ID: " + loanId);
        }
    }
}
