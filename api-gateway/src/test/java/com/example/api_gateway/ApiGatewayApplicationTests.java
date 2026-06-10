package com.example.api_gateway;

import org.junit.jupiter.api.Test;

<<<<<<< HEAD
@SpringBootTest(properties = {
		"jwt.secret=rdfko93M8NvW70IhIibp4MqryE6qciSbUCPGlP4JtIfX7wEBEbG7rRQKSN1yj9nT",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
})
=======
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

>>>>>>> f2a47b675b99bfa9d348dd883d616e779c379039
class ApiGatewayApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.api_gateway.ApiGatewayApplication"));
	}

}
