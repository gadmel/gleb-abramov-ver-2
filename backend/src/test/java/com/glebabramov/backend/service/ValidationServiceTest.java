package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUserAuthRequest;
import com.glebabramov.backend.model.MongoUserRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {
	ValidationService validationService;

	MongoUserAuthRequest mongoUserAuthRequestValid = new MongoUserAuthRequest("username", "password");
	MongoUserAuthRequest mongoUserAuthRequestWithPasswordNull = new MongoUserAuthRequest("username", null);
	MongoUserAuthRequest mongoUserAuthRequestWithoutPassword = new MongoUserAuthRequest("username", "");
	MongoUserAuthRequest mongoUserAuthRequestWithUsernameNull = new MongoUserAuthRequest(null, "password");
	MongoUserAuthRequest mongoUserAuthRequestWithoutUsername = new MongoUserAuthRequest("", "password");

	MongoUserRequest mongoUserRequestValid = new MongoUserRequest("id", "username", "associatedResume");
	MongoUserRequest mongoUserRequestWithIdNull = new MongoUserRequest(null, "username", "associatedResume");
	MongoUserRequest mongoUserRequestWithoutId = new MongoUserRequest("", "username", "associatedResume");
	MongoUserRequest mongoUserRequestWithUsernameNull = new MongoUserRequest("id", null, "associatedResume");
	MongoUserRequest mongoUserRequestWithoutUsername = new MongoUserRequest("id", "", "associatedResume");
	MongoUserRequest mongoUserRequestWithAssociatedResumeNull = new MongoUserRequest("id", "username", null);
	MongoUserRequest mongoUserRequestWithoutAssociatedResume = new MongoUserRequest("id", "username", "");

	String validId = "id";
	String invalidId1 = null;
	String invalidId2 = "";

	ResponseStatusException usernameIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
	ResponseStatusException passwordIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
	ResponseStatusException idIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
	ResponseStatusException associatedResumeIsRequiredException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Associated resume is required");

	@BeforeEach
	void setUp() {
		validationService = new ValidationService();
	}

	@Nested
	@DisplayName("validateMongoUserAuthRequest()")
	class validateMongoUserAuthRequest {


		@Test
		@DisplayName("... should throw 'Bad Request' (400) if username is null or empty")
		void validateMongoUserAuthRequest_shouldThrow400BadRequest_ifUsernameIsInvalid() {
			//WHEN
			ResponseStatusException expected = usernameIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserAuthRequest(mongoUserAuthRequestWithUsernameNull));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserAuthRequest(mongoUserAuthRequestWithoutUsername));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should throw 'Bad Request' (400) if password is null or empty")
		void validateMongoUserAuthRequest_shouldThrow400BadRequest_ifPasswordIsInvalid() {
			//WHEN
			ResponseStatusException expected = passwordIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserAuthRequest(mongoUserAuthRequestWithPasswordNull));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserAuthRequest(mongoUserAuthRequestWithoutPassword));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should not throw any exception if username and password are valid")
		void validateMongoUserAuthRequest_shouldReturnTrue_ifUsernameAndPasswordAreValid() {
			validationService.validateMongoUserAuthRequest(mongoUserAuthRequestValid);
		}

	}

	@Nested
	@DisplayName("validateMongoUserRequest()")
	class validateMongoUserRequest {

		@Test
		@DisplayName("... should throw 'Bad Request' (400) if id is null or empty")
		void validateMongoUserRequest_shouldThrow400BadRequest_ifIdIsInvalid() {
			//WHEN
			ResponseStatusException expected = idIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithIdNull));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithoutId));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should throw 'Bad Request' (400) if username is null or empty")
		void validateMongoUserRequest_shouldThrow400BadRequest_ifUsernameIsInvalid() {
			//WHEN
			ResponseStatusException expected = usernameIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithUsernameNull));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithoutUsername));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should throw 'Bad Request' (400) if associatedResume is null or empty")
		void validateMongoUserRequest_shouldThrow400BadRequest_ifAssociatedResumeIsInvalid() {
			//WHEN
			ResponseStatusException expected = associatedResumeIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithAssociatedResumeNull));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateMongoUserRequest(mongoUserRequestWithoutAssociatedResume));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should not throw any exception if id, username and associatedResume are valid")
		void validateMongoUserRequest_shouldNotThrowAnyException_ifIdUsernameAndAssociatedResumeAreValid() {
			assertDoesNotThrow(() -> validationService.validateMongoUserRequest(mongoUserRequestValid));
		}

	}

	@Nested
	@DisplayName("validateIdRequest()")
	class validateIdRequest {

		@Test
		@DisplayName("... should throw 'Bad Request' (400) if id is null or empty")
		void validateIdRequest_shouldThrow400BadRequest_ifIdIsInvalid() {
			//WHEN
			ResponseStatusException expected = idIsRequiredException;
			ResponseStatusException actual1 = assertThrows(ResponseStatusException.class, () -> validationService.validateIdRequest(invalidId1));
			ResponseStatusException actual2 = assertThrows(ResponseStatusException.class, () -> validationService.validateIdRequest(invalidId2));
			//THEN
			assertEquals(expected.getMessage(), actual1.getMessage());
			assertEquals(expected.getClass(), actual1.getClass());
			assertEquals(expected.getMessage(), actual2.getMessage());
			assertEquals(expected.getClass(), actual2.getClass());
		}

		@Test
		@DisplayName("... should not throw any exception if id is valid")
		void validateIdRequest_shouldNotThrowAnyException_ifIdIsValid() {
			assertDoesNotThrow(() -> validationService.validateIdRequest(validId));
		}

	}

}
