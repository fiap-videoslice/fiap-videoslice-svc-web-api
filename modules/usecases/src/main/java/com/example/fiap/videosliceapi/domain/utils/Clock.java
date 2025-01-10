package com.example.fiap.videosliceapi.domain.utils;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Obtenção de data/hora em um serviço isolado para facilitar os testes.
 */
public class Clock {
    public Instant now() {
        return Instant.now();
    }
}
