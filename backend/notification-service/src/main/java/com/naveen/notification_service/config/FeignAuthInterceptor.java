
package com.naveen.notification_service.config;

import com.naveen.notification_service.security.ServiceTokenProvider;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignAuthInterceptor {

    private final ServiceTokenProvider serviceTokenProvider;

    @Bean
    public RequestInterceptor notificationServiceRequestInterceptor() {
        return requestTemplate ->
                requestTemplate.header(
                        "Authorization",
                        "Bearer " + serviceTokenProvider.createToken()
                );
    }
}
