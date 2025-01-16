package com.example.fiap.videosliceapi.adapters.auth;

/**
 * Representação de um usuário autenticado no sistema.
 * É responsabilidade das camadas mais externas interagir com o mecanismo de autenticação e autorização, fornecendo
 * instâncias válidas para a camada de use cases
 */
public interface LoggedUser {
    boolean authenticated();

    String getName();
    String getEmail();
    String getUserId();

    UserGroup getGroup();

    String identityToken();

    String authError();
}
