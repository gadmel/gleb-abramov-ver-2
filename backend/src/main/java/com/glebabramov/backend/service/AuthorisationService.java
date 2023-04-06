package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class AuthorisationService {

	private final MongoUserDetailsService mongoUserDetailsService;

	public boolean isAuthorised(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
		}
		return true;
	}

	public boolean isAuthorisedByRole(String role, String authorisedTo, Principal principal) {
		isAuthorised(principal);
		MongoUserResponse currentUser = mongoUserDetailsService.getCurrentUser(principal);
		if (!currentUser.role().equals(role)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to " + authorisedTo);
		}
		return true;
	}

}
