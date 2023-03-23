package com.glebabramov.backend.model;

public record MongoUserResponse(String id, String username, String role, String associatedResume) {
}
