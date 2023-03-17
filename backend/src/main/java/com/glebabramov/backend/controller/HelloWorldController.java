package com.glebabramov.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class HelloWorldController {
	@GetMapping("/hello/")
	String hello() {
		return "Hello, world. It's me again - Gleb!";
	}
}
