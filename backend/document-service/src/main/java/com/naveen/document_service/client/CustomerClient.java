package com.naveen.document_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${services.customer.url}")
public interface CustomerClient {

    @GetMapping("/api/customers/{customerId}")
    Object getCustomerById(@PathVariable Long customerId);
}
