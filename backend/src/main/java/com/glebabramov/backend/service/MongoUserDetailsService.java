package com.glebabramov.backend.service;

import com.glebabramov.backend.model.*;
import com.glebabramov.backend.repository.MongoUserRepository;

import com.glebabramov.backend.repository.ResumeRepository;
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
	private final ResumeRepository resumeRepository;
	private final IdService idService;
	private final PasswordEncoder passwordEncoder;
	private final AuthorisationService authorisationService = new AuthorisationService(this);
	private final ValidationService validationService;
	private final VerificationService verificationService;
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String USER_ROLE = "BASIC";
	private static final String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";
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
		return user.toResponseDTO();
	}

	public List<MongoUserResponse> getAllUsers(Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "view all users",  principal);
		return repository.findAll().stream()
				.map(MongoUser::toResponseDTO)
				.toList();
	}

	public MongoUserResponse register(MongoUserAuthRequest user, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "register users",  principal);
		validationService.validateMongoUserAuthRequest(user);
		verificationService.userDoesNotExistByUsername(user.username());

		MongoUser newUser = new MongoUser(idService.generateId(), user.username(), passwordEncoder.encode(user.password()), USER_ROLE, STANDARD_RESUME_ID);
		Resume standardResume = verificationService.resumeDoesExistById(STANDARD_RESUME_ID, true);

		resumeRepository.save(standardResume.assignToUser(newUser.id()));
		MongoUser savedUser = repository.save(newUser);
		return savedUser.toResponseDTO();
	}

	public MongoUserResponse update(MongoUserRequest incomingUser, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "update users",  principal);
		validationService.validateMongoUserRequest(incomingUser);
		MongoUser userToUpdate = verificationService.userDoesExistById(incomingUser.id());
		Resume associatedResume = verificationService.resumeDoesExistById(incomingUser.associatedResume(), true);
		Resume previousAssociatedResume = verificationService.resumeDoesExistById(userToUpdate.associatedResume(), true);

		resumeRepository.save(associatedResume.assignToUser(userToUpdate.id()));
		resumeRepository.save(previousAssociatedResume.unassignFromUser(userToUpdate.id()));

		MongoUser savedUser = repository.save(userToUpdate.updateWithRequestDTO(incomingUser));
		return savedUser.toResponseDTO();
	}

	public MongoUserResponse delete(String id, Principal principal) {
		authorisationService.isAuthorisedByRole(ADMIN_ROLE, "delete users",  principal);
		validationService.validateIdRequest(id);
		MongoUser userToDelete = verificationService.userDoesExistById(id);
		verificationService.userMayBeDeleted(userToDelete);
		Resume associatedResume = verificationService.resumeDoesExistById(userToDelete.associatedResume(), true);

		resumeRepository.save(associatedResume.unassignFromUser(userToDelete.id()));
		repository.deleteById(id);
		return userToDelete.toResponseDTO();
	}

}
