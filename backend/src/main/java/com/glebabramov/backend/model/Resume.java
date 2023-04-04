package com.glebabramov.backend.model;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Document("resumes")
public record Resume(String id, String name, Set<String> userIds, boolean invitationSent, boolean isPublished) {

	public Resume assignToUser(String userId) {
		this.userIds().add(userId);
		return this;
	}

	public Resume unassignFromUser(String userId) {
		this.userIds().remove(userId);
		return this;
	}

}
