package com.glebabramov.backend.model;

public record MongoUserAuthRequest(String username, String password) {
}
