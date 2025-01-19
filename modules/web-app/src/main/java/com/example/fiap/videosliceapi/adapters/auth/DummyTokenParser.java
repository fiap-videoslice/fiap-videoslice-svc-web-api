package com.example.fiap.videosliceapi.adapters.auth;

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

    private static final String HEADER_NAME = "Authorization";

    @Override
    public @NotNull LoggedUser verifyLoggedUser(HttpHeaders headers) {
        String header = headers.getFirst(HEADER_NAME);

        if (header == null) {
            return new DummyUser(false, null, null, null, null, null,
                    HEADER_NAME + " header is missing");
        }

        String[] parts = header.split(" ");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("Dummy")) {
            return new DummyUser(false, null, null, null, null, null,
                    HEADER_NAME + " token has invalid format. Authorization: Dummy User1|User2|Admin");
        }

        String identityToken = parts[1];

        if (identityToken.equals("User1")) {
            return new DummyUser(true, "Test-User-1", "Test User 1", "user1@fiap.example.com", UserGroup.User, identityToken, null);
        } else if (identityToken.equals("User2")) {
            return new DummyUser(true, "Test-User-2", "Test User 2", "user2@fiap.example.com", UserGroup.User, identityToken, null);
        } else if (identityToken.equals("Admin")) {
            return new DummyUser(true, "Test-User-Admin-1", "Test Admin", "admin@fiap.example.com", UserGroup.Admin, identityToken, null);
        } else {
            return new DummyUser(false, null, null, null, null, identityToken,
                    HEADER_NAME + " token is invalid. Authorization: Dummy User1|User2|Admin");
        }
    }

    public record DummyUser(boolean authenticated,
                            String userId,
                            String name,
                            String email,
                            UserGroup group,
                            String idToken,
                            String authError)
            implements LoggedUser {

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getUserId() {
            return userId;
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
