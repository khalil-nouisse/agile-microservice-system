package com.example.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=rdfko93M8NvW70IhIibp4MqryE6qciSbUCPGlP4JtIfX7wEBEbG7rRQKSN1yj9nT",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
