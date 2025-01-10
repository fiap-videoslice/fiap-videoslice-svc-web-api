package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.auth.UserGroup;

public interface UserRegistry {
    void registerUser(String name, String email, UserGroup group, String password);
}
