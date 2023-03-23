package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.MongoUserRequest;
import com.glebabramov.backend.model.MongoUserResponse;
import com.glebabramov.backend.service.MongoUserDetailsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MongoUserController {
	private final MongoUserDetailsService mongoUserDetailsService;

	@GetMapping("/current/")
	public MongoUserResponse getCurrentUser(Principal principal) {
		return mongoUserDetailsService.getCurrentUser(principal);
	}

	@PostMapping("/login/")
	public MongoUserResponse login(Principal principal) {
		return mongoUserDetailsService.getCurrentUser(principal);
	}

	@PostMapping("/logout/")
	public void logout() {
		// logout is handled by Spring Security
	}

	@PostMapping("/register/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public MongoUserResponse register(@RequestBody MongoUserRequest user, Principal principal) {
		return mongoUserDetailsService.register(user, principal);
	}

}
