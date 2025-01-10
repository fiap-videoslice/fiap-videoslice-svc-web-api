package com.example.fiap.archburgers.adapters.testUtils;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Atribui um Locale esperado e previsível para uso na execução de testes automatizados
 */
public class TestLocale {
    public static void setDefault() {
        Locale.setDefault(Locale.of("pt", "BR"));
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }
}
