package com.glebabramov.backend.service;

import com.glebabramov.backend.model.MongoUserAuthRequest;
import com.glebabramov.backend.model.MongoUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ValidationService {

	public void validateMongoUserAuthRequest(MongoUserAuthRequest user) {
		if (user.username() == null || user.username().length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}
		if (user.password() == null || user.password().length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}
	}

	public void validateMongoUserRequest(MongoUserRequest user) throws ResponseStatusException {
		if (user.id() == null || user.id().length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
		}
		if (user.username() == null || user.username().length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}
		if (user.associatedResume() == null || user.associatedResume().length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Associated resume is required");
		}
	}

	public void validateIdRequest(String id) throws ResponseStatusException {
		if (id == null || id.length() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is required");
		}
	}

}
