package com.example.sprint_service;

import org.junit.jupiter.api.Test;

<<<<<<< HEAD
@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:sprint_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.kafka.listener.auto-startup=false"
})
=======
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

>>>>>>> f2a47b675b99bfa9d348dd883d616e779c379039
class SprintServiceApplicationTests {

	@Test
	void applicationClassIsAvailable() {
		assertDoesNotThrow(() -> Class.forName("com.example.sprint_service.SprintServiceApplication"));
	}

}
