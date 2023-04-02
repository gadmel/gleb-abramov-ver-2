package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VerificationServiceTest {
	@Autowired
	MongoUserRepository mongoUserRepository;
	@Autowired
	ResumeRepository resumeRepository;
	VerificationService verificationService;

	MongoUser basicUser = new MongoUser("Some other ID", "Basic user's name", "Test password", "BASIC", "[]");
	String basicUserName = basicUser.username();
	String basicUserId = basicUser.id();
	Resume testResume = new Resume("Some-ID", "Company name", "Some-user-id", false,false);
	String testResumeId = testResume.id();
	ResponseStatusException userAlreadyExistsException = new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
	ResponseStatusException userDoesNotExistException = new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
	ResponseStatusException resumeNotFoundException  = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");
	ResponseStatusException associatedResumeDoesNotExistException = new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated resume does not exist");

	@BeforeEach
	void setUp() {
		verificationService = new VerificationService(mongoUserRepository, resumeRepository);
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
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.userDoesExistById(basicUserId));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

	}

	@Nested
	@DisplayName("resumeDoesExistById()")
	class resumeDoesExistById {

		@Test
		@DisplayName("... should throw 'Not Found' (404) if during the primary or secondary request the resume to verify does not exist")
		void resumeDoesExistById_shouldThrow404NotFound_ifTheResumeToVerifyDoesNotExist() {
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException expectedSecondaryRequest = associatedResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> verificationService.resumeDoesExistById(testResumeId));
			ResponseStatusException actualSecondaryRequest = assertThrows(ResponseStatusException.class, () -> verificationService.resumeDoesExistById(testResumeId, true));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
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
