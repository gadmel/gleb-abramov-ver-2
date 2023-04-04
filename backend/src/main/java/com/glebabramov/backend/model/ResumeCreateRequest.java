package com.glebabramov.backend.model;

import java.util.Set;

public record ResumeCreateRequest(String name, Set<String> userIds) {
}
