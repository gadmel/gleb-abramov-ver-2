package com.glebabramov.backend.model;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record MongoUser(String id, String username, String password, String role, String associatedResume) {

	public MongoUserResponse toResponseDTO() {
		return new MongoUserResponse(id, username, role, associatedResume);
	}

	public MongoUser updateWithRequestDTO(MongoUserRequest request) {
		return new MongoUser(request.id(), request.username(), password, role, request.associatedResume());
	}


}
