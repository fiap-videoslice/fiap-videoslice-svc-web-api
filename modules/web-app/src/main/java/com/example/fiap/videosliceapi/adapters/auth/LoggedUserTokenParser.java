package com.example.fiap.videosliceapi.adapters.auth;

import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.auth.UserGroup;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Integração com o autenticador externo da aplicação.
 * Protocolo para identificação do usuário logado: é esperado que o Autenticador insira nos headers da requisição
 * o IdToken do Cognito User Pool, sob a chave "IdentityToken"
 */
@Service
public class LoggedUserTokenParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggedUserTokenParser.class);

    public static final String HEADER_NAME = "IdentityToken";
    private static final String HEADER_NAME_LOWER = "identitytoken";

    private final JwtParser jwtParser;

    @Autowired
    public LoggedUserTokenParser(CognitoJwksApi cognitoJwksApi) {
        jwtParser = Jwts.parser()
                .keyLocator(new JwksKeyLocator(cognitoJwksApi))
                .build();
    }

    @VisibleForTesting
    LoggedUserTokenParser(CognitoJwksApi cognitoJwksApi, Clock expirationCheckClock) {
        jwtParser = Jwts.parser()
                .clock(expirationCheckClock)
                .keyLocator(new JwksKeyLocator(cognitoJwksApi))
                .build();
    }

    @NotNull
    public LoggedUser verifyLoggedUser(HttpHeaders headers) {
        String identityToken = headers.getFirst(HEADER_NAME);
        if (identityToken == null)
            identityToken = headers.getFirst(HEADER_NAME_LOWER);

        if (identityToken == null) {
            return new TokenBasedLoggedUser(false, null, null, null,
                    "IdentityToken is missing", null);
        }

        Jwt<?, ?> jwt;
        try {
            jwt = jwtParser.parse(identityToken);
        } catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            LOGGER.warn("Erro validando IdentityToken: {} -- {}", e, identityToken);
            return new TokenBasedLoggedUser(false, null, null, null,
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
                claims.get("name", String.class),
                claims.get("email", String.class),
                group,
                null,
                identityToken);
    }

    private class TokenBasedLoggedUser implements LoggedUser {
        private final boolean authenticated;
        private final String name;
        private final String email;
        private final UserGroup group;
        private final String authError;
        private final String token;

        private TokenBasedLoggedUser(boolean authenticated,
                                     String name, String email, UserGroup group,
                                     String authError, String token) {
            this.authenticated = authenticated;
            this.name = name;
            this.email = email;
            this.group = group;
            this.authError = authError;
            this.token = token;
        }

        public boolean authenticated() {
            return authenticated;
        }

        public String getName() {
            if (!authenticated)
                throw new IllegalStateException("Usuario não autenticado");
            return name;
        }

        @Override
        public String getEmail() {
            if (!authenticated)
                throw new IllegalStateException("Usuario não autenticado");
            return email;
        }

        public UserGroup getGroup() {
            if (!authenticated)
                throw new IllegalStateException("Usuario não autenticado");
            return group;
        }

        @Override
        public String identityToken() {
            if (!authenticated)
                throw new IllegalStateException("Usuario não autenticado");
            return token;
        }

        @Override
        public String authError() {
            return authError;
        }
    }
}
