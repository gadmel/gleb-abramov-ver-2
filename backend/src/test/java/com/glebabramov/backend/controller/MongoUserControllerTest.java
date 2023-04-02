package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.security.Principal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class MongoUserControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	MongoUserRepository mongoUserRepository;
	@Autowired
	ResumeRepository resumeRepository;
	Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

	Principal mockedPrincipal = mock(Principal.class);

	MongoUser adminUser;
	MongoUser basicUser;
	String rawPassword = "password";
	Resume basicUserResume;

	@BeforeEach
	void setUp() {
		adminUser = new MongoUser("Some-ID", "Test admin", encoder.encode(rawPassword), "ADMIN", "company");
		basicUser = new MongoUser("Another-ID", "Test user", encoder.encode(rawPassword), "BASIC", "company");
		basicUserResume = new Resume("Some-ID", "Test resume", basicUser.id(), false, false);
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
	}

	@Nested
	@DisplayName("GET /api/users/current/ a.k.a. useAuth()-Endpoint")
	class getCurrentUser {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorised' (401) if the user is not logged in (not authenticated)")
		void getCurrentUser_shouldReturnUnauthorised401_ifUserIsNotAuthenticated() throws Exception {
			mockMvc.perform(get("/api/users/current/"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@WithMockUser(username = "Test admin")
		@DirtiesContext
		@DisplayName("... should return 'OK' (200) and the user's data without the password if the user is logged in (authenticated)")
		void getCurrentUser_shouldReturnOK200_andUserWithoutPassword_ifUserIsAuthenticated() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// THEN
			mockMvc.perform(get("/api/users/current/"))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"username": "Test admin",
								"role": "ADMIN",
								"associatedResume": "company"
							}
							"""
					))
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andExpect(jsonPath("$.password").doesNotExist());
		}

	}

	@Nested
	@DisplayName("POST /api/users/login/")
	class login {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorised' (401) if the user with respective password does not exist")
		void login_shouldReturnUnauthorised401_ifUserWithRespectivePasswordDoesNotExist() throws Exception {
			mockMvc.perform(post("/api/users/login/")
							.with(httpBasic(adminUser.username(), rawPassword))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@DisplayName("...should return user object without password (200) if the user with respective password exists")
		void login_shouldReturnUserObjectWithoutPassword_ifUserWithRespectivePasswordExists() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// THEN
			mockMvc.perform(post("/api/users/login/")
							.with(httpBasic(adminUser.username(), rawPassword))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"username": "Test admin",
								"role": "ADMIN",
								"associatedResume": "company"
							}
							"""
					))
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andExpect(jsonPath("$.password").doesNotExist());
		}

	}

	@Nested
	@DisplayName("POST /api/admin/users/register/ - Admin only")
	class register {

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = "BASIC")
		@DisplayName("... should return 'Forbidden' (403) if the user registering is not an admin")
		void register_shouldReturnForbidden403_ifTheUserIsNotAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(post("/api/admin/users/register/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test user",
										"password": "Test password"
									}
									""")
					)
					.andExpect(status().isForbidden());

		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("...should return 'Created' (201) if the user registering is an admin")
		void register_shouldReturnCreated201_ifTheUserIsAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			mockMvc.perform(post("/api/admin/users/register/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test user",
										"password": "Test password"
									}
									""")
					)
					.andExpect(status().isCreated());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("...should return 'Conflict' (409) if the user already exists")
		void register_shouldReturnConflict409_ifTheUserAlreadyExists() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			//WHEN
			mockMvc.perform(post("/api/admin/users/register/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test user",
										"password": "Test password"
									}
									""")
					)
					.andExpect(status().isConflict());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("...should return 'Bad Request' (400) if the username or password is missing")
		void register_shouldReturnBadRequest400_ifTheUsernameOrPasswordIsMissing() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			//WHEN
			mockMvc.perform(post("/api/admin/users/register/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test admin"
									}
									""")
					)
					.andExpect(status().isBadRequest());
		}

	}

	@Nested
	@DisplayName("GET /api/admin/users/ - Admin only")
	class getAllUsers {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorised' (401) if the user is not logged in")
		void getAllUsers_shouldReturnUnauthorised401_ifTheUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(get("/api/admin/users/")
							.with(csrf()))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = "BASIC")
		@DisplayName("... should return 'Forbidden' (403) if the user is not an admin")
		void getAllUsers_shouldReturnForbidden403_ifTheUserIsNotAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(get("/api/admin/users/")
							.with(csrf()))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'OK' (200) and a list of users if the user is an admin")
		void getAllUsers_shouldReturnOK200_andAListOfUsers_ifTheUserIsAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(get("/api/admin/users/")
							.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							[
								{
									"username": "Test admin",
									"role": "ADMIN",
									"associatedResume": "company"
								},
								{
									"username": "Test user",
									"role": "BASIC",
									"associatedResume": "company"
								}
							]
							"""
					))
					.andExpect(jsonPath("$[0].id").isNotEmpty())
					.andExpect(jsonPath("$[0].password").doesNotExist())
					.andExpect(jsonPath("$[1].id").isNotEmpty())
					.andExpect(jsonPath("$[1].password").doesNotExist());
		}

	}

	@Nested
	@DisplayName("PUT /api/admin/users/ - Admin only")
	class updateUser {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorised' (401) if the user is not logged in")
		void updateUser_shouldReturnUnauthorised401_ifTheUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(put("/api/admin/users/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = "BASIC")
		@DisplayName("... should return 'Forbidden' (403) if the user is not an admin")
		void updateUser_shouldReturnForbidden403_ifTheUserIsNotAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Bad Request' (400) if the user ID, username or associated resume is missing or blank")
		void updateUser_shouldReturnBadRequest400_ifTheUserIDIsInvalid() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isBadRequest());

			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "",
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isBadRequest());

		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Bad Request' (400) if the user username is missing or blank")
		void updateUser_shouldReturnBadRequest400_ifTheUserUsernameIsInvalid() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isBadRequest());

			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isBadRequest());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Bad Request' (400) if the user associated resume is missing or blank")
		void updateUser_shouldReturnBadRequest400_ifTheUserAssociatedResumeIsInvalid() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user"
									}
									""")
					)
					.andExpect(status().isBadRequest());

			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": ""
									}
									""")
					)
					.andExpect(status().isBadRequest());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Not Found' (404) if the user to update is not found by ID")
		void updateUser_shouldReturnNotFound404_ifTheUserToUpdateIsNotFoundByID() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Not Found' (404) if the user associated resume is not found by ID")
		void updateUser_shouldReturnNotFound404_ifTheUserAssociatedResumeIsNotFoundByID() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": "company"
									}
									""")
					)
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should update the user and the associated resume, return 'OK' (200) and the updated user if the user is updated successfully")
		void updateUser_shouldUpdateTheUserAndTheAssociatedResumeAndReturnOK200AndTheUpdatedUser_ifTheUserIsUpdatedSuccessfully() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			resumeRepository.save(basicUserResume);
			// WHEN
			mockMvc.perform(put("/api/admin/users/update/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"id": "Some-ID",
										"username": "Test user",
										"associatedResume": "Some-ID"
									}
									""")
					)
					// THEN
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value("Some-ID"))
					.andExpect(jsonPath("$.username").value("Test user"))
					.andExpect(jsonPath("$.associatedResume").value("Some-ID"));
		}


	}

	@Nested
	@DisplayName("DELETE /api/admin/users/delete/{id}/ - Admin only")
	class deleteUser {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorised' (401) if the user is not logged in")
		void deleteUser_shouldReturnUnauthorised401_ifTheUserIsNotLoggedIn() throws Exception {
			mockMvc.perform(delete("/api/admin/users/delete/" + basicUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = "BASIC")
		@DisplayName("... should return 'Forbidden' (403) if the user is not an admin")
		void deleteUser_shouldReturnForbidden403_ifTheUserIsNotAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/users/delete/" + basicUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isForbidden());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'Not Found' (404) if the user does not exist")
		void deleteUser_shouldReturnBadRequest400_ifTheUserIdIsInvalid() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/users/delete/" + basicUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isNotFound());
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("... should return 'OK' (200) and the DTO of the deleted user and delete the user if the user exists and the user deleting is an admin")
		void deleteUser_shouldReturnOK200_andTheDTOOfTheDeletedUser_andDeleteTheUser_ifTheUserExistsAndTheUserDeletingIsAnAdmin() throws Exception {
			// GIVEN
			mongoUserRepository.save(adminUser);
			mongoUserRepository.save(basicUser);
			// WHEN
			mockMvc.perform(delete("/api/admin/users/delete/" + basicUser.id() + "/")
							.with(csrf()))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{
								"username": "Test user",
								"role": "BASIC",
								"associatedResume": "company"
							}
							"""
					))
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andExpect(jsonPath("$.password").doesNotExist());
			// THEN
			Assertions.assertFalse(mongoUserRepository.existsById(basicUser.id()));
		}
	}

}
