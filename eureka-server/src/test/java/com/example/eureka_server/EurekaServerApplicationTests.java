package com.example.eureka_server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EurekaServerApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.eureka_server.EurekaServerApplication"));
	}

}
