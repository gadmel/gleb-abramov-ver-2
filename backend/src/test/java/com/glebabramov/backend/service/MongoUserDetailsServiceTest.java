package com.glebabramov.backend.service;

import com.glebabramov.backend.model.*;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoUserDetailsServiceTest {

	MongoUserRepository mongoUserRepository = mock(MongoUserRepository.class);
	ResumeRepository resumeRepository = mock(ResumeRepository.class);
	IdService idService = mock(IdService.class);
	PasswordEncoder passwordEncoder = mock(Argon2PasswordEncoder.class);
	Principal mockedPrincipal = mock(Principal.class);
	MongoUserDetailsService mongoUserDetailsService;

	String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";
	String NON_EXISTENT_RESUME_ID = "Some-non-existent-resume-id";
	String ADMIN_ID = "Some-ID";
	String BASIC_ID = "Some-other-ID";
	Resume standardResume = new Resume(STANDARD_RESUME_ID, "Standard resume", Set.of(ADMIN_ID, BASIC_ID), false, false);
	Resume otherResume = new Resume("Some-other-resumes-ID", "Another resume", Set.of(), false, false);
	MongoUser adminUser = new MongoUser(ADMIN_ID, "Admin's name", "Test password", "ADMIN", STANDARD_RESUME_ID);
	MongoUser basicUser = new MongoUser(BASIC_ID, "Basic user's name", "Test password", "BASIC", STANDARD_RESUME_ID);
	MongoUser corruptedUserWithNonExistentResume = new MongoUser("Another-ID", "Basic user's name", "Test password", "BASIC", NON_EXISTENT_RESUME_ID);
	MongoUserAuthRequest basicAuthRequestDTO = new MongoUserAuthRequest(basicUser.username(), basicUser.password());
	MongoUserRequest basicRequestDTO = new MongoUserRequest(basicUser.id(), basicUser.username(), basicUser.associatedResume());
	MongoUserRequest corruptedUserRequestDTO = new MongoUserRequest(corruptedUserWithNonExistentResume.id(), corruptedUserWithNonExistentResume.username(), corruptedUserWithNonExistentResume.associatedResume());
	MongoUserResponse basicResponseDTO = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), basicUser.associatedResume());
	MongoUserResponse adminResponseDTO = new MongoUserResponse(adminUser.id(), adminUser.username(), adminUser.role(), adminUser.associatedResume());
	MongoUserResponse corruptedResponseDTO = new MongoUserResponse(corruptedUserWithNonExistentResume.id(), corruptedUserWithNonExistentResume.username(), corruptedUserWithNonExistentResume.role(), corruptedUserWithNonExistentResume.associatedResume());

	ResponseStatusException unauthorisedUserException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	UsernameNotFoundException usernameNotFoundException = new UsernameNotFoundException("User not found");
	ResponseStatusException idIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
	ResponseStatusException usernameIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
	ResponseStatusException passwordIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
	ResponseStatusException userAlreadyExistsException = new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
	ResponseStatusException forbiddenToRegisterException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to register users");
	ResponseStatusException forbiddenToGetAllUsersException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view all users");
	ResponseStatusException forbiddenToUpdateUserException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to update users");
	ResponseStatusException forbiddenToDeleteUserException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to delete users");
	ResponseStatusException forbiddenToDeleteAdminException = new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot be deleted");
	ResponseStatusException userDoesNotExistException = new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
	ResponseStatusException standardResumeDoesNotExistException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + STANDARD_RESUME_ID + " does not exist");
	ResponseStatusException associatedResumeDoesNotExistException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + NON_EXISTENT_RESUME_ID + " does not exist");
	ResponseStatusException invalidRequestIdException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
	ResponseStatusException invalidRequestUsernameException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
	ResponseStatusException invalidRequestAssociatedResumeException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Associated resume is required");

	@BeforeEach
	void setUp() {
		mongoUserDetailsService = new MongoUserDetailsService(mongoUserRepository, resumeRepository, idService, passwordEncoder);
		when(idService.generateId()).thenReturn(basicUser.id());
		when(mongoUserRepository.save(any(MongoUser.class))).thenAnswer(invocation -> {
			MongoUser savedUser = invocation.getArgument(0);
			when(mongoUserRepository.findById(savedUser.id())).thenReturn(Optional.of(savedUser));
			when(mongoUserRepository.existsByUsername(savedUser.username())).thenReturn(true);
			when(mongoUserRepository.findByUsername(savedUser.username())).thenReturn(Optional.of(savedUser));
			return new MongoUser(savedUser.id(), savedUser.username(), savedUser.password(), savedUser.role(), savedUser.associatedResume());
		});
		mongoUserRepository.save(adminUser);
		mongoUserRepository.save(basicUser);
		mongoUserRepository.save(corruptedUserWithNonExistentResume);
		when(mongoUserRepository.findAll()).thenReturn(List.of(adminUser, basicUser, corruptedUserWithNonExistentResume));
		when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
			Resume savedResume = invocation.getArgument(0);
			when(resumeRepository.findById(savedResume.id())).thenReturn(Optional.of(savedResume));
			return new Resume(savedResume.id(), savedResume.name(), savedResume.userIds(), savedResume.invitationSent(), savedResume.isPublished());
		});
		resumeRepository.save(standardResume);
		resumeRepository.save(otherResume);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
	}

	@Nested
	@DisplayName("loadUserByUsername()")
	class loadUserByUsername {

		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the user with the given username if user exists")
		void loadUserByUsername_shouldReturnMongoUserResponseDTO_ifUserExists() {
			//GIVEN
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
			//GIVEN
			when(mongoUserRepository.findByUsername(adminUser.username())).thenReturn(Optional.empty());
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
		@DisplayName("...should throw 'Unauthorised' (401) if current user does not exist or is not logged in")
		void getCurrentUser_shouldThrowUnauthorised_ifUserDoesNotExist_unauthenticated() {
			//GIVEN
			when(mockedPrincipal.getName()).thenReturn(null);
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.getCurrentUser(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the current user if user exists and is logged in")
		void getCurrentUser_shouldReturnMongoUserResponseDTO_ifUserExistsAndIsLoggedIn() {
			//WHEN
			MongoUserResponse expected = adminResponseDTO;
			MongoUserResponse actual = mongoUserDetailsService.getCurrentUser(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
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
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(basicAuthRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void register_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
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
			//WHEN
			ResponseStatusException expected = userAlreadyExistsException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(basicAuthRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the standard resume does not exist")
		void register_shouldThrowUnprocessableEntity422_ifStandardResumeDoesNotExist() {
			//GIVEN
			when(mongoUserRepository.existsByUsername(basicUser.username())).thenReturn(false);
			when(resumeRepository.findById(STANDARD_RESUME_ID)).thenReturn(Optional.empty());
			//WHEN
			ResponseStatusException expected = standardResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.register(basicAuthRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return a MongoUserResponseDTO of the created user if username does not exist and provided username and password are not empty")
		void register_shouldReturnMongoUserResponseDTOAndCreated201_ifUsernameDoesNotExistAndProvidedUsernameAndPasswordAreNotEmpty() {
			//GIVEN
			when(mongoUserRepository.existsByUsername(basicUser.username())).thenReturn(false);
			//WHEN
			MongoUserResponse expected = basicResponseDTO;
			MongoUserResponse actual = mongoUserDetailsService.register(basicAuthRequestDTO, mockedPrincipal);

			Set<String> expectedUserIds = Stream.concat(standardResume.userIds().stream(), Stream.of(basicUser.id())).collect(Collectors.toSet());
			Resume expectedSideEffect = new Resume(standardResume.id(), standardResume.name(), expectedUserIds, false, false);
			Resume actualSideEffect = resumeRepository.findById(STANDARD_RESUME_ID).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect, actualSideEffect);
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
			when(mockedPrincipal.getName()).thenReturn(null);
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
			List<MongoUserResponse> expected = Arrays.asList(adminResponseDTO, basicResponseDTO, corruptedResponseDTO);
			when(mockedPrincipal.getName()).thenReturn(adminUser.username());
			//WHEN
			List<MongoUserResponse> actual = mongoUserDetailsService.getAllUsers(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}
	}

	@Nested
	@DisplayName("update() user")
	class update {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void updateUser_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(basicRequestDTO, null));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void updateUser_shouldThrow403Forbidden_ifUserIsNotAdmin() {
			//GIVEN
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToUpdateUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(basicRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if the user's id, username or associated resume is missing or empty")
		void updateUser_shouldThrow400BadRequest_ifUpdateRequestIsInvalid() {
			//GIVEN
			MongoUserRequest requestDTOInvalidId1 = new MongoUserRequest(null, basicUser.username(), basicUser.associatedResume());
			MongoUserRequest requestDTOInvalidId2 = new MongoUserRequest("", basicUser.username(), basicUser.associatedResume());
			MongoUserRequest requestDTOInvalidUsername1 = new MongoUserRequest(basicUser.id(), null, basicUser.associatedResume());
			MongoUserRequest requestDTOInvalidUsername2 = new MongoUserRequest(basicUser.id(), "", basicUser.associatedResume());
			MongoUserRequest requestDTOInvalidResume1 = new MongoUserRequest(basicUser.id(), basicUser.username(), null);
			MongoUserRequest requestDTOInvalidResume2 = new MongoUserRequest(basicUser.id(), basicUser.username(), "");
			//WHEN
			ResponseStatusException expectedId = invalidRequestIdException;
			ResponseStatusException expectedUsername = invalidRequestUsernameException;
			ResponseStatusException expectedResume = invalidRequestAssociatedResumeException;
			ResponseStatusException actualId1 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidId1, mockedPrincipal));
			ResponseStatusException actualId2 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidId2, mockedPrincipal));
			ResponseStatusException actualUsername1 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidUsername1, mockedPrincipal));
			ResponseStatusException actualUsername2 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidUsername2, mockedPrincipal));
			ResponseStatusException actualResume1 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidResume1, mockedPrincipal));
			ResponseStatusException actualResume2 = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(requestDTOInvalidResume2, mockedPrincipal));
			//THEN
			assertEquals(expectedId.getClass(), actualId1.getClass());
			assertEquals(expectedId.getMessage(), actualId1.getMessage());
			assertEquals(expectedId.getClass(), actualId2.getClass());
			assertEquals(expectedId.getMessage(), actualId2.getMessage());
			assertEquals(expectedUsername.getClass(), actualUsername1.getClass());
			assertEquals(expectedUsername.getMessage(), actualUsername1.getMessage());
			assertEquals(expectedUsername.getClass(), actualUsername2.getClass());
			assertEquals(expectedUsername.getMessage(), actualUsername2.getMessage());
			assertEquals(expectedResume.getClass(), actualResume1.getClass());
			assertEquals(expectedResume.getMessage(), actualResume1.getMessage());
			assertEquals(expectedResume.getClass(), actualResume2.getClass());
			assertEquals(expectedResume.getMessage(), actualResume2.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Not Found' (404) if the user does not exist")
		void updateUser_shouldThrow404NotFound_ifUserDoesNotExist() {
			//GIVEN
			when(mongoUserRepository.findById(basicUser.id())).thenReturn(Optional.empty());
			//WHEN
			ResponseStatusException expected = userDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(basicRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "admin", roles = "ADMIN")
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user's to update new associated resume does not exist and thus cannot assigned to the user")
		void updateUser_shouldThrow422UnprocessableEntity_ifNewAssociatedResumeDoesNotExist() {
			//GIVEN
			when(resumeRepository.findById(standardResume.id())).thenReturn(Optional.empty());
			//WHEN
			ResponseStatusException expected = standardResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(basicRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "admin", roles = "ADMIN")
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user's to update previous associated resume does not exist and thus cannot be unassigned from the user")
			// Redundant: tests the behaviour, which cannot occur so long only the prescribed methods are used to manipulate data (data has not been corrupted through direct access to the database)
		void updateUser_shouldThrow422UnprocessableEntity_ifPreviousAssociatedResumeDoesNotExist() {
			//WHEN
			ResponseStatusException expected = associatedResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.update(corruptedUserRequestDTO, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "admin", roles = "ADMIN")
		@DisplayName("...should update the user, the user's associated resume and return the updated user if the user is an admin")
		void updateUser_shouldUpdateUserAndAssociatedResume_andReturnTheUpdatedUser_ifUserIsAdmin() {
			//GIVEN
			MongoUserRequest requestDTO = new MongoUserRequest(basicUser.id(), "Altered username", otherResume.id());
			//WHEN
			MongoUserResponse expected = new MongoUserResponse(basicUser.id(), "Altered username", basicUser.role(), otherResume.id());
			MongoUserResponse actual = mongoUserDetailsService.update(requestDTO, mockedPrincipal);
			MongoUser expectedEffect = new MongoUser(basicUser.id(), "Altered username", basicUser.password(), basicUser.role(), otherResume.id());
			MongoUser actualEffect = mongoUserRepository.findById(basicUser.id()).get();
			Set<String> expectedOtherResumesUserIds = Stream.concat(otherResume.userIds().stream(), Stream.of(basicUser.id())).collect(Collectors.toSet());
			Resume expectedSideEffect1 = new Resume(otherResume.id(), otherResume.name(), expectedOtherResumesUserIds, otherResume.invitationSent(), otherResume.isPublished());
			Resume actualSideEffect1 = resumeRepository.findById(otherResume.id()).get();
			Set<String> expectedStandardResumesUserIds = standardResume.userIds().stream().filter(id -> !id.equals(basicUser.id())).collect(Collectors.toSet());
			Resume expectedSideEffect2 = new Resume(standardResume.id(), standardResume.name(), expectedStandardResumesUserIds, standardResume.invitationSent(), standardResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(standardResume.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedEffect, actualEffect);
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
		}

	}

	@Nested
	@DisplayName("delete() user")
	class delete {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorized' (401) if the user is not logged in")
		void deleteUser_shouldThrowUnauthorized401_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(BASIC_ID, null));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void deleteUser_shouldThrowForbidden403_ifUserIsNotAdmin() {
			//GIVEN
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToDeleteUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(BASIC_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Bad Request' (400) if the user's to delete id is invalid")
		void deleteUser_shouldThrowBadRequest400_ifUserIdIsInvalid() {
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

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Not Found' (404) if the user to delete does not exist")
		void deleteUser_shouldThrowNotFound404_ifUserDoesNotExist() {
			//WHEN
			ResponseStatusException expected = userDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete("invalidId", mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is an admin but the user to delete is an admin")
		void deleteUser_shouldThrowForbidden403_ifUserToDeleteIsAdmin() {
			//WHEN
			ResponseStatusException expected = forbiddenToDeleteAdminException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(ADMIN_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user to delete exists but its associated resume does not exist")
		void deleteUser_shouldThrowUnprocessableEntity422_ifUserDoesExistAndAssociatedResumeDoesNotExist() {
			// GIVEN
			when(resumeRepository.findById(standardResume.id())).thenReturn(Optional.empty());
			//WHEN
			ResponseStatusException expected = standardResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> mongoUserDetailsService.delete(BASIC_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should delete the user, unassign him from his associated resume's user IDs and return the deleted user if the user is an admin, the user to delete exists and is not an admin and its associated resume exists")
		void deleteUser_shouldDeleteUserAndUnassignHimFromHisResume_andReturnDeletedUser_ifUserDoesExistAndIsNotAdminAndAssociatedResumeExists() {
			//WHEN
			MongoUserResponse expected = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), standardResume.id());
			MongoUserResponse actual = mongoUserDetailsService.delete(basicUser.id(), mockedPrincipal);
			Set<String> expectedAssociatedStandardResumeUserIds = standardResume.userIds().stream().filter(id -> !id.equals(basicUser.id())).collect(Collectors.toSet());
			Resume expectedSideEffect = new Resume(standardResume.id(), standardResume.name(), expectedAssociatedStandardResumeUserIds, standardResume.invitationSent(), standardResume.isPublished());
			Resume actualSideEffect = resumeRepository.findById(standardResume.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertFalse(mongoUserRepository.existsById(basicUser.id()));
			assertEquals(expectedSideEffect, actualSideEffect);
		}

	}

}
