package com.glebabramov.backend.model;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record MongoUser(String id, String username, String password, String role, String associatedResume) {
}