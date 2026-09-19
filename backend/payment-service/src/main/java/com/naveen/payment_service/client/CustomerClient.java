package com.naveen.payment_service.client;

import com.naveen.payment_service.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${services.customer.url}")
public interface CustomerClient {

    @GetMapping("/api/customers/internal/{customerId}")
    CustomerResponse getCustomerById(@PathVariable("customerId") Long customerId);
}
