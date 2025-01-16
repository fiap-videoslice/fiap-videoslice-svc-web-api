package com.example.fiap.videosliceapi.adapters.presenters;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUser;

import java.util.Map;

public class LoggedUserPresenter {
    public static Map<String, Object> toMap(LoggedUser usuarioLogado) {
        return Map.of(
                "nome", usuarioLogado.getName(),
                "email", usuarioLogado.getEmail(),
                "grupo", usuarioLogado.getGroup() != null ? usuarioLogado.getGroup().name() : "",
                "token", usuarioLogado.identityToken()
        );
    }
}
