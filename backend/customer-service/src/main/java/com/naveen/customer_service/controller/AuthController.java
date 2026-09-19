package com.naveen.customer_service.controller;

import com.naveen.customer_service.dto.CustomerResponse;
import com.naveen.customer_service.dto.LoginRequest;
import com.naveen.customer_service.dto.LoginResponse;
import com.naveen.customer_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCustomerByEmail(authentication.getName()));
    }
}
