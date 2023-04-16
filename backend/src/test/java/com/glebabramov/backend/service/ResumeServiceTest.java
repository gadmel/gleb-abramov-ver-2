package com.glebabramov.backend.service;

import com.glebabramov.backend.model.*;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResumeServiceTest {
	ResumeRepository resumeRepository = mock(ResumeRepository.class);
	MongoUserRepository mongoUserRepository = mock(MongoUserRepository.class);
	MongoUserDetailsService mongoUserDetailsService = mock(MongoUserDetailsService.class);
	IdService idService = mock(IdService.class);
	Principal mockedPrincipal = mock(Principal.class);
	ResumeService resumeService;

	String ADMIN_USER_ID = "Some-other-ID";
	String BASIC_USER_ID = "Some-ID";
	String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";
	String BASIC_USERS_ASSOCIATED_RESUME_ID = "Users-specific-resume-ID";
	MongoUser adminUser = new MongoUser(ADMIN_USER_ID, "Admin's name", "Test password", "ADMIN", STANDARD_RESUME_ID);
	MongoUserResponse adminUserResponse = adminUser.toResponseDTO();
	MongoUser basicUser = new MongoUser(BASIC_USER_ID, "Basic user's name", "Test password", "BASIC", BASIC_USERS_ASSOCIATED_RESUME_ID);
	Resume oldResume = new Resume(BASIC_USERS_ASSOCIATED_RESUME_ID, "Company name", Set.of(BASIC_USER_ID), false, false);
	MongoUserResponse basicUserResponse = basicUser.toResponseDTO();
	String TEST_RESUMES_ID = "Some-Resume-ID";
	Resume basicUsersResume = new Resume(TEST_RESUMES_ID, "Company name", Set.of(BASIC_USER_ID), false, false);
	Resume standardResume = new Resume(STANDARD_RESUME_ID, "Company name", Set.of(ADMIN_USER_ID), false, false);
	ResumeCreateRequest testResumeCreateRequest = new ResumeCreateRequest("Company name", Set.of(BASIC_USER_ID));
	ResumeRequest testResumeRequest = new ResumeRequest(TEST_RESUMES_ID, "Company name", Set.of(BASIC_USER_ID));
	String INVALID_USER_ID = "Some-Invalid-Resume-ID";
	ResponseStatusException unauthorisedUserException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	ResponseStatusException resumeNotFoundException = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");
	ResponseStatusException nonExistentUserException = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because user with id " + INVALID_USER_ID + " does not exist");
	ResponseStatusException associatedResumeDoesNotExistException(String associatedResumeId) {
		return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + associatedResumeId + " does not exist");
	}
	ResponseStatusException forbiddenToViewResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view all resumes");
	ResponseStatusException forbiddenToCreateResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to create resumes");
	ResponseStatusException forbiddenToUpdateResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to update resumes");
	ResponseStatusException forbiddenToDeleteResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to delete resumes");

	@BeforeEach
	void setUp() {
		resumeService = new ResumeService(resumeRepository, mongoUserRepository, mongoUserDetailsService, idService);

		when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenReturn(adminUserResponse);
		when(mongoUserRepository.save(any(MongoUser.class))).thenAnswer(invocation -> {
			MongoUser savedUser = invocation.getArgument(0);
			when(mongoUserRepository.findById(savedUser.id())).thenReturn(Optional.of(savedUser));
			when(mongoUserRepository.existsByUsername(savedUser.username())).thenReturn(true);
			when(mongoUserRepository.findByUsername(savedUser.username())).thenReturn(Optional.of(savedUser));
			return new MongoUser(savedUser.id(), savedUser.username(), savedUser.password(), savedUser.role(), savedUser.associatedResume());
		});
		when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
			Resume savedResume = invocation.getArgument(0);
			when(resumeRepository.findById(savedResume.id())).thenReturn(Optional.of(savedResume));

			return new Resume(savedResume.id(), savedResume.name(), savedResume.userIds(), savedResume.invitationSent(), savedResume.isPublished());
		});
		when(idService.generateId()).thenReturn(TEST_RESUMES_ID);
		resumeRepository.save(basicUsersResume);
	}

	@Nested
	@DisplayName("getAllResumes()")
	class getAllResumes {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void getAllResumes_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenThrow(unauthorisedUserException);
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
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenReturn(basicUserResponse);
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
			when(resumeRepository.findAll()).thenReturn(List.of(basicUsersResume));
			//WHEN
			List<Resume> expected = Arrays.asList(basicUsersResume);
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
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenThrow(unauthorisedUserException);
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
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenReturn(basicUserResponse);
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
			mongoUserRepository.save(basicUser);
			//WHEN
			ResponseStatusException expected = associatedResumeDoesNotExistException(BASIC_USERS_ASSOCIATED_RESUME_ID);
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
			mongoUserRepository.save(basicUser);
			resumeRepository.save(oldResume);
			ResumeCreateRequest createRequestWithAUserWhoHasAnExistingResume = new ResumeCreateRequest("Company name", Set.of(BASIC_USER_ID));
			//WHEN
			Resume expected = new Resume(TEST_RESUMES_ID, "Company name", Set.of(basicUser.id()), false, false);
			Resume actual = resumeService.createResume(createRequestWithAUserWhoHasAnExistingResume, mockedPrincipal);
			MongoUser expectedSideEffect1 = new MongoUser(basicUser.id(), basicUser.username(), basicUser.password(), basicUser.role(), TEST_RESUMES_ID);
			MongoUser actualSideEffect1 = mongoUserRepository.findById(BASIC_USER_ID).get();
			Set<String> oldResumesUserIds = oldResume.userIds().stream().filter(userId -> !userId.equals(BASIC_USER_ID)).collect(Collectors.toSet());
			Resume expectedSideEffect2 = new Resume(oldResume.id(), oldResume.name(), oldResumesUserIds, oldResume.invitationSent(), oldResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(oldResume.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
		}

	}

	@Nested
	@DisplayName("updateResume()")
	class updateResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void updateResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenThrow(unauthorisedUserException);
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.updateResume(testResumeRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void updateResume_shouldThrow403Forbidden_ifUserIsNotAdmin() {
			//GIVEN
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenReturn(basicUserResponse);
			//WHEN
			ResponseStatusException expected = forbiddenToUpdateResumesException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.updateResume(testResumeRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume's to update associated user does not exist")
		void updateResume_shouldThrow422UnprocessableEntity_ifResumesToUpdateAssociatedUserDoesNotExist() {
			//GIVEN
			Resume resumeWithNonExistentUser = new Resume(TEST_RESUMES_ID, "Company name", Set.of(INVALID_USER_ID), false, false);
			resumeRepository.save(resumeWithNonExistentUser);
			//WHEN
			ResponseStatusException expected = nonExistentUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.updateResume(testResumeRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Not Found' (404) if the resume's to update does not exist")
		void updateResume_shouldThrow404NotFound_ifResumeToUpdateDoesNotExist() {
			resumeRepository.save(basicUsersResume);
			ResumeRequest resumeUpdateRequest = new ResumeRequest(BASIC_USERS_ASSOCIATED_RESUME_ID, "Company name", Collections.emptySet());
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.updateResume(resumeUpdateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume's to update associated user's previously assigned resume does not exist")
		void updateResume_shouldThrow422UnprocessableEntity_ifResumesToUpdateAssociatedUsersPreviouslyAssignedResumeDoesNotExist() {
			//GIVEN
			resumeRepository.save(standardResume);
			Resume resumeWithNonExistentUser = new Resume(BASIC_USERS_ASSOCIATED_RESUME_ID, "Company name", Set.of(BASIC_USER_ID), false, false);
			resumeRepository.save(resumeWithNonExistentUser);
			MongoUser userWithNonExistentResume = new MongoUser(BASIC_USER_ID, "username", "password", "user", "non-existent-resume-id");
			mongoUserRepository.save(userWithNonExistentResume);
			ResumeRequest resumeUpdateRequest = new ResumeRequest(BASIC_USERS_ASSOCIATED_RESUME_ID, "Company name", Collections.emptySet());
			//WHEN
			ResponseStatusException expected = associatedResumeDoesNotExistException("non-existent-resume-id");
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.updateResume(resumeUpdateRequest, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should update the resume, handle respective user reassignments and return the updated resume if the user is an admin and all operations have been verified")
		void updateResume_shouldUpdateResumeHandleRespectiveUserReassignments_andReturnUpdatedResume_ifUserIsAdminAndAllOperationsHaveBeenVerified() {
			//GIVEN
			resumeRepository.save(standardResume);
			resumeRepository.save(basicUsersResume);
			mongoUserRepository.save(basicUser);
			mongoUserRepository.save(adminUser);
			resumeRepository.save(oldResume);
			ResumeRequest resumeUpdateRequest = new ResumeRequest(TEST_RESUMES_ID, "Company name", Set.of(ADMIN_USER_ID));
			//WHEN
			Resume expected = new Resume(TEST_RESUMES_ID, "Company name", Set.of(ADMIN_USER_ID), false, false);
			Resume actual = resumeService.updateResume(resumeUpdateRequest, mockedPrincipal);
			MongoUser expectedSideEffect1 = new MongoUser(BASIC_USER_ID, basicUser.username(), basicUser.password(), basicUser.role(), STANDARD_RESUME_ID);
			MongoUser actualSideEffect1 = mongoUserRepository.findById(BASIC_USER_ID).get();
			MongoUser expectedSideEffect2 = new MongoUser(ADMIN_USER_ID, adminUser.username(), adminUser.password(), adminUser.role(), TEST_RESUMES_ID);
			MongoUser actualSideEffect2 = mongoUserRepository.findById(ADMIN_USER_ID).get();
			Resume expectedSideEffect4 = new Resume(BASIC_USERS_ASSOCIATED_RESUME_ID, oldResume.name(), Collections.emptySet(), oldResume.invitationSent(), oldResume.isPublished());
			Resume actualSideEffect4 = resumeRepository.findById(BASIC_USERS_ASSOCIATED_RESUME_ID).get();
			Resume expectedSideEffect3 = new Resume(STANDARD_RESUME_ID, standardResume.name(), Set.of(BASIC_USER_ID), standardResume.invitationSent(), standardResume.isPublished());
			Resume actualSideEffect3 = resumeRepository.findById(STANDARD_RESUME_ID).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
			assertEquals(expectedSideEffect4, actualSideEffect4);
			assertEquals(expectedSideEffect3, actualSideEffect3);
		}
	}


	@Nested
	@DisplayName("deleteResume()")
	class deleteResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void deleteResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//GIVEN
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenThrow(unauthorisedUserException);
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
			when(mongoUserDetailsService.getCurrentUser(mockedPrincipal)).thenReturn(basicUserResponse);
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
			when(resumeRepository.findById(TEST_RESUMES_ID)).thenReturn(Optional.empty());
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
			Resume standardResume = new Resume(STANDARD_RESUME_ID, "Standard resume", Set.of(ADMIN_USER_ID), false, false);
			resumeRepository.save(standardResume);
			mongoUserRepository.save(adminUser);
			MongoUser testUser = new MongoUser(BASIC_USER_ID, "Test user", "password", "BASIC", BASIC_USERS_ASSOCIATED_RESUME_ID);
			mongoUserRepository.save(testUser);
			Resume resumeToDelete = new Resume(BASIC_USERS_ASSOCIATED_RESUME_ID, "Company name", Set.of(BASIC_USER_ID), false, false);
			resumeRepository.save(resumeToDelete);
			//WHEN
			Resume expected = resumeToDelete;
			Resume actual = resumeService.deleteResume(BASIC_USERS_ASSOCIATED_RESUME_ID, mockedPrincipal);
			when(resumeRepository.findById(BASIC_USERS_ASSOCIATED_RESUME_ID)).thenReturn(Optional.empty());
			MongoUser expectedSideEffect = new MongoUser(testUser.id(), testUser.username(), testUser.password(), testUser.role(), STANDARD_RESUME_ID);
			MongoUser actualSideEffect = mongoUserRepository.findById(testUser.id()).get();
			//THEN
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect, actualSideEffect);
		}

	}

	@Nested
	@DisplayName("getAssociatedResume()")
	class getAssociatedResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void getAssociatedResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = unauthorisedUserException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.getAssociatedResume(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return 'Not Found' (404) if the user is logged in but has a corrupted association to a resume")
		void getAssociatedResume_shouldThrow404NotFound_ifUsersAssociatedResumeIsInvalid() {
			//GIVEN
			MongoUser userWithInvalidAssociatedResume = new MongoUser("Different-id", "Different-name", basicUser.password(), basicUser.role(), "invalid-resume-id");
			mongoUserRepository.save(userWithInvalidAssociatedResume);
			when(mockedPrincipal.getName()).thenReturn(userWithInvalidAssociatedResume.username());
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.getAssociatedResume(mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return resume if the user is logged in and has a valid association to a resume")
		void getAssociatedResume_shouldReturnResume_ifUserIsLoggedInAndHasValidAssociation() {
			//GIVEN
			mongoUserRepository.save(basicUser);
			when(mockedPrincipal.getName()).thenReturn(basicUser.username());
			resumeRepository.save(oldResume);
			//WHEN
			Resume expected = oldResume;
			Resume actual = resumeService.getAssociatedResume(mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
		}


	}

}
