package com.naveem.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"JWT_SECRET=test-only-secret-for-context-loading-123456789"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}
}