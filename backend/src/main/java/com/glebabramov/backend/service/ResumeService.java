package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUserResponse;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.model.ResumeCreateRequest;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequiredArgsConstructor
@Service
public class ResumeService {
	private final ResumeRepository repository;
	private final MongoUserDetailsService mongoUserDetailsService;
	private final IdService idService;
	private static final String ADMIN_ROLE = "ADMIN";

	public Resume createResume(ResumeCreateRequest resume, Principal principal) {
		MongoUserResponse currentUser = mongoUserDetailsService.getCurrentUser(principal);

		if (!currentUser.role().equals(ADMIN_ROLE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to create resumes");
		}

		Resume newResume = new Resume(idService.generateId(), resume.name(), resume.userId(), false, false);
		return repository.save(newResume);
	}
}
