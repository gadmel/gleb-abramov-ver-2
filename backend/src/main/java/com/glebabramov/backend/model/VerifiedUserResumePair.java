package com.glebabramov.backend.model;

public record VerifiedUserResumePair(MongoUser user, Resume resume) {
}
