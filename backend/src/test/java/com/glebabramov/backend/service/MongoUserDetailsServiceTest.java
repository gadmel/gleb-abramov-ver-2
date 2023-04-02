package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.MongoUserAuthRequest;
import com.glebabramov.backend.model.MongoUserResponse;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class MongoUserDetailsServiceTest {

	@Autowired
	MongoUserRepository mongoUserRepository;
	@Autowired
	ResumeRepository resumeRepository;
	@Autowired
	AuthorisationService authorisationService;
	@Autowired
	ValidationService validationService;
	@Autowired
	VerificationService verificationService;
	IdService idService = mock(IdService.class);
	PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

	@Autowired
	MongoUserDetailsService mongoUserDetailsService;

	Principal mockedPrincipal = mock(Principal.class);

	MongoUser adminUser = new MongoUser("Some ID", "Admin's name", "Test password", "ADMIN", "[]");
	MongoUser basicUser = new MongoUser("Some other ID", "Basic user's name", "Test password", "BASIC", "[]");

	MongoUserResponse responseDTO = new MongoUserResponse(adminUser.id(), adminUser.username(), adminUser.role(), adminUser.associatedResume());
	MongoUserAuthRequest requestDTO = new MongoUserAuthRequest(adminUser.username(), adminUser.password());
	MongoUserAuthRequest basicAuthRequestDTO = new MongoUserAuthRequest(basicUser.username(), basicUser.password());


	ResponseStatusException unauthorisedUserException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	UsernameNotFoundException usernameNotFoundException = new UsernameNotFoundException("User not found");
	ResponseStatusException idIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
	ResponseStatusException usernameIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
	ResponseStatusException passwordIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
	ResponseStatusException userAlreadyExistsException = new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
	ResponseStatusException forbiddenToRegisterException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to register users");
	ResponseStatusException forbiddenToGetAllUsersException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view all users");
	ResponseStatusException forbiddenToDeleteUserException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to delete users");
	ResponseStatusException userDoesNotExistException = new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");


	@BeforeEach
	void setUp() {
		mongoUserDetailsService = new MongoUserDetailsService(mongoUserRepository, idService, passwordEncoder, validationService, verificationService);
		mongoUserRepository.findByUsername(adminUser.username()).ifPresent(mongoUserRepository::delete);
		mongoUserRepository.findByUsername(basicUser.username()).ifPresent(mongoUserRepository::delete);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
		when(idService.generateId()).thenReturn(adminUser.id());
	}

	@Nested
	@DisplayName("loadUserByUsername()")
	class loadUserByUsername {

		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the user with the given username if user exists")
		void loadUserByUsername_shouldReturnMongoUserResponseDTO_ifUserExists() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			GrantedAuthority grantedAuthority = () -> "ROLE_" + adminUser.role();
			Collection<GrantedAuthority> mongoUserAuthorities = new ArrayList<>(Arrays.asList(grantedAuthority));
			//WHEN
			UserDetails expected = new User(adminUser.username(), adminUser.password(), mongoUserAuthorities);
			UserDetails actual = mongoUserDetailsService.loadUserByUsername(adminUser.username());
			//THEN
			assertEquals(expected, actual);
		}


		@Test
		@DirtiesContext
		@DisplayName("...should throw a UsernameNotFoundException if user does not exist")
		void loadUserByUsername_shouldThrowUsernameNotFoundException_ifUserDoesNotExist() {
			//WHEN
			UsernameNotFoundException expected = usernameNotFoundException;
			UsernameNotFoundException actual = assertThrows(expected.getClass(), () -> mongoUserDetailsService.loadUserByUsername(adminUser.username()));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}
	}

	@Nested
	@DisplayName("getCurrentUser()")
	class getCurrentUser {
		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the current user if user exists and is logged in")
		void getCurrentUser_shouldReturnMongoUserResponseDTO_ifUserExistsAndIsLoggedIn() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			MongoUserResponse expected = responseDTO;
			MongoUserResponse actual = mongoUserDetailsService.getCurrentUser(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if current user does not exist or is not logged in")
		void getCurrentUser_shouldThrowUnauthorised_ifUserDoesNotExist_unauthenticated() {
			//GIVEN
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.getCurrentUser(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}
	}

	@Nested
	@DisplayName("register() user")
	class register {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if user is not logged in")
		void register_shouldThrowUnauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			when(mockedPrincipal.getName()).thenReturn(null);
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void register_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToRegisterException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(basicAuthRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if username is empty or null")
		void register_shouldThrowBadRequest400_ifUsernameIsEmptyOrNull() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			MongoUserAuthRequest requestDTOWithoutName = new MongoUserAuthRequest("", "password");
			MongoUserAuthRequest requestDTOWithNameNull = new MongoUserAuthRequest(null, "password");
			//WHEN
			ResponseStatusException expected = usernameIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTOWithoutName, mockedPrincipal));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTOWithNameNull, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if password is empty or null")
		void register_shouldThrowBadRequest400_ifPasswordIsEmptyOrNull() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			MongoUserAuthRequest requestDTOWithoutPassword = new MongoUserAuthRequest("username", "");
			MongoUserAuthRequest requestDTOWithPasswordNull = new MongoUserAuthRequest("username", null);
			//WHEN
			ResponseStatusException expected = passwordIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTOWithoutPassword, mockedPrincipal));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTOWithPasswordNull, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Conflict' (409) if the username is taken")
		void register_shouldThrowConflict409_ifUsernameIsTaken() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			ResponseStatusException expected = userAlreadyExistsException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(requestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the created user if username does not exist and provided username amd password are not empty")
		void register_shouldReturnMongoUserResponseDTOAndCreated201_ifUsernameDoesNotExistAndProvidedUsernameAndPasswordAreNotEmpty() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			MongoUserAuthRequest requestDTOBasicUser = new MongoUserAuthRequest(basicUser.username(), basicUser.password());
			MongoUserResponse responseDTOBasicUser = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), basicUser.associatedResume());
			when(idService.generateId()).thenReturn(basicUser.id());
			//WHEN
			MongoUserResponse expected = responseDTOBasicUser;
			MongoUserResponse actual = mongoUserDetailsService.register(requestDTOBasicUser, mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}

	}

	@Nested
	@DisplayName("getAllUsers()")
	class getAllUsers {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if current user is not logged in")
		void getAllUsers_shouldThrowUnauthorised_ifUserIsNotAuthenticated() {
			//GIVEN
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.getAllUsers(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void getAllUsers_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToGetAllUsersException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.getAllUsers(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return a list of all users if the user is an admin")
		void getAllUsers_shouldReturnListOfAllUsers_ifUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			MongoUserResponse basicResponseDTO = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), basicUser.associatedResume());
			List<MongoUserResponse> expected = Arrays.asList(responseDTO, basicResponseDTO);
			//WHEN
			List<MongoUserResponse> actual = mongoUserDetailsService.getAllUsers(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}
	}


	@Nested
	@DisplayName("delete() user")
	class delete {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorized' (401) if the user is not logged in")
		void deleteUser_shouldThrowUnauthorized401_ifUserIsNotLoggedIn() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			String validId = basicUser.id();
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(validId, null));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void deleteUser_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			String validId = basicUser.id();
			//WHEN
			ResponseStatusException expected = forbiddenToDeleteUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(validId, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should delete the user if the user is an admin")
		void deleteUser_shouldDeleteUser_ifUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			//WHEN
			mongoUserDetailsService.delete(basicUser.id(), mockedPrincipal);
			//THEN
			assertFalse(mongoUserRepository.existsById(basicUser.id()));
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Not Found' (404) if the user does not exist")
		void deleteUser_shouldThrowNotFound404_ifUserDoesNotExist() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			String invalidId = "invalidId";
			//WHEN
			ResponseStatusException expected = userDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(invalidId, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if the user id is invalid")
		void deleteUser_shouldThrowBadRequest400_ifUserIdIsInvalid() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			ResponseStatusException expected = idIsRequiredException;
			ResponseStatusException actualWithoutId = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete("", mockedPrincipal));
			ResponseStatusException actualWithIdNull = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(null, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actualWithoutId.getClass());
			assertEquals(expected.getMessage(), actualWithoutId.getMessage());
			assertEquals(expected.getClass(), actualWithIdNull.getClass());
			assertEquals(expected.getMessage(), actualWithIdNull.getMessage());
		}
	}
}
