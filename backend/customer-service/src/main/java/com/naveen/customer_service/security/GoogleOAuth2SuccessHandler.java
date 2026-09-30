package com.naveen.customer_service.security;

import com.naveen.customer_service.dto.LoginResponse;
import com.naveen.customer_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        LoginResponse loginResponse = authService.loginWithGoogle(oauth2User);

        response.setContentType("application/json");

        response.getWriter().write(
                """
                {
                    "message": "Google login successful",
                    "accessToken": "%s",
                    "refreshToken": "%s",
                    "expiresIn": %d,
                    "refreshExpiresIn": %d
                }
                """.formatted(
                        loginResponse.getAccessToken(),
                        loginResponse.getRefreshToken(),
                        loginResponse.getExpiresIn(),
                        loginResponse.getRefreshExpiresIn()
                )
        );
    }
}
