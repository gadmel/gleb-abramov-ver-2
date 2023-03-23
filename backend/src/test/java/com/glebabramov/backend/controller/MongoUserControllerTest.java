package com.glebabramov.backend.controller;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.repository.MongoUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class MongoUserControllerTest {
	@Autowired
	MockMvc mockMvc;
	@Autowired
	MongoUserRepository mongoUserRepository;

	Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

	// Mock of the principal interface
	Principal mockedPrincipal = mock(Principal.class);
	// Test data
	MongoUser adminUser;
	MongoUser basicUser;

	// Integration tests for the MongoUserController incl.:
	// GET /api/users/current/,
	// POST /api/users/login,
	// POST /api/users/register

	@BeforeEach
	void setUp() {
		adminUser = new MongoUser("Some ID", "Test admin", encoder.encode("admin"), "ADMIN", "company");
		basicUser = new MongoUser("Another ID", "Test user", encoder.encode("basic"), "BASIC", "company");
		when(mockedPrincipal.getName()).thenReturn(adminUser.username());
	}

	@Nested
	@DisplayName("GET /api/users/current/ a.k.a. useAuth()-Endpoint")
	class getCurrentUser {

		@Test
		@DirtiesContext
		@DisplayName("... should return 'Unauthorized' (401) if the user is not logged in (not authenticated)")
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
			MongoUser expected = new MongoUser(adminUser.id(), adminUser.username(), encoder.encode(adminUser.password()), adminUser.role(), adminUser.associatedResume());
			mongoUserRepository.save(expected);

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
		@DisplayName("... should return 'Unauthorized' (401) if the user with respective password does not exist")
		void login_shouldReturnUnauthorised401_ifUserWithRespectivePasswordDoesNotExist() throws Exception {
			mockMvc.perform(post("/api/users/login/")
							.with(httpBasic(adminUser.username(), adminUser.password()))
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
			MongoUser expected = new MongoUser(adminUser.id(), adminUser.username(), encoder.encode(adminUser.password()), adminUser.role(), adminUser.associatedResume());
			mongoUserRepository.save(expected);
			// THEN
			mockMvc.perform(post("/api/users/login/")
							.with(httpBasic(adminUser.username(), adminUser.password()))
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
	@DisplayName("POST /api/users/register/ - Admin only")
	class register {


		@Test
		@DirtiesContext
		@WithMockUser(username = "Test user", roles = "BASIC")
		@DisplayName("... should return 'Forbidden' (403) if the user is not an admin")
		void register_shouldReturnForbidden403_ifTheUserIsNotAnAdmin() throws Exception {
			// GIVEN
			MongoUser userInDB = new MongoUser(basicUser.id(), basicUser.username(), encoder.encode(basicUser.password()), basicUser.role(), basicUser.associatedResume());
			mongoUserRepository.save(userInDB);
			// WHEN
			mockMvc.perform(post("/api/users/register/")
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
		void register_shouldReturnCreated201_ifTheUserRegisteringIsAnAdmin() throws Exception {
			// GIVEN
			MongoUser userInDB = new MongoUser(adminUser.id(), adminUser.username(), encoder.encode(adminUser.password()), adminUser.role(), adminUser.associatedResume());
			mongoUserRepository.save(userInDB);
			//WHEN
			mockMvc.perform(post("/api/users/register/")
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
			//THEN
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("...should return 'Conflict' (409) if the user already exists")
		void register_shouldReturnConflict409_ifTheUserAlreadyExists() throws Exception {
			// GIVEN
			MongoUser userInDB = new MongoUser(adminUser.id(), adminUser.username(), encoder.encode(adminUser.password()), adminUser.role(), adminUser.associatedResume());
			mongoUserRepository.save(userInDB);
			//WHEN
			mockMvc.perform(post("/api/users/register/")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
										"username": "Test admin",
										"password": "Test password"
									}
									""")
					)
					.andExpect(status().isConflict());
			//THEN
		}

		@Test
		@DirtiesContext
		@WithMockUser(username = "Test admin", roles = "ADMIN")
		@DisplayName("...should return 'Bad Request' (400) if the username or password is missing")
		void register_shouldReturnBadRequest400_ifTheUsernameOrPasswordIsMissing() throws Exception {
			// GIVEN
			MongoUser userInDB = new MongoUser(adminUser.id(), adminUser.username(), encoder.encode(adminUser.password()), adminUser.role(), adminUser.associatedResume());
			mongoUserRepository.save(userInDB);
			//WHEN
			mockMvc.perform(post("/api/users/register/")
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

}
