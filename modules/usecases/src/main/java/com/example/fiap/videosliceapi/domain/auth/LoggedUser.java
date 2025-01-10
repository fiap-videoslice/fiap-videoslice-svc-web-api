package com.example.fiap.videosliceapi.domain.auth;

/**
 * Representação de um usuário autenticado no sistema.
 * É responsabilidade das camadas mais externas interagir com o mecanismo de autenticação e autorização, fornecendo
 * instâncias válidas para a camada de use cases
 */
public interface LoggedUser {
    boolean authenticated();

    String getName();
    String getEmail();

    UserGroup getGroup();

    String identityToken();

    String authError();
}
