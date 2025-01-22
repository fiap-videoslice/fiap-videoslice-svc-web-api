package com.example.fiap.videosliceapi.adapters.controllers;

import org.intellij.lang.annotations.Language;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexApiHandler {
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
}
