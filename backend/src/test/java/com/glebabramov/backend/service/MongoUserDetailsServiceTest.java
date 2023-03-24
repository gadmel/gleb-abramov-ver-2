package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.MongoUserRequest;
import com.glebabramov.backend.model.MongoUserResponse;
import com.glebabramov.backend.repository.MongoUserRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class MongoUserDetailsServiceTest {

	@Autowired
	MongoUserRepository mongoUserRepository;
	IdService idService = mock(IdService.class);
	PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

	@Autowired
	MongoUserDetailsService mongoUserDetailsService;

	Principal mockedPrincipal = mock(Principal.class);

	MongoUser adminUser = new MongoUser("Some ID", "Admin's name", "Test password", "ADMIN", "[]");
	MongoUser basicUser = new MongoUser("Some ID", "Basic user's name", "Test password", "BASIC", "[]");

	MongoUserResponse responseDTO = new MongoUserResponse(adminUser.id(), adminUser.username(), adminUser.role(), adminUser.associatedResume());
	MongoUserRequest requestDTO = new MongoUserRequest(adminUser.username(), adminUser.password());
	MongoUserRequest basicRequestDTO = new MongoUserRequest(basicUser.username(), basicUser.password());

	ResponseStatusException userNotFoundException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
	UsernameNotFoundException usernameNotFoundException = new UsernameNotFoundException("User not found");
	ResponseStatusException usernameIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
	ResponseStatusException passwordIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
	ResponseStatusException userAlreadyExistsException = new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
	ResponseStatusException forbiddenException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to register new users");


	@BeforeEach
	void setUp() {
		mongoUserDetailsService = new MongoUserDetailsService(mongoUserRepository, idService, passwordEncoder);
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
			ResponseStatusException expected = userNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.getCurrentUser(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}
	}

	@Nested
	@DisplayName("register()")
	class register {
		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the created user if username does not exist and provided username amd password are not empty")
		void register_shouldReturnMongoUserResponseDTOAndCreated201_ifUsernameDoesNotExistAndProvidedUsernameAndPasswordAreNotEmpty() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			MongoUserRequest requestDTOBasicUser = new MongoUserRequest(basicUser.username(), basicUser.password());
			MongoUserResponse responseDTOBasicUser = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), basicUser.associatedResume());
			//WHEN
			MongoUserResponse expected = responseDTOBasicUser;
			MongoUserResponse actual = mongoUserDetailsService.register(requestDTOBasicUser, mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if username is empty or null")
		void register_shouldThrowBadRequest400_ifUsernameIsEmptyOrNull() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			MongoUserRequest requestDTOWithoutName = new MongoUserRequest("", "password");
			MongoUserRequest requestDTOWithNameNull = new MongoUserRequest(null, "password");
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
			MongoUserRequest requestDTOWithoutPassword = new MongoUserRequest("username", "");
			MongoUserRequest requestDTOWithPasswordNull = new MongoUserRequest("username", null);
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
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void register_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(basicRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

	}

}
