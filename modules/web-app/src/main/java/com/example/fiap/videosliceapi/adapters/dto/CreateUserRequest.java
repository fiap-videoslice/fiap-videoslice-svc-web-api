package com.example.fiap.videosliceapi.adapters.dto;

import com.example.fiap.videosliceapi.adapters.auth.UserGroup;

public record CreateUserRequest(
        String name,
        String email,
        String group,
        String password
) {
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be null or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank.");
        }
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Group must not be null or blank.");
        }

        UserGroup.valueOf(group); // Throws if unknown

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank.");
        }
    }
}
