package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificationServiceTest {
	MongoUserRepository mongoUserRepository = mock(MongoUserRepository.class);
	ResumeRepository resumeRepository = mock(ResumeRepository.class);
	VerificationService verificationService;
	String BASIC_ID = "Some-ID";
	String ADMIN_ID = "Some-other-ID";

	MongoUser basicUser = new MongoUser(BASIC_ID, "Basic user's name", "Test password", "BASIC", "8c687299-9ab7-4f68-8fd9-3de3c521227e");
	MongoUser adminUser = new MongoUser(ADMIN_ID, "Admin user's name", "Test password", "ADMIN", "8c687299-9ab7-4f68-8fd9-3de3c521227e");
	String basicUserName = basicUser.username();

	Resume testResume = new Resume("8c687299-9ab7-4f68-8fd9-3de3c521227e", "Company name", "Hello, company!",Set.of("Some-ID"), false, false);

	ResponseStatusException userNotLoggedInException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	ResponseStatusException userAlreadyExistsException = new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
	ResponseStatusException userDoesNotExistException = new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
	ResponseStatusException forbiddenToDeleteAdminException = new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot be deleted");
	String NON_EXISTENT_RESUME_ID = "Some-non-existent-resume-id";
	ResponseStatusException resumeNotFoundException = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");
	ResponseStatusException associatedResumeDoesNotExistException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + NON_EXISTENT_RESUME_ID + " does not exist");
	Principal mockedPrincipalAuthorisedUser = mock(Principal.class);

	@BeforeEach
	void setUp() {
		verificationService = new VerificationService(mongoUserRepository, resumeRepository);
		when(mongoUserRepository.save(any(MongoUser.class))).thenAnswer(invocation -> {
			MongoUser savedUser = invocation.getArgument(0);
			when(mongoUserRepository.findById(savedUser.id())).thenReturn(Optional.of(savedUser));
			when(mongoUserRepository.existsByUsername(savedUser.username())).thenReturn(true);
			when(mongoUserRepository.findByUsername(savedUser.username())).thenReturn(Optional.of(savedUser));
			return new MongoUser(savedUser.id(), savedUser.username(), savedUser.password(), savedUser.role(), savedUser.associatedResume());
		});
		mongoUserRepository.save(adminUser);
		when(mongoUserRepository.findAll()).thenReturn(List.of(adminUser, basicUser));
		when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
			Resume savedResume = invocation.getArgument(0);
			when(resumeRepository.findById(savedResume.id())).thenReturn(Optional.of(savedResume));
			return new Resume(savedResume.id(), savedResume.name(), savedResume.addressing(), savedResume.userIds(), savedResume.invitationSent(), savedResume.isPublished());
		});
		resumeRepository.save(testResume);
		when(mockedPrincipalAuthorisedUser.getName()).thenReturn(basicUserName);
	}

	@Nested
	@DisplayName("userDoesNotExistByUsername()")
	class userDoesNotExistByUsername {

		@Test
		@DirtiesContext
		@DisplayName("... should throw 'Conflict' (409) if user already exists")
		void userDoesNotExistByUsername_shouldThrow409Conflict_ifUserAlreadyExists() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			//WHEN
			ResponseStatusException expected = userAlreadyExistsException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.userDoesNotExistByUsername(basicUserName));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DisplayName("... should return true if user does not exist")
		void userDoesNotExistByUsername_shouldReturnTrue_ifUserDoesNotExist() {
			//WHEN
			boolean expected = true;
			boolean actual = verificationService.userDoesNotExistByUsername(basicUser.username());
			//THEN
			assertEquals(expected, actual);
		}

	}

	@Nested
	@DisplayName("userDoesExistById()")
	class userDoesExistById {

		@Test
		@DirtiesContext
		@DisplayName("... should return the given user if the user to verify exists")
		void userDoesExistById_shouldReturnTheGivenUser_ifTheUserToVerifyExists() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			//WHEN
			MongoUser expected = basicUser;
			MongoUser actual = verificationService.userDoesExistById(basicUser.id());
			//THEN
			assertEquals(expected, actual);
		}

		@Test
		@DisplayName("... should throw 'Not Found' (404) if the user to verify does not exist")
		void userDoesExistById_shouldThrow404NotFound_ifTheUserToVerifyDoesNotExist() {
			//WHEN
			ResponseStatusException expected = userDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.userDoesExistById(BASIC_ID));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

	}

	@Nested
	@DisplayName("userMayBeDeleted()")
	class userMayBeDeleted {

		@Test
		@DisplayName("... should throw 'Forbidden' (403) if the user to delete is an admin")
		void userMaybeDeleted_shouldThrow403Forbidden_ifUserToDeleteIsAdmin() {
			//WHEN
			ResponseStatusException expected = forbiddenToDeleteAdminException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.userMayBeDeleted(adminUser));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DisplayName("... should return true if the user to delete may be deleted")
		void userMayBeDeleted_shouldReturnTrue_ifUserMayBeDeleted() {
			//WHEN
			boolean expected = true;
			boolean actual = verificationService.userMayBeDeleted(basicUser);
			//THEN
			assertEquals(expected, actual);
		}

	}

	@Nested
	@DisplayName("whatResumeUserMayAccess()")
	class whatResumeUserMayAccess {

		@Test
		@DisplayName("... should throw 'Unauthorised' (401) if the user is not authenticated and thus does not have an associated resume")
		void whatResumeUserMayAccess_shouldThrow401Unauthorised_ifUserIsNotAuthenticated() {
			//WHEN
			ResponseStatusException expected = userNotLoggedInException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.whatResumeUserMayAccess(mockedPrincipalAuthorisedUser));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}


		@Test
		@DisplayName("... should throw 'Not Found' (404) if the associated resume saved in the user cannot be found")
		void whatResumeUserMayAccess_shouldThrow404NotFound_ifAssociatedResumeIsNotFound() {
			//GIVEN
			MongoUser corruptedUser = new MongoUser("Valid id", "Different user name", basicUser.password(), basicUser.role(), "non-existent-resume");
			mongoUserRepository.save(corruptedUser);
			when(mockedPrincipalAuthorisedUser.getName()).thenReturn(corruptedUser.username());
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.whatResumeUserMayAccess(mockedPrincipalAuthorisedUser));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DisplayName("... should return the resume if the user's associated resume does exist")
		void whatResumeUserMayAccess_shouldReturnTheResume_ifResumeExists() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipalAuthorisedUser.getName()).thenReturn(basicUserName);
			//WHEN
			Resume expected = testResume;
			Resume actual = verificationService.whatResumeUserMayAccess(mockedPrincipalAuthorisedUser);
			//THEN
			assertEquals(expected, actual);
		}

	}

	@Nested
	@DisplayName("resumeDoesExistById()")
	class resumeDoesExistById {

		@Test
		@DisplayName("... should throw 'Not Found' (404) and 'Unprocessable Entity' (422) respectively if during the primary or secondary request the resume to verify does not exist")
		void resumeDoesExistById_shouldThrow404NotFound_ifTheResumeToVerifyDoesNotExist() {
			//WHEN
			ResponseStatusException expectedPrimaryRequest = resumeNotFoundException;
			ResponseStatusException actualPrimaryRequest = assertThrows(ResponseStatusException.class, () -> verificationService.resumeDoesExistById(NON_EXISTENT_RESUME_ID));
			ResponseStatusException expectedSecondaryRequest = associatedResumeDoesNotExistException;
			ResponseStatusException actualSecondaryRequest = assertThrows(ResponseStatusException.class, () -> verificationService.resumeDoesExistById(NON_EXISTENT_RESUME_ID, true));
			//THEN
			assertEquals(expectedPrimaryRequest.getClass(), actualPrimaryRequest.getClass());
			assertEquals(expectedPrimaryRequest.getMessage(), actualPrimaryRequest.getMessage());
			assertEquals(expectedSecondaryRequest.getClass(), actualSecondaryRequest.getClass());
			assertEquals(expectedSecondaryRequest.getMessage(), actualSecondaryRequest.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("... should return the given resume if the resume to verify exists")
		void resumeDoesExistById_shouldReturnTheGivenResume_ifTheResumeToVerifyExists() {
			//GIVEN
			resumeRepository.save(testResume);
			//WHEN
			Resume expected = testResume;
			Resume actual = verificationService.resumeDoesExistById(testResume.id());
			//THEN
			assertEquals(expected, actual);
		}

	}

}
