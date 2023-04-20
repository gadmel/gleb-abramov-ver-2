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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
	Resume testResume = new Resume(testResumeId, "Company name", "Hello, company!", Set.of(testResumesAssignedUserId), false, false);
	MongoUser testResumeAssignedUser = new MongoUser(testResumesAssignedUserId, "Test resume's assigned user", encoder.encode(rawPassword), "BASIC", testResumeId);
	Resume standardResume = new Resume("8c687299-9ab7-4f68-8fd9-3de3c521227e", "Standard company name", "Hello, company!", Set.of(), false, false);

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
	@DisplayName("GET /api/resume/")
	class getResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user not logged in")
		void getResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(get("/api/resume/"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Corrupted user", roles = "BASIC")
		@DisplayName("...should throw 'Not found' (404) if the user has an invalid resume ID")
		void getResume_shouldThrow404NotFound_ifUserHasInvalidResumeId() throws Exception {
			MongoUser userWithInvalidResumeId = new MongoUser("Some-user-ID", "Corrupted user", encoder.encode(rawPassword), "BASIC", "Invalid-resume-ID");
			mongoUserRepository.save(userWithInvalidResumeId);
			mockMvc.perform(get("/api/resume/"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test resume's assigned user", roles = "BASIC")
		@DisplayName("...should return the resume associated with the user logged in")
		void getResume_shouldReturnTheResumeAssociatedWithTheLoggedInUser() throws Exception {
			mongoUserRepository.save(testResumeAssignedUser);
			resumeRepository.save(testResume);
			mockMvc.perform(get("/api/resume/"))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"id": "Some-resume-ID",
								"name": "Company name",
								"userIds": ["Some-user-ID"],
								"invitationSent": false,
								"isPublished": false
							}
							"""
					));

		}

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
			Resume expectedSideEffect2 = new Resume(testResume.id(), testResume.name(), testResume.addressing(), oldResumesUserIds, testResume.invitationSent(), testResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(testResumeId).get();
			// THEN
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);

		}

	}

	@Nested
	@DisplayName("PUT /api/admin/resumes/update/")
	class updateResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void updateResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = {"BASIC"})
		@DisplayName("...should throw 'Forbidden' (403) if the user is not an admin")
		void updateResume_shouldThrow403Forbidden_ifUserIsNotAdmin() throws Exception {
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Not Found' (404) if the user is an admin but the resume to update does not exist")
		void updateResume_shouldThrow404NotFound_ifResumeToUpdateDoesNotExist() throws Exception {
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Some-user-id"]
									}
									"""))
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume to update has an invalid associated user's id")
		void updateResume_shouldThrow422UnprocessableEntity_ifAssociatedUserDoesNotExist() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			// WHEN
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Some-invalid-user-ID"]
									}
									"""))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin but the resume's to update associated user's previously assigned resume does not exist and thus cannot be unassigned from it")
		void updateResume_shouldThrow422UnprocessableEntity_ifAssociatedUserPreviouslyAssignedResumeDoesNotExist() throws Exception {
			// GIVEN
			MongoUser corruptedUser = new MongoUser("Another-user-ID", testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), "Some-invalid-resume-ID");
			mongoUserRepository.save(corruptedUser);
			resumeRepository.save(testResume);
			mongoUserRepository.save(testResumeAssignedUser);
			// WHEN
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Some-user-ID", "Another-user-ID"]
									}
									"""))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume's to update associated user's previously assigned resume does exist but the standard resume does not and thus the user cannot be re-assigned to it")
		void updateResume_shouldThrow422UnprocessableEntity_ifAssociatedUserPreviouslyAssignedResumeExistsButReplacingResumeDoesNotExist() throws Exception {
			// GIVEN
			MongoUser anotherTestUser = new MongoUser("Another-user-ID", testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), "Some-valid-resume-ID");
			mongoUserRepository.save(anotherTestUser);
			Resume testResume2 = new Resume("Some-valid-resume-ID", testResume.name(), testResume.addressing(), Set.of("Another-user-ID"), testResume.invitationSent(), testResume.isPublished());
			resumeRepository.save(testResume);
			resumeRepository.save(testResume2);
			mongoUserRepository.save(testResumeAssignedUser);
			// WHEN
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Another-user-ID"]
									}
									"""))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should update the resume, and handle the associated users' (former and newly set) resume assignments, return 200 (OK) and the updated resume if the user is an admin, resume to update exists, associated users exist and their assigned resumes as well as the replacing resume exist")
		void updateResume_shouldreturn200OKAndTheUpdatedResume_andUpdateResumeAndItsAssociatedUsers__ifResumeToUpdateAndAllAssociatedUsersAndResumesDoExist() throws Exception {
			// GIVEN
			MongoUser anotherTestUser = new MongoUser("Another-user-ID", testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), "Some-valid-resume-ID");
			mongoUserRepository.save(anotherTestUser);
			mongoUserRepository.save(testResumeAssignedUser);
			Resume testResume2 = new Resume("Some-valid-resume-ID", testResume.name(), testResume.addressing(), Set.of("Another-user-ID"), testResume.invitationSent(), testResume.isPublished());
			resumeRepository.save(testResume2);
			resumeRepository.save(testResume);
			resumeRepository.save(standardResume);
			// WHEN
			mockMvc.perform(put("/api/admin/resumes/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-resume-ID",
										"name": "Resume name",
										"userIds": ["Another-user-ID"]
									}
									"""))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"id": "Some-resume-ID",
								"name": "Resume name",
								"userIds": ["Another-user-ID"],
								"invitationSent": false,
								"isPublished": false
							}
							"""));
			// THEN
			MongoUser expectedSideEffect1 = new MongoUser(testResumesAssignedUserId, testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), standardResume.id());
			MongoUser actualSideEffect1 = mongoUserRepository.findById(testResumesAssignedUserId).get();
			Resume expectedSideEffect2 = new Resume(standardResume.id(), standardResume.name(), standardResume.addressing(), Set.of(testResumesAssignedUserId), standardResume.invitationSent(), standardResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(standardResume.id()).get();
			MongoUser expectedSideEffect3 = new MongoUser(anotherTestUser.id(), anotherTestUser.username(), anotherTestUser.password(), anotherTestUser.role(), testResumeId);
			MongoUser actualSideEffect3 = mongoUserRepository.findById(anotherTestUser.id()).get();
			Resume expectedSideEffect4 = new Resume(testResume2.id(), testResume2.name(), testResume2.addressing(), Set.of(), testResume2.invitationSent(), testResume2.isPublished());
			Resume actualSideEffect4 = resumeRepository.findById(testResume2.id()).get();
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
			assertEquals(expectedSideEffect3, actualSideEffect3);
			assertEquals(expectedSideEffect4, actualSideEffect4);
		}

	}

	@Nested
	@DisplayName("DELETE /api/admin/resumes/delete/{id}/")
	class deleteResume {

		@Test
		@DirtiesContext
		@DisplayName("...should throw 'Unauthorised' (401) if the user is not logged in")
		void deleteResume_shouldThrow401Unauthorised_ifUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResumeId + "/")
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
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResumeId + "/")
							.with(csrf()))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Not Found' (404) if user is an admin but the resume does not exist")
		void deleteResume_shouldThrow404NotFound_ifResumeDoesNotExist() throws Exception {
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResumeId + "/")
							.with(csrf()))
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume to delete does exist but one of the users who are assigned to it does not exist and thus cannot be unassigned from it")
		void deleteResume_shouldThrow422UnprocessableEntity_ifOneOfTheUsersAssignedDoesNotExist() throws Exception {
			// GIVEN
			Resume resumeWithNonExistingUser = new Resume("Some-valid-ID", "Company name", "Hello, company!", Set.of("Some-non-existing-user-id"), false, false);
			resumeRepository.save(resumeWithNonExistingUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + resumeWithNonExistingUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should throw 'Unprocessable Entity' (422) if the user is an admin and the resume to delete does exist but the standard resume does not exist and thus cannot be assigned to the users who were un-assigned from the deleted resume")
		void deleteResume_shouldThrow422UnprocessableEntity_ifReplacingResumeDoesNotExist() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			mongoUserRepository.save(testResumeAssignedUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResumeId + "/")
							.with(csrf()))
					.andExpect(status().isUnprocessableEntity());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = {"ADMIN"})
		@DisplayName("...should delete resume, re-assign it's associated users to a standard resume, return 'OK' (200) and the deleted resume if the user is an admin and the resume does exist")
		void deleteResume_shouldDeleteResumeAndReassignResumeToItsUsers_andReturn200OK_ifUserIsAdminAndResumeExists() throws Exception {
			// GIVEN
			resumeRepository.save(testResume);
			mongoUserRepository.save(testResumeAssignedUser);
			resumeRepository.save(standardResume);
			// WHEN
			mockMvc.perform(delete("/api/admin/resumes/delete/" + testResumeId + "/")
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
			Optional<Resume> actual = resumeRepository.findById(testResumeId);
			MongoUser expectedSideEffect1 = new MongoUser(testResumesAssignedUserId, testResumeAssignedUser.username(), testResumeAssignedUser.password(), testResumeAssignedUser.role(), standardResume.id());
			MongoUser actualSideEffect1 = mongoUserRepository.findById(testResumesAssignedUserId).get();
			Resume expectedSideEffect2 = new Resume(standardResume.id(), standardResume.name(), standardResume.addressing(), Set.of(testResumesAssignedUserId), standardResume.invitationSent(), standardResume.isPublished());
			Resume actualSideEffect2 = resumeRepository.findById(standardResume.id()).get();
			assertEquals(expected, actual);
			assertEquals(expectedSideEffect1, actualSideEffect1);
			assertEquals(expectedSideEffect2, actualSideEffect2);
		}

	}

}
