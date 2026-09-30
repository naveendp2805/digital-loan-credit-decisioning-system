package com.naveen.customer_service.service;

import com.naveen.customer_service.dto.*;
import com.naveen.customer_service.entity.AuthProvider;
import com.naveen.customer_service.entity.Customer;
import com.naveen.customer_service.entity.Role;
import com.naveen.customer_service.exception.CustomerNotFoundException;
import com.naveen.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public LoginResponse loginWithGoogle(OAuth2User oauth2User) {

        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("firstName");
        String lastName = oauth2User.getAttribute("lastName");
        String providerId = oauth2User.getAttribute("sub");

        Boolean emailVerified = oauth2User.getAttribute("email_verified");

        if(email == null || providerId == null)
            throw new RuntimeException("Google account information is incomplete");

        if(!Boolean.TRUE.equals(emailVerified))
            throw new RuntimeException("Google email is not verified");

        Customer customer = customerRepository.findByEmail(email)
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .passwordHash(null)
                            .role(Role.USER)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .build();

                    return customerRepository.save(newCustomer);
                });

        if(customer.getAuthProvider() == AuthProvider.LOCAL)
            throw new RuntimeException("An account already exists with this email. " + "Please login with your password.");

        if (!providerId.equals(customer.getProviderId()))
            throw new RuntimeException("Google account identity mismatch");

        String accessToken = jwtService.generateToken(email, customer.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken();

        refreshTokenService.storeRefreshToken(refreshToken,customer.getEmail(), customer.getRole().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshTokenService.getExpirationSeconds())
                .build();
    }

    @Transactional
    public LoginResponse register(RegisterRequest request)
    {
        String email = request.getEmail();

        if(customerRepository.findByEmail(email).isPresent())
            throw new RuntimeException("An account already exists with this email");

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .providerId(null)
                .phone(request.getPhone())
                .build();

        Customer saved =
                customerRepository.save(customer);

        String accessToken =
                jwtService.generateToken(
                        saved.getEmail(),
                        saved.getRole().name()
                );

        String refreshToken =
                refreshTokenService.createRefreshToken();

        refreshTokenService.storeRefreshToken(
                refreshToken,
                saved.getEmail(),
                saved.getRole().name()
        );

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
