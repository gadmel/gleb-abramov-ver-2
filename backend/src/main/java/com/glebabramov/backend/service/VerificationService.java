package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {

	private final MongoUserRepository mongoUserRepository;
	private final ResumeRepository resumeRepository;

	public boolean userDoesNotExistByUsername(String username) {
		if (mongoUserRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}
		return true;
	}

	public MongoUser userDoesExistById(String id) {
		Optional<MongoUser> userToUpdate = mongoUserRepository.findById(id);
		if (userToUpdate.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
		}
		return userToUpdate.get();
	}

	public Resume resumeDoesExistById(String id, boolean secondaryRequest) {
		Optional<Resume> resume = resumeRepository.findById(id);
		if (resume.isEmpty()) {
			if (secondaryRequest) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated resume does not exist");
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found");
			}
		}
		return resume.get();
	}

	public Resume resumeDoesExistById(String id) {
		return this.resumeDoesExistById(id, false);
	}

}
