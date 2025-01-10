package com.example.fiap.archburgers.adapters.presenters;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.dto.ItemCardapioDto;
import com.example.fiap.archburgers.adapters.dto.ValorMonetarioDto;
import com.example.fiap.archburgers.adapters.testUtils.TestLocale;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.TipoItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemCardapioPresenterTest {
    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @Test
    void entityToPresentationDto() {
        var dto = ItemCardapioPresenter.entityToPresentationDto(new ItemCardapio(123,
                TipoItemCardapio.LANCHE, "Cheeseburger", "Hambúrguer com queijo", new ValorMonetario("25.99")));

        assertThat(dto).isEqualTo(new ItemCardapioDto(
                123, "LANCHE", "Cheeseburger", "Hambúrguer com queijo",
                new ValorMonetarioDto("25.99", "R$ 25,99")
        ));
    }

}