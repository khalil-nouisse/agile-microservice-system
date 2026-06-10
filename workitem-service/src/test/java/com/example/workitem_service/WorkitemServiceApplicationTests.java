package com.example.workitem_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class WorkitemServiceApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.workitem_service.WorkitemServiceApplication"));
	}

}
