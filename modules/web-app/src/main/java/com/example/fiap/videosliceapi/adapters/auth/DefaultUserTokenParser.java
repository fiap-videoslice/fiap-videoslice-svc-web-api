package com.example.fiap.videosliceapi.adapters.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * Integração com o autenticador externo da aplicação.
 * Protocolo para identificação do usuário logado: é esperado que o Autenticador insira nos headers da requisição
 * o IdToken do Cognito User Pool, sob a chave "IdentityToken"
 */
public class DefaultUserTokenParser implements LoggedUserTokenParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserTokenParser.class);

    public static final String HEADER_NAME = "IdentityToken";
    private static final String HEADER_NAME_LOWER = "identitytoken";

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
        String identityToken = headers.getFirst(HEADER_NAME);
        if (identityToken == null)
            identityToken = headers.getFirst(HEADER_NAME_LOWER);

        if (identityToken == null) {
            return new TokenBasedLoggedUser(false, null, null, null, null,
                    "IdentityToken is missing", null);
        }

        Jwt<?, ?> jwt;
        try {
            jwt = jwtParser.parse(identityToken);
        } catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            LOGGER.warn("Erro validando IdentityToken: {} -- {}", e, identityToken);
            return new TokenBasedLoggedUser(false, null, null, null, null,
                    "Erro ao validar IdentityToken: " + e.getMessage(), null);
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
                identityToken);
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
        public String identityToken() {
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
