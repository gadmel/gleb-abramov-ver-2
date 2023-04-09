package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUser;
import com.glebabramov.backend.model.Resume;
import com.glebabramov.backend.repository.MongoUserRepository;
import com.glebabramov.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
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

	public MongoUser userDoesExistById(String id, boolean secondaryRequest) {
		Optional<MongoUser> userToUpdate = mongoUserRepository.findById(id);
		if (userToUpdate.isEmpty()) {
			if (secondaryRequest) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because user with id " + id + " does not exist");
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
			}
		}
		return userToUpdate.get();
	}

	public MongoUser userDoesExistById(String id) {
		return this.userDoesExistById(id, false);
	}

	public boolean userMayBeDeleted(MongoUser user) {
		if (user.role().equals("ADMIN")) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot be deleted");
		}
		return true;
	}

	public Resume whatResumeUserMayAccess(Principal principal) {
		Optional<MongoUser> currentUser = mongoUserRepository.findByUsername(principal.getName());
		if (currentUser.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
		}
		String id = currentUser.get().associatedResume();
		return this.resumeDoesExistById(id);
	}

	public Resume resumeDoesExistById(String id, boolean secondaryRequest) {
		Optional<Resume> resume = resumeRepository.findById(id);
		if (resume.isEmpty()) {
			if (secondaryRequest) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Secondary condition not met, because resume with id " + id + " does not exist");
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
