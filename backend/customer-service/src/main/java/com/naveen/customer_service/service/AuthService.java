package com.naveen.customer_service.service;

import com.naveen.customer_service.dto.*;
import com.naveen.customer_service.entity.Customer;
import com.naveen.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    public LoginResponse login(LoginRequest request) {

        Customer customer = customerRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String email = customer.getEmail();
        String role = customer.getRole().name();

        String accessToken = jwtService.generateToken(email, role);
        String refreshToken = refreshTokenService.createRefreshToken();

        refreshTokenService.storeRefreshToken(refreshToken, email, role);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshTokenService.getExpirationSeconds())
                .build();
    }

    public LoginResponse refresh(RefreshTokenRequest request) {

        String oldRefreshToken = request.getRefreshToken();

        String tokenData = refreshTokenService.getRefreshTokenData(oldRefreshToken);

        if (tokenData == null) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String[] parts = tokenData.split("\\|", 2);

        if (parts.length != 2) {
            refreshTokenService.deleteRefreshToken(oldRefreshToken);

            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = parts[0];
        String role = parts[1];

        refreshTokenService.deleteRefreshToken(oldRefreshToken);

        String newAccessToken = jwtService.generateToken(email, role);
        String newRefreshToken = refreshTokenService.createRefreshToken();

        refreshTokenService.storeRefreshToken(
                newRefreshToken,
                email,
                role
        );


        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(newRefreshToken)
                .refreshExpiresIn(refreshTokenService.getExpirationSeconds())
                .build();
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not Found!"));

        return CustomerMapper.toDto(customer);
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }
}
