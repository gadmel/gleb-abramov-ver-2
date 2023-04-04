package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.MongoUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
class AuthorisationServiceTest {

	MongoUserDetailsService mongoUserDetailsService = mock(MongoUserDetailsService.class);
	Principal mockedPrincipalBasicUser = mock(Principal.class);
	Principal mockedPrincipalAuthorisedUser = mock(Principal.class);

	@Autowired
	AuthorisationService authorisationService;

	final String ROLE = "Some role to restrict by";
	final String UNAUTHORISED_ROLE = "Some irrelevant role";
	String RESTRICTION = "fulfill this action";
	MongoUser basicUser = new MongoUser("Some-ID", "Basic user", "Test-password", UNAUTHORISED_ROLE, "associatedResumeId");
	MongoUserResponse basicUserResponse = new MongoUserResponse(basicUser.id(), basicUser.username(), basicUser.role(), basicUser.associatedResume());
	User basicUserDetails = new User(basicUser.username(), basicUser.password(), List.of(new SimpleGrantedAuthority(basicUser.role())));
	MongoUser authorisedUser = new MongoUser("Some-ID-2", "Authorised user", "Test-password", ROLE, "associatedResumeId");
	MongoUserResponse authorisedUserResponse = new MongoUserResponse(authorisedUser.id(), authorisedUser.username(), authorisedUser.role(), authorisedUser.associatedResume());
	User authorisedUserDetails = new User(authorisedUser.username(), authorisedUser.password(), List.of(new SimpleGrantedAuthority(authorisedUser.role())));
	ResponseStatusException userNotLoggedInException = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
	ResponseStatusException userNotAuthorisedException = new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to " + RESTRICTION);

	@BeforeEach
	void setUp() {
		authorisationService = new AuthorisationService(mongoUserDetailsService);
		when(mockedPrincipalBasicUser.getName()).thenReturn(basicUser.username());
		when(mockedPrincipalAuthorisedUser.getName()).thenReturn(authorisedUser.username());
		when(mongoUserDetailsService.loadUserByUsername(basicUser.username())).thenReturn(basicUserDetails);
		when(mongoUserDetailsService.loadUserByUsername(authorisedUser.username())).thenReturn(authorisedUserDetails);
		when(mongoUserDetailsService.getCurrentUser(mockedPrincipalBasicUser)).thenReturn(basicUserResponse);
		when(mongoUserDetailsService.getCurrentUser(mockedPrincipalAuthorisedUser)).thenReturn(authorisedUserResponse);
	}

	@Nested
	@DisplayName("isAuthorised()")
	class isAuthorised {

		@Test
		@DisplayName("... should throw 'Unauthorized' (401) if user is not logged in")
		void isAuthorised_shouldThrow401Unauthorized_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = userNotLoggedInException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> authorisationService.isAuthorised(null));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@DisplayName("... should not throw any exception if user is logged in")
		void isAuthorised_shouldNotThrowAnyException_ifUserIsLoggedIn() {
			assertDoesNotThrow(() -> authorisationService.isAuthorised(mockedPrincipalBasicUser));
		}

	}

	@Nested
	@DisplayName("isAuthorisedByRole()")
	class isAuthorisedByRole {
		@Test
		@DisplayName("... should throw 'Unauthorized' (401) if user is not logged in")
		void isAuthorisedByRole_shouldThrow401Unauthorized_ifUserIsNotLoggedIn() {
			//WHEN
			ResponseStatusException expected = userNotLoggedInException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> authorisationService.isAuthorisedByRole(ROLE, RESTRICTION, null));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@WithMockUser(username = "Basic user", roles = UNAUTHORISED_ROLE)
		@DisplayName("... should throw 'Forbidden' (403) if user is logged in but not authorised to fulfil the action")
		void isAuthorisedByRole_shouldThrow403Forbidden_ifUserIsLoggedInButNotAuthorised() {
			//WHEN
			ResponseStatusException expected = userNotAuthorisedException;
			ResponseStatusException actual = assertThrows(ResponseStatusException.class, () -> authorisationService.isAuthorisedByRole(ROLE, RESTRICTION, mockedPrincipalBasicUser));
			//THEN
			assertEquals(expected.getClass(), actual.getClass());
			assertEquals(expected.getMessage(), actual.getMessage());
		}

		@Test
		@WithMockUser(username = "Authorised user", roles = ROLE)
		@DisplayName("... should not throw any exception if user is logged in and authorised to fulfil the action")
		void isAuthorisedByRole_shouldNotThrowAnyException_ifUserIsLoggedInAndAuthorised() {
			//WHEN
			assertDoesNotThrow(() -> authorisationService.isAuthorisedByRole(ROLE, RESTRICTION, mockedPrincipalAuthorisedUser));
		}

	}

}