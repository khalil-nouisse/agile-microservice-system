package com.example.api_gateway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApiGatewayApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.api_gateway.ApiGatewayApplication"));
	}

}
