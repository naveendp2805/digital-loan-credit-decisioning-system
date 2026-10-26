
package com.naveen.notification_service;

import com.naveen.notification_service.client.CustomerClient;
import com.naveen.notification_service.client.LoanClient;
import com.naveen.notification_service.dto.LoanResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class NotificationServiceApplicationTests {

	@Autowired
	private CustomerClient customerClient;

	@Autowired
	private LoanClient loanClient;

	@Test
	void notificationServiceCanReadCustomerAndLoan() {
		Object customer = customerClient.getCustomerById(2L);
		assertNotNull(customer, "Customer Service returned no customer");

		LoanResponse loan = loanClient.getLoanById(7L);
		assertNotNull(loan, "Loan Service returned no loan");
		assertEquals(2L, loan.customerId(),
				"Loan 7 should belong to customer 2");
	}
}
