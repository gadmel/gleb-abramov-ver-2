package com.glebabramov.backend.model;

public record MongoUserRequest(String id, String username, String associatedResume) {
}
