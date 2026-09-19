package com.naveen.repayment_service.client;

import com.naveen.repayment_service.dto.PaymentOrderRequest;
import com.naveen.repayment_service.dto.PaymentOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${services.payment.url}")
public interface PaymentClient {

    @PostMapping("/api/payments/orders")
    PaymentOrderResponse createPayment(@RequestBody PaymentOrderRequest request);
}
