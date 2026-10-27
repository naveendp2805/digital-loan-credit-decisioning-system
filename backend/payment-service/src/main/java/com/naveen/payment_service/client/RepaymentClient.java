package com.naveen.payment_service.client;

import com.naveen.payment_service.dto.RepaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "repayment-service",
        url = "${services.repayment.url}"
)
public interface RepaymentClient {

    @GetMapping("/api/repayments/{repaymentId}")
    RepaymentResponse getRepaymentById(
            @PathVariable("repaymentId") Long repaymentId
    );
}