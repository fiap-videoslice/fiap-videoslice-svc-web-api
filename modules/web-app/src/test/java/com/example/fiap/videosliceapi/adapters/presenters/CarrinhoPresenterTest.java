package com.example.fiap.archburgers.adapters.presenters;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.dto.CarrinhoDto;
import com.example.fiap.archburgers.adapters.dto.ItemPedidoDto;
import com.example.fiap.archburgers.adapters.dto.ValorMonetarioDto;
import com.example.fiap.archburgers.adapters.testUtils.TestLocale;
import com.example.fiap.archburgers.domain.entities.Carrinho;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.valueobjects.CarrinhoDetalhe;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import com.example.fiap.archburgers.domain.valueobjects.TipoItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrinhoPresenterTest {

    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @Test
    void entityToPresentationDto() {
        var entity = Carrinho.carrinhoSalvoClienteIdentificado(123, new IdCliente(98),
                List.of(
                        new ItemPedido(1, 2),
                        new ItemPedido(2, 14)
                ),"Não adicionar molho",
                LocalDateTime.of(2024, 4, 29, 15, 30)
        );

        var detalhesItens = Map.of(
                2, new ItemCardapio(2, TipoItemCardapio.LANCHE, "Cheese Burger",
                        "Hamburger com queijo", new ValorMonetario("18.50")),
                14, new ItemCardapio(14, TipoItemCardapio.BEBIDA, "Refrigerante P",
                        "Refrigerante 300ml", new ValorMonetario("4.99"))
        );

        var dto = CarrinhoPresenter.entityToPresentationDto(new CarrinhoDetalhe(entity, detalhesItens));

        List<ItemPedidoDto> dtoItens = List.of(
                new ItemPedidoDto(1, 2, "LANCHE", "Cheese Burger",
                        "Hamburger com queijo", new ValorMonetarioDto("18.50", "R$ 18,50")),
                new ItemPedidoDto(2, 14, "BEBIDA", "Refrigerante P",
                        "Refrigerante 300ml", new ValorMonetarioDto("4.99", "R$ 4,99"))
        );

        assertThat(dto).isEqualTo(new CarrinhoDto(123, 98, null,
                dtoItens, "Não adicionar molho",
                new ValorMonetarioDto("23.49", "R$ 23,49"), 1714415400000L));
    }

    @Test
    void entityToPresentationDto_invalidDetailMaooing() {
        var entity = Carrinho.carrinhoSalvoClienteIdentificado(123, new IdCliente(98),
                List.of(
                        new ItemPedido(1, 2),
                        new ItemPedido(2, 14)
                ),"Não adicionar molho",
                LocalDateTime.of(2024, 4, 29, 15, 30)
        );

        var detalhesItens = Map.of(
                2, new ItemCardapio(2, TipoItemCardapio.LANCHE, "Cheese Burger",
                        "Hamburger com queijo", new ValorMonetario("18.50"))
        );

        assertThatThrownBy(() -> CarrinhoPresenter.entityToPresentationDto(new CarrinhoDetalhe(entity, detalhesItens)))
                .hasMessageContaining("details missing");
    }
}