package com.glebabramov.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HelloWorldControllerTest {

	@Nested
	@DisplayName("GET /api/hello/")
	class testHelloWorld {
		@Test
		@DisplayName("...should return 200 a string 'Hello, world. It's me again - Gleb!'")
		void hello_shouldReturn200AndString() {
			HelloWorldController helloWorldController = new HelloWorldController();
			assertEquals("Hello, world. It's me again - Gleb!", helloWorldController.hello());
		}
	}
}