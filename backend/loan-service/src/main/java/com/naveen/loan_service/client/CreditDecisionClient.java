package com.naveen.loan_service.client;

import com.naveen.loan_service.dto.CreditDecisionRequest;
import com.naveen.loan_service.dto.CreditDecisionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "credit-decision-service", url = "${credit-decision-service.url}")
public interface CreditDecisionClient {

    @PostMapping("/api/credit-decisions")
    CreditDecisionResponse makeDecision(@RequestBody CreditDecisionRequest request);
}
