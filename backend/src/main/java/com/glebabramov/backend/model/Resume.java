package com.glebabramov.backend.model;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("resumes")
public record Resume(String id, String name, String userId, boolean invitationSent, boolean isPublished) {

	public Resume assignToUser(String userId) {
		return new Resume(id, name, userId, invitationSent, isPublished);
	}

}
