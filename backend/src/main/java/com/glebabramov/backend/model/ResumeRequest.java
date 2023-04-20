package com.glebabramov.backend.model;
import java.util.Set;

public record ResumeRequest(String id, String name, String addressing, Set<String> userIds) {
}
