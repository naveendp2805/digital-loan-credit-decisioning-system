package com.naveen.payment_service.client;

import com.naveen.payment_service.entity.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service", url = "${services.loan.url}")
public interface LoanClient {

    @GetMapping("/api/loans/{loanId}")
    LoanResponse getLoanById(@PathVariable("loanId") Long loanId);
}
