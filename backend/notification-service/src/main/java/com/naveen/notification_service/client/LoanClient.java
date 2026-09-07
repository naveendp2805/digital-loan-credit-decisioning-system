package com.naveen.notification_service.client;

import com.naveen.notification_service.dto.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service", url = "${services.loan.url}")
public interface LoanClient {

    @GetMapping("/api/loans/{id}")
    LoanResponse getLoanById(@PathVariable("id") Long id);
}
