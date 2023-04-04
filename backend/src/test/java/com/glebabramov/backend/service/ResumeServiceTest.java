package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ResumeServiceTest {

	@Autowired
	ResumeRepository resumeRepository;
	@Autowired
	MongoUserRepository mongoUserRepository;
	IdService idService = mock(IdService.class);
	@Autowired
	AuthorisationService authorisationService;
	@Autowired
	VerificationService verificationService;
	Principal mockedPrincipal = mock(Principal.class);
	@Autowired
	ResumeService resumeService;

	String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";
	String ADMIN_USER_ID = "Some-other-ID";
	String BASIC_USER_ID = "Some-ID";
	String BASIC_USERS_ASSOCIATED_RESUME_ID = "Users-specific-resume-ID";
	MongoUser adminUser = new MongoUser(ADMIN_USER_ID, "Admin's name", "Test password", "ADMIN", STANDARD_RESUME_ID);
	MongoUser basicUser = new MongoUser(BASIC_USER_ID, "Basic user's name", "Test password", "BASIC", BASIC_USERS_ASSOCIATED_RESUME_ID);
	String TEST_RESUMES_ID = "Some-Resume-ID";
	Resume basicUsersResume = new Resume(TEST_RESUMES_ID, "Company name", Set.of(BASIC_USER_ID), false, false);
	ResumeCreateRequest testResumeCreateRequest = new ResumeCreateRequest("Company name", Set.of(BASIC_USER_ID));
	String INVALID_USER_ID = "Some-Invalid-Resume-ID";
	ResponseStatusException nonExistentUserException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because user with id " + INVALID_USER_ID + " does not exist");
	ResponseStatusException associatedResumeDoesNotExistException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + BASIC_USERS_ASSOCIATED_RESUME_ID + " does not exist");
	ResponseStatusException unauthorisedUserException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	ResponseStatusException forbiddenToViewResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view all resumes");
	ResponseStatusException forbiddenToCreateResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to create resumes");
	ResponseStatusException forbiddenToDeleteResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to delete resumes");
	ResponseStatusException resumeNotFoundException = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");

	@BeforeEach
	void setUp() {
		resumeService = new ResumeService(resumeRepository, mongoUserRepository, idService, authorisationService, verificationService);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
		when(idService.generateId()).thenReturn(TEST_RESUMES_ID);
	}

	@Nested
	@DisplayName("getAllResumes()")
	class getAllResumes {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void getAllResumes_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.getAllResumes(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void getAllResumes_shouldThrow403Forbidden_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToViewResumesException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.getAllResumes(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return 'OK' (200) and a list of all resumes if the user is an admin")
		void getAllResumes_shouldReturn200OK_andAListOfAllResumes_ifUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			resumeRepository.save(basicUsersResume);
			List<Resume> expected = Arrays.asList(basicUsersResume);
			//WHEN
			List<Resume> actual = resumeService.getAllResumes(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}


	}

	@Nested
	@DisplayName("createResume()")
	class createResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void createResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.createResume(testResumeCreateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void createResume_shouldThrow403Forbidden_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToCreateResumesException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.createResume(testResumeCreateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume's to create associated user does not exist")
		void createResume_shouldThrow404NotFound_ifResumesToCreateAssociatedUserDoesNotExist() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			ResumeCreateRequest resumeCreateRequest = new ResumeCreateRequest("Company name", Set.of(INVALID_USER_ID));
			//WHEN
			ResponseStatusException expected = nonExistentUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.createResume(resumeCreateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume's to create associated user exists but his previously assigned resume does not exist and thus cannot be unassigned")
			// Redundant: tests the behaviour, which cannot occur so long only the prescribed methods are used to manipulate data (data has not been corrupted through direct access to the database)
		void createResume_shouldThrow404NotFound_ifResumeToCreateAssociatedUsersPreviouslyAssignedResumeDoesNotExist() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			//WHEN
			ResponseStatusException expected = associatedResumeDoesNotExistException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.createResume(testResumeCreateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should create a new resume, reassign the associated users to it, unassign the associated users' previously assigned resumes from these users and return 'Created' (201) and the created resume if the user is an admin, the created resume associated users exist and their previously assigned resumes exist")
		void createResume_shouldReturn201Created_andTheCreatedResume_ifUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			Resume oldResume = new Resume(basicUser.associatedResume(), "Company name", Set.of(basicUser.id()), false, false);
			resumeRepository.save(oldResume);
			ResumeCreateRequest createRequestWithAUserWhoHasAnExistingResume = new ResumeCreateRequest("Company name", Set.of(basicUser.id()));
			//WHEN
			Resume expected = new Resume(TEST_RESUMES_ID, "Company name", Set.of(basicUser.id()), false, false);
			Resume actual = resumeService.createResume(createRequestWithAUserWhoHasAnExistingResume, mockedPrincipal);
			MongoUser expectedSideEffect1 = new MongoUser(basicUser.id(), basicUser.username(), basicUser.password(), basicUser.role(), TEST_RESUMES_ID);
			MongoUser actualSideEffect1 = mongoUserRepository.findById(basicUser.id()).get();
			Set<String> oldResumesUserIds = new HashSet<>(oldResume.userIds());
			oldResumesUserIds.removeAll(createRequestWithAUserWhoHasAnExistingResume.userIds());
			Resume expectedSideEffect2 = new Resume(oldResume.id(), oldResume.name(), oldResumesUserIds, oldResume.invitationSent(), oldResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(oldResume.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
		}

	}

	@Nested
	@DisplayName("deleteResume()")
	class deleteResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void deleteResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(TEST_RESUMES_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void deleteResume_shouldThrow403Forbidden_ifUserIsNotAdmin() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			//WHEN
			ResponseStatusException expected = forbiddenToDeleteResumesException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(TEST_RESUMES_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return 'Not Found' (404) if the user is an admin but the resume to delete does not exist")
		void deleteResume_shouldReturn404NotFound_ifResumeDoesNotExist() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(TEST_RESUMES_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return 'Unprocessable Entity' (422) if the user is an admin and the resume to delete does exist but one of the users who are assigned to it does not exist and thus cannot be unassigned from it")
		void deleteResume_shouldReturn422UnprocessableEntity_ifOneOfTheUsersAssignedDoesNotExist() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			Resume resumeWithNonExistentUser = new Resume(TEST_RESUMES_ID, "Company name", Set.of(INVALID_USER_ID), false, false);
			resumeRepository.save(resumeWithNonExistentUser);
			//WHEN
			ResponseStatusException expected = nonExistentUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(TEST_RESUMES_ID, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should delete the resume and reassign the standard resume to the users who were assigned to the deleted one, return 'OK' (200) and the deleted resume if user is an admin, the resume to delete exists and all users who were assigned to it exist")
		void deleteResume_shouldDeleteResumeAndReassignTheStandardToItsUsers_andReturnIt_ifResumeAndItsUsersExistAndUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			resumeRepository.save(basicUsersResume);
			//WHEN
			Resume expected = basicUsersResume;
			Resume actual = resumeService.deleteResume(TEST_RESUMES_ID, mockedPrincipal);
			MongoUser expectedSideEffect = new MongoUser(basicUser.id(), basicUser.username(), basicUser.password(), basicUser.role(), STANDARD_RESUME_ID);
			MongoUser actualSideEffect = mongoUserRepository.findById(basicUser.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertTrue(resumeRepository.findById(TEST_RESUMES_ID).isEmpty());
			assertEquals(expectedSideEffect, actualSideEffect);
		}

	}

}
