package com.glebabramov.backend.model;

import java.util.Set;

public record ResumeCreateRequest(String name, String addressing, Set<String> userIds) {
}
