package com.naveen.document_service.client;

import com.naveen.document_service.entity.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service", url = "${services.loan.url}")
public interface LoanClient {

    @GetMapping("/api/loans/{loanId}")
    LoanResponse getLoanById(@PathVariable Long loanId);
}
