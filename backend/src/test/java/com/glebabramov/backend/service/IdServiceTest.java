package com.glebabramov.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DisplayName("IdService")
class IdServiceTest {
	@Test
	@DisplayName("... should generate a random id")
	void generateId() {
		IdService idService = new IdService();
		String id = idService.generateId();
		assertNotNull(id);
	}
}