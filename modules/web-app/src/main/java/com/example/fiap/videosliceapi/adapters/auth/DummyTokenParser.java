package com.example.fiap.videosliceapi.adapters.auth;

import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.auth.UserGroup;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

/**
 * An alternative implementation of the Token validation to be used during development.
 * It does not require a real token or valid signature.
 * <br />
 * While using this implementation, send one of the following values in the IdentityToken header:
 * IdentityToken: User1
 * IdentityToken: User2
 * IdentityToken: Admin
 * <br />
 * Each will map directly to the identity of one of the test users. Any other value results in error
 */
public class DummyTokenParser implements LoggedUserTokenParser {
    public static final String ENABLE_DUMMY_TOKENS_ENV_KEY = "videosliceapi.auth.development-dummy-tokens.enabled";

    public static final String HEADER_NAME = "IdentityToken";
    private static final String HEADER_NAME_LOWER = "identitytoken";

    @Override
    public @NotNull LoggedUser verifyLoggedUser(HttpHeaders headers) {
        String identityToken = headers.getFirst(HEADER_NAME);
        if (identityToken == null)
            identityToken = headers.getFirst(HEADER_NAME_LOWER);

        if (identityToken == null) {
            return new DummyUser(false, null, null, null, null,
                    "IdentityToken is missing. Check DummyTokenParser for valid values");
        }

        if (identityToken.equals("User1")) {
            return new DummyUser(true, "Test User 1", "user1@fiap.example.com", UserGroup.User, identityToken, null);
        } else if (identityToken.equals("User2")) {
            return new DummyUser(true, "Test User 2", "user2@fiap.example.com", UserGroup.User, identityToken, null);
        } else if (identityToken.equals("Admin")) {
            return new DummyUser(true, "Test Admin", "admin@fiap.example.com", UserGroup.Admin, identityToken, null);
        } else {
            return new DummyUser(false, null, null, null, identityToken,
                    "Invalid IdentityToken: " + identityToken + ". Check DummyTokenParser for valid values");
        }
    }

    public record DummyUser(boolean authenticated,
                            String name,
                            String email,
                            UserGroup group,
                            String identityToken,
                            String authError)
            implements LoggedUser {

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public UserGroup getGroup() {
            return group;
        }
    }
}
