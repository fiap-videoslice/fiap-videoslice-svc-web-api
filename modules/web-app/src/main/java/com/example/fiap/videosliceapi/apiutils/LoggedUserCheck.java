package com.example.fiap.videosliceapi.apiutils;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.adapters.auth.LoggedUser;
import org.springframework.http.HttpHeaders;

public class LoggedUserCheck {
    private LoggedUserCheck() {
    }

    public static LoggedUser ensureLoggedUser(LoggedUserTokenParser loggedUserTokenParser, HttpHeaders headers) throws NotAuthenticatedException {
        LoggedUser user = loggedUserTokenParser.verifyLoggedUser(headers);
        if (!user.authenticated())
            throw new NotAuthenticatedException("User is not authenticated. " + user.authError());

        return user;
    }

    public static class NotAuthenticatedException extends Exception {
        public NotAuthenticatedException(String message) {
            super(message);
        }
    }
}
