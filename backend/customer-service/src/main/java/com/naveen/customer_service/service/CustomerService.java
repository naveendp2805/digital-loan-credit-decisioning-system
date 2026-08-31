package com.naveen.customer_service.service;

import com.naveen.customer_service.dto.CustomerMapper;
import com.naveen.customer_service.dto.CustomerRequest;
import com.naveen.customer_service.dto.CustomerResponse;
import com.naveen.customer_service.entity.Customer;
import com.naveen.customer_service.exception.CustomerNotFoundException;
import com.naveen.customer_service.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Customer already exists with email: " + request.getEmail());

        if (customerRepository.existsByPhone(request.getPhone()))
            throw new IllegalArgumentException("Customer already exists with phone: " + request.getPhone());

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .monthlyIncome(request.getMonthlyIncome())
                .employmentType(request.getEmploymentType())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerMapper.toDto(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerMapper.toDto(customer);
    }

    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::toDto)
                .toList();
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (!customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Customer already exists with email: " + request.getEmail());

        if (!customer.getPhone().equals(request.getPhone()) && customerRepository.existsByPhone(request.getPhone()))
            throw new IllegalArgumentException("Customer already exists with phone: " + request.getPhone());

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setEmploymentType(request.getEmploymentType());

        Customer updatedCustomer = customerRepository.save(customer);

        return CustomerMapper.toDto(updatedCustomer);
    }

    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(customer);
    }
}
