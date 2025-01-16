package com.example.fiap.videosliceapi.adapters.auth;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

public interface LoggedUserTokenParser {
    @NotNull LoggedUser verifyLoggedUser(HttpHeaders headers);
}
