package com.naveen.customer_service.service;

import com.naveen.customer_service.dto.LoginRequest;
import com.naveen.customer_service.dto.LoginResponse;
import com.naveen.customer_service.entity.Customer;
import com.naveen.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash()))
            throw new RuntimeException("Invalid email or password");

        String token = jwtService.generateToken(customer.getEmail(), customer.getRole().name());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(900)
                .build();

    }
}
