package com.glebabramov.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Nested
@DisplayName("GET /api/csrf/")
class CsrfControllerTest {
	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("... should return 200 OK and a cookie named XSRF-TOKEN and a string 'CSRF Secured'")
	void getCsrf_shouldReturn200AndCookie() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/csrf/"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(content().string("CSRF Secured"))
		;
	}
}
