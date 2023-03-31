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
import java.util.Optional;

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
	Resume testResume = new Resume("Some-ID", "Company name", "Some user id", false, false);

	@BeforeEach
	void setUp() {
		adminUser = new MongoUser("Some ID", "Test admin", encoder.encode(rawPassword), "ADMIN", "company");
		basicUser = new MongoUser("Another ID", "Test user", encoder.encode(rawPassword), "BASIC", "company");
		mongoUserRepository.save(basicUser);
		mongoUserRepository.save(adminUser);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
		when(idService.generateId()).thenReturn("Some ID");
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
									"id": "Some-ID",
									"name": "Company name",
									"userId": "Some user id",
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
										"userId": "Some user id"
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
										"userId": "Some user id"
									}
									"""))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should return 'Created' (201) and the created resume if the user is an admin")
		void createResume_shouldReturn201Created_andCreatedResume_ifUserIsAdmin() throws Exception {
			mockMvc.perform(post("/api/admin/resumes/create/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"name": "Company name",
										"userId": "Some user id"
									}
									"""))
					.andExpect(status().isCreated())
					.andExpect(content().json("""
							{
								"name": "Company name",
								"userId": "Some user id",
								"invitationSent": false,
								"isPublished": false
							}
							"""))
					.andExpect(jsonPath("$.id").isNotEmpty());

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
		@DisplayName("...should delete resume, return 'OK' (200) and the deleted resume if the user is an admin and the resume does exist")
		void deleteResume_shouldDeleteResume_return200OK_andDeletedResume_ifUserIsAdminAndResumeExists() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResume.id() + "/")
							.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"id": "Some-ID",
								"name": "Company name",
								"userId": "Some user id",
								"invitationSent": false,
								"isPublished": false
							}
							"""));
			// THEN
			Optional<Resume> expected = Optional.empty();
			Optional<Resume> actual = resumeRepository.findById(testResume.id());
			Assertions.assertEquals(expected, actual);
		}
	}

}
