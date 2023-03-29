package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.MongoUserAuthRequest;
import com.glebabramov.backend.model.MongoUserResponse;
import com.glebabramov.backend.service.MongoUserDetailsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MongoUserController {
	private final MongoUserDetailsService mongoUserDetailsService;

	@GetMapping("/users/current/")
	public MongoUserResponse getCurrentUser(Principal principal) {
		return mongoUserDetailsService.getCurrentUser(principal);
	}

	@PostMapping("/users/login/")
	public MongoUserResponse login(Principal principal) {
		return mongoUserDetailsService.getCurrentUser(principal);
	}

	@GetMapping("/admin/users/")
	public List<MongoUserResponse> getAllUsers(Principal principal)  {
		return mongoUserDetailsService.getAllUsers(principal);
	}

	@PostMapping("/admin/users/register/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public MongoUserResponse register(@RequestBody MongoUserAuthRequest user, Principal principal) {
		return mongoUserDetailsService.register(user, principal);
	}

	@DeleteMapping("/admin/users/delete/{id}/")
	public MongoUserResponse delete(@PathVariable String id, Principal principal) {
		return mongoUserDetailsService.deleteUser(id, principal);
	}

}
