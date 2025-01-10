package com.example.fiap.archburgers.adapters.dto;

import com.example.fiap.archburgers.adapters.testUtils.TestLocale;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValorMonetarioDtoTest {

    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @Test
    void from() {
        assertThat(ValorMonetarioDto.from(new ValorMonetario("12.50"))).isEqualTo(
                new ValorMonetarioDto("12.50", "R$ 12,50"));

        assertThat(ValorMonetarioDto.from(new ValorMonetario(".8"))).isEqualTo(
                new ValorMonetarioDto("0.80", "R$ 0,80"));
    }
}