package com.glebabramov.backend.model;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record MongoUser(String id, String username, String password, String role, String associatedResume) {

	private static final String STANDARD_RESUME_ID = "8c687299-9ab7-4f68-8fd9-3de3c521227e";

	public MongoUserResponse toResponseDTO() {
		return new MongoUserResponse(id, username, role, associatedResume);
	}

	public MongoUser updateWithRequestDTO(MongoUserRequest request) {
		return new MongoUser(request.id(), request.username(), password, role, request.associatedResume());
	}

	public MongoUser reassignResume(String resumeId) {
		return new MongoUser(id, username, password, role, resumeId);
	}

	public MongoUser unassignResume() {
		return new MongoUser(id, username, password, role, STANDARD_RESUME_ID);
	}
}
