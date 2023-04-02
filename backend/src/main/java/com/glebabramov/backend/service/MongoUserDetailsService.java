package com.glebabramov.backend.service;

import com.glebabramov.backend.model.*;
import com.glebabramov.backend.repository.MongoUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoUserDetailsService implements UserDetailsService {
	private final MongoUserRepository repository;
	private final IdService idService;
	private final PasswordEncoder passwordEncoder;
	private final AuthorisationService authorisationService = new AuthorisationService(this);
	private final ValidationService validationService;
	private final VerificationService verificationService;
	private static final String ADMIN_ROLE = "ADMIN";
	UsernameNotFoundException userNotFoundException = new UsernameNotFoundException("User not found");

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MongoUser mongoUser = repository.findByUsername(username)
				.orElseThrow(() -> userNotFoundException);

		return new User(mongoUser.username(), mongoUser.password(),
				List.of(new SimpleGrantedAuthority("ROLE_" + mongoUser.role())));
	}

	public MongoUserResponse getCurrentUser(Principal principal) {
		MongoUser user = repository.findByUsername(principal.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in"));

		return new MongoUserResponse(user.id(), user.username(), user.role(), user.associatedResume());
	}

	public List<MongoUserResponse> getAllUsers(Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "view all users");
		return repository.findAll().stream()
				.map(user -> new MongoUserResponse(user.id(), user.username(), user.role(), user.associatedResume()))
				.toList();
	}

	public MongoUserResponse register(MongoUserAuthRequest user, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "register users");
		validationService.validateMongoUserAuthRequest(user);
		verificationService.userDoesNotExistByUsername(user.username());

		MongoUser newUser = new MongoUser(idService.generateId(), user.username(), passwordEncoder.encode(user.password()), "BASIC", "[]");
		MongoUser savedUser = repository.save(newUser);

		return new MongoUserResponse(savedUser.id(), savedUser.username(), savedUser.role(), savedUser.associatedResume());
	}

	public MongoUserResponse delete(String id, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, principal, "delete users");
		validationService.validateIdRequest(id);
		MongoUser userToDelete = verificationService.userDoesExistById(id);

		repository.deleteById(id);
		return new MongoUserResponse(userToDelete.id(), userToDelete.username(), userToDelete.role(), userToDelete.associatedResume());
	}

}
