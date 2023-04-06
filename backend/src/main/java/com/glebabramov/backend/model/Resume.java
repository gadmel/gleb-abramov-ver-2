package com.glebabramov.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Document("resumes")
public record Resume(String id, String name, Set<String> userIds, boolean invitationSent, boolean isPublished) {

	public Resume assignToUser(String userId) {
		Set<String> newSet = Stream
				.concat(userIds().stream(), Stream.of(userId))
				.collect(Collectors.toSet());
		return new Resume(id(), name(), newSet, invitationSent(), isPublished());
	}

	public Resume unassignFromUser(String userId) {
		Set<String> newUserIds = userIds().stream()
				.filter(id -> !id.equals(userId))
				.collect(Collectors.toSet());
		return new Resume(id(), name(), newUserIds, invitationSent(), isPublished());
	}

}
