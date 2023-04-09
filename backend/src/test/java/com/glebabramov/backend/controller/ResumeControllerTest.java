package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import com.glebabramov.backend.service.IdService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeControllerTest {
	@Autowired
	MockMvc mockMvc;
	@Autowired
	MongoUserRepository mongoUserRepository;
	@Autowired
	ResumeRepository resumeRepository;
	IdService idService = mock(IdService.class);
	Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
	Principal mockedPrincipal = mock(Principal.class);

	MongoUser adminUser;
	MongoUser basicUser;
	String rawPassword = "password";
	String testResumeId = "Some-resume-ID";
	String testResumesAssignedUserId = "Some-user-ID";
	Resume testResume = new Resume(testResumeId, "Company name", Set.of(testResumesAssignedUserId), false, false);
	MongoUser testResumeAssignedUser = new MongoUser(testResumesAssignedUserId, "Test resume's assigned user", encoder.encode(rawPassword), "BASIC", testResumeId);

	@BeforeEach
	void setUp() {
		adminUser = new MongoUser("Some-admin-ID", "Test admin", encoder.encode(rawPassword), "ADMIN", testResumeId);
		basicUser = new MongoUser("Another-user-ID", "Test user", encoder.encode(rawPassword), "BASIC", testResumeId);
		mongoUserRepository.save(basicUser);
		mongoUserRepository.save(adminUser);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
		when(idService.generateId()).thenReturn(testResumeId);
	}

	@Nested
	@DisplayName("GET /api/admin/resumes/")
	class getAllResumes {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void getAllResumes_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(get("/api/admin/resumes/"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = {"BASIC"})
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void getAllResumes_shouldThrow403Forbidden_ifUserIsNotAdmin() throws Exception {
			mockMvc.perform(get("/api/admin/resumes/"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should return 'OK' (200) and a list of all resumes if the user is an admin")
		void getAllResumes_shouldReturn200Ok_andListOfAllResumes_ifUserIsAdmin() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			// WHEN
			mockMvc.perform(get("/api/admin/resumes/"))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							[
								{
									"id": "Some-resume-ID",
									"name": "Company name",
									"userIds": ["Some-user-ID"],
									"invitationSent": false,
									"isPublished": false
								}
							]
							"""
					));
		}

	}

	@Nested
	@DisplayName("POST /api/admin/resumes/create/")
	class createResume {
		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void createResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Company name",
										"userIds": ["Some-user-id"]
									}
									"""
							))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = {"BASIC"})
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void createResume_shouldThrow403Forbidden_ifUserIsNotAdmin() throws Exception {
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Company name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume's to create associated user does not exist")
		void createResume_shouldThrow404NotFound_ifResumesToCreateAssociatedUserDoesNotExist() throws Exception {
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Company name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume's to create associated user exists but his previously assigned resume does not exist and thus cannot be unassigned")
		void createResume_shouldThrow422UnprocessableEntity_ifResumesToCreateAssociatedUserExistsButHisPreviouslyAssignedResumeDoesNotExist() throws Exception {
			// GIVEN
			MongoUser userWithAnInvalidAssignedResume = new MongoUser(testResumesAssignedUserId, testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), "Some-invalid-resume-id");
			mongoUserRepository.save(userWithAnInvalidAssignedResume);
			// WHEN
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Company name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should create a new resume, reassign the associated users to it, unassign the associated users' previously assigned resumes from these users and return 'Created' (201) and the created resume if the user is an admin")
		void createResume_shouldReturn201Created_andCreatedResume_ifUserIsAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(testResumeAssignedUser);
			resumeRepository.save(testResume);
			// WHEN
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Resume name",
										"userIds": ["Some-user-ID"]
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().json("""
							{
								"name": "Resume name",
								"userIds": ["Some-user-ID"],
								"invitationSent": false,
								"isPublished": false
							}
							"""))
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andReturn().getResponse().getContentAsString();
			// AND
			MongoUser expectedSideEffect1 = new MongoUser(basicUser.id(), basicUser.username(), basicUser.password(), basicUser.role(), testResumeId);
			MongoUser actualSideEffect1 = mongoUserRepository.findById(basicUser.id()).get();
			Set<String> oldResumesUserIds = new HashSet<>();
			Resume expectedSideEffect2 = new Resume(testResume.id(), testResume.name(), oldResumesUserIds, testResume.invitationSent(), testResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(testResumeId).get();
			// THEN
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);

		}
	}

	@Nested
	@DisplayName("DELETE /api/admin/resumes/delete/{id}/")
	class deleteResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void deleteResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResume.id() + "/")
							.with(csrf()))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = {"BASIC"})
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void deleteResume_shouldThrow403Forbidden_ifUserIsNotAdmin() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResume.id() + "/")
							.with(csrf()))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Not Found' (404) if user is an admin but the resume does not exist")
		void deleteResume_shouldThrow404NotFound_ifResumeDoesNotExist() throws Exception {
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResume.id() + "/")
							.with(csrf()))
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume to delete does exist but one of the users who are assigned to it does not exist and thus cannot be unassigned from it")
		void deleteResume_shouldThrow422UnprocessableEntity_ifOneOfTheUsersAssignedDoesNotExist() throws Exception {
			// GIVEN
			Resume resumeWithNonExistingUser = new Resume("Some-valid-ID", "Company name", Set.of("Some-non-existing-user-id"), false, false);
			resumeRepository.save(resumeWithNonExistingUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + resumeWithNonExistingUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isUnprocessableEntity());
		}


		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should delete resume, return 'OK' (200) and the deleted resume if the user is an admin and the resume does exist")
		void deleteResume_shouldDeleteResumeAndReassignResumeToItsUsers_andReturn200OK_ifUserIsAdminAndResumeExists() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			mongoUserRepository.save(testResumeAssignedUser);
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResume.id() + "/")
							.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"id": "Some-resume-ID",
								"name": "Company name",
								"userIds": ["Some-user-ID"],
								"invitationSent": false,
								"isPublished": false
							}
							"""));
			// THEN
			Optional<Resume> expected = Optional.empty();
			Optional<Resume> actual = resumeRepository.findById(testResume.id());
			assertEquals(expected, actual);
		}

	}

}
