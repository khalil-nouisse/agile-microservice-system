package com.example.notification_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.notification_service.NotificationServiceApplication"));
	}

}
