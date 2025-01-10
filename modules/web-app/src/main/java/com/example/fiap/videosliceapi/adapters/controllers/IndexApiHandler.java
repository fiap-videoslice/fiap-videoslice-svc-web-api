package com.example.fiap.videosliceapi.adapters.controllers;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.adapters.presenters.LoggedUserPresenter;
import com.example.fiap.videosliceapi.apiutils.WebUtils;
import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import io.swagger.v3.oas.annotations.Operation;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IndexApiHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexApiHandler.class);

    private final LoggedUserTokenParser loggedUserTokenParser;

    @Autowired
    public IndexApiHandler(LoggedUserTokenParser loggedUserTokenParser) {
        this.loggedUserTokenParser = loggedUserTokenParser;
    }

    @GetMapping(path = "/", produces = "text/html")
    public String index() {
        @Language("html")
        var indexContents = """
                <html>
                <body>
                    Docs: <a href="/v3/api-docs">/v3/api-docs (JSON)</a> - <a href="/swagger-ui.html">/swagger-ui.html (UI)</a>
                </body>
                </html>
                """.stripIndent();
        return indexContents;
    }

    @GetMapping(path = "/healthcheck", produces = "text/plain")
    public String healthcheck() {
        return "OK";
    }

    @Operation(description = "Retorna os dados do usuário logado")
    @GetMapping(path = "/usuario/conectado")
    public ResponseEntity<Map<String, Object>> getClienteConectado(@RequestHeader HttpHeaders headers) {
        try {
            LoggedUser loggedUser = loggedUserTokenParser.verifyLoggedUser(headers);

            if (loggedUser.authenticated()) {
                return WebUtils.okResponse(LoggedUserPresenter.toMap(loggedUser));
            } else {
                return WebUtils.errorResponse(HttpStatus.UNAUTHORIZED, loggedUser.authError());
            }
        } catch (Exception e) {
            LOGGER.error("Ocorreu um erro ao recuperar usuário autenticado: {}", e, e);
            return WebUtils.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro ao recuperar usuário autenticado");
        }
    }
}
