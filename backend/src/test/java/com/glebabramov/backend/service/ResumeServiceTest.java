package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
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

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

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
	@Autowired VerificationService verificationService;
	Principal mockedPrincipal = mock(Principal.class);
	@Autowired
	ResumeService resumeService;

	MongoUser adminUser = new MongoUser("Some ID", "Admin's name", "Test password", "ADMIN", "[]");
	MongoUser basicUser = new MongoUser("Some other ID", "Basic user's name", "Test password", "BASIC", "[]");

	Resume testResume = new Resume("Some ID", "Company name", "Some user id", false,false);
	String testResumeId = testResume.id();
	ResumeCreateRequest testResumeCreateRequest = new ResumeCreateRequest("Company name", "Some user id");
	ResponseStatusException unauthorisedUserException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	ResponseStatusException forbiddenToViewResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view all resumes");
	ResponseStatusException forbiddenToCreateResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to create resumes");
	ResponseStatusException forbiddenToDeleteResumesException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to delete resumes");
	ResponseStatusException resumeNotFoundException = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");

	@BeforeEach
	void setUp() {
		resumeService = new ResumeService(resumeRepository, idService, authorisationService, verificationService);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
		when(idService.generateId()).thenReturn(testResume.id());
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
			resumeRepository.save(testResume);
			List<Resume> expected = Arrays.asList(testResume);
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
		@DisplayName("...should return 'Created' (201) and the created resume if the user is an admin")
		void createResume_shouldReturn201Created_andTheCreatedResume_ifUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			Resume expected = testResume;
			//WHEN
			Resume actual = resumeService.createResume(testResumeCreateRequest, mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
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
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(testResumeId, mockedPrincipal));
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
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(testResumeId, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return 'Not Found' (404) if the resume does not exist and the user is an admin")
		void deleteResume_shouldReturn404NotFound_ifResumeDoesNotExistAndUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			ResponseStatusException expected = resumeNotFoundException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> resumeService.deleteResume(testResumeId, mockedPrincipal));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should delete the resume, return 'OK' (200) and the deleted resume if the resume exists and the user is an admin")
		void deleteResume_shouldDeleteResume_andReturnIt_ifResumeExistsAndUserIsAdmin() {
			//GIVEN
			mongoUserRepository.save(adminUser);
			resumeRepository.save(testResume);
			Resume expected = testResume;
			//WHEN
			Resume actual = resumeService.deleteResume(testResume.id(), mockedPrincipal);
			//THEN
			assertEquals(expected, actual);
			resumeRepository.findById(testResume.id()).ifPresentOrElse(
					resume -> fail("Resume was not deleted"),
					() -> assertTrue(true)
			);
		}

	}

}
