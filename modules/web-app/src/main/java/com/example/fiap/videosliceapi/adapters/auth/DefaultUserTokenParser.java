package com.example.fiap.videosliceapi.adapters.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Integração com o autenticador externo da aplicação.
 * É esperado que o IdToken do CognitoUserPool esteja presente no cabeçalho 'Authorization: Bearer ......Token............'
 * <br />
 * Token obtido normalmente através de um endpoint específico configurado no API Gateway (ex: /token)
 */
public class DefaultUserTokenParser implements LoggedUserTokenParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserTokenParser.class);

    private static final Pattern HEADER_VALIDATION = Pattern.compile("^\\s*[Bb][Ee][Aa][Rr][Ee][Rr]\\s+([a-zA-Z0-9+/=_-]+\\.[a-zA-Z0-9+/=_-]+\\.[a-zA-Z0-9+/=_-]+)\\s*$");

    private static final String HEADER_NAME = "Authorization";

    private final JwtParser jwtParser;

    public DefaultUserTokenParser(CognitoJwksApi cognitoJwksApi) {
        jwtParser = Jwts.parser()
                .keyLocator(new JwksKeyLocator(cognitoJwksApi))
                .build();
    }

    @VisibleForTesting
    DefaultUserTokenParser(CognitoJwksApi cognitoJwksApi, Clock expirationCheckClock) {
        jwtParser = Jwts.parser()
                .clock(expirationCheckClock)
                .keyLocator(new JwksKeyLocator(cognitoJwksApi))
                .build();
    }

    @Override
    @NotNull
    public LoggedUser verifyLoggedUser(HttpHeaders headers) {
        String authHeader = headers.getFirst(HEADER_NAME);

        if (authHeader == null) {
            return new TokenBasedLoggedUser(false, null, null, null, null,
                    HEADER_NAME + " header is missing", null);
        }

        String idToken;
        var matcher = HEADER_VALIDATION.matcher(authHeader);
        if (matcher.matches()) {
            idToken = matcher.group(1);
        } else {
            return new TokenBasedLoggedUser(false, null, null, null, null,
                    HEADER_NAME + " header is invalid", null);
        }

        Jwt<?, ?> jwt;
        try {
            jwt = jwtParser.parse(idToken);
        } catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            LOGGER.warn("Erro validando IdToken: {} -- {}", e, idToken);
            return new TokenBasedLoggedUser(false, null, null, null, null,
                    "Erro ao validar IdToken: " + e.getMessage(), null);
        }

        Claims claims = (Claims) jwt.getPayload();

        List<?> groups = claims.get("cognito:groups", List.class);
        UserGroup group = null;
        if (groups != null && !groups.isEmpty() && groups.getFirst() instanceof String groupStr) {
            try {
                group = UserGroup.valueOf(groupStr);
            } catch (IllegalArgumentException ignored) {
                // Nenhum grupo conhecido para a aplicação
            }
        }

        return new TokenBasedLoggedUser(true,
                claims.get("sub", String.class),
                claims.get("name", String.class),
                claims.get("email", String.class),
                group,
                null,
                idToken);
    }

    private class TokenBasedLoggedUser implements LoggedUser {
        private final boolean authenticated;
        private final String id;
        private final String name;
        private final String email;
        private final UserGroup group;
        private final String authError;
        private final String token;

        private TokenBasedLoggedUser(boolean authenticated,
                                     String id, String name, String email, UserGroup group,
                                     String authError, String token) {
            this.authenticated = authenticated;
            this.id = id;
            this.name = name;
            this.email = email;
            this.group = group;
            this.authError = authError;
            this.token = token;
        }

        public boolean authenticated() {
            return authenticated;
        }

        public String getUserId() {
            if (!authenticated)
                throw new IllegalStateException("User not authenticated");
            return id;
        }
        
        public String getName() {
            if (!authenticated)
                throw new IllegalStateException("User not authenticated");
            return name;
        }

        @Override
        public String getEmail() {
            if (!authenticated)
                throw new IllegalStateException("User not authenticated");
            return email;
        }

        public UserGroup getGroup() {
            if (!authenticated)
                throw new IllegalStateException("User not authenticated");
            return group;
        }

        @Override
        public String idToken() {
            if (!authenticated)
                throw new IllegalStateException("User not authenticated");
            return token;
        }

        @Override
        public String authError() {
            return authError;
        }
    }
}
