package com.glebabramov.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class BackendApplication {

	@RestController
	class HelloworldController {
		@GetMapping("/api/")
		String hello() {
			return "Hello, world. It's me again, Gleb!";
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
