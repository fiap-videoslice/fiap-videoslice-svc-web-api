package com.example.fiap.archburgers.adapters.externalsystem;

import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.entities.Pedido;
import com.example.fiap.archburgers.domain.exception.DomainArgumentException;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.external.Pagamento;
import com.example.fiap.archburgers.domain.valueobjects.*;
import com.example.fiap.archburgers.testUtils.TestLocale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

public class PagamentoServiceImplTest {

    private PagamentoServiceWebApi webApi;
    private PagamentoServiceQueueApi queueApi;
    private PagamentoServiceImpl pagamentoServiceImpl;

    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @BeforeEach
    void setup() {
        webApi = mock(PagamentoServiceWebApi.class);
        queueApi = mock(PagamentoServiceQueueApi.class);
        pagamentoServiceImpl = new PagamentoServiceImpl(webApi, queueApi);
    }

    @Test
    void validarFormaPagamento_valido() throws Exception {
        IdFormaPagamento formaPagamentoValid = new IdFormaPagamento("valid");
        when(webApi.listFormasPagamento()).thenReturn(Collections.singletonList(formaPagamentoValid));

        IdFormaPagamento returnedFormaPagamento = pagamentoServiceImpl.validarFormaPagamento("valid");

        assertSame(formaPagamentoValid, returnedFormaPagamento);
        verify(webApi, times(1)).listFormasPagamento();
    }

    @Test
    void validarFormaPagamento_checkUsesCache() throws Exception {
        pagamentoServiceImpl = new PagamentoServiceImpl(webApi, queueApi, 200L);

        IdFormaPagamento formaPagamentoValid = new IdFormaPagamento("valid");
        when(webApi.listFormasPagamento()).thenReturn(Collections.singletonList(formaPagamentoValid));

        pagamentoServiceImpl.validarFormaPagamento("valid");
        pagamentoServiceImpl.validarFormaPagamento("valid");
        try {
            pagamentoServiceImpl.validarFormaPagamento("invalid");
        } catch (Exception ignored) {
        }
        pagamentoServiceImpl.validarFormaPagamento("valid");

        // external API invoked only once until now
        verify(webApi, times(1)).listFormasPagamento();

        // wait cache expiration before performing new request
        Thread.sleep(250L);

        pagamentoServiceImpl.validarFormaPagamento("valid");
        verify(webApi, times(2)).listFormasPagamento();
    }

    @Test
    void validarFormaPagamento_invalido() throws Exception {
        when(webApi.listFormasPagamento()).thenReturn(List.of(new IdFormaPagamento("valid")));

        assertThatThrownBy(() -> pagamentoServiceImpl.validarFormaPagamento("invalid"))
                .isInstanceOf(DomainArgumentException.class)
                .hasMessage("Forma de pagamento desconhecida: invalid");
    }

    @Test
    void iniciarPagamento() throws Exception {
        List<ItemPedido> itensPedido = List.of(
                new ItemPedido(1, 1000),
                new ItemPedido(2, 1001)
        );

        Map<Integer, ItemCardapio> detalhesItensPedido = Map.of(
                1000, new ItemCardapio(1000, TipoItemCardapio.LANCHE, "Hamburger", "Hamburger gigante",
                        new ValorMonetario("25.90")),
                1001, new ItemCardapio(1001, TipoItemCardapio.BEBIDA, "Refrigerante", "Refrigerante lata",
                        new ValorMonetario("5.00"))
        );

        var pedido = Pedido.pedidoRecuperado(223, new IdCliente(25), null, itensPedido,
                "Lanche sem cebola", StatusPedido.PAGAMENTO, new IdFormaPagamento("MERCADO_PAGO"),
                LocalDateTime.of(2024, 5, 18, 15, 30));

        ///
        pagamentoServiceImpl.iniciarPagamento(new PedidoDetalhe(pedido, detalhesItensPedido));

        var expectedJson = """
                {
                    "id": 223,
                    "idClienteIdentificado": 25,
                    "itens": [
                      {
                        "numSequencia": 1,
                        "itemCardapio": {
                          "id": 1000,
                          "tipo": "LANCHE",
                          "nome": "Hamburger",
                          "descricao": "Hamburger gigante",
                          "valor": "25.90"
                        }
                      },
                      {
                        "numSequencia": 2,
                        "itemCardapio": {
                          "id": 1001,
                          "tipo": "BEBIDA",
                          "nome": "Refrigerante",
                          "descricao": "Refrigerante lata",
                          "valor": "5.00"
                        }
                      }
                    ],
                    "observacoes": "Lanche sem cebola",
                    "status": "PAGAMENTO",
                    "formaPagamento": "MERCADO_PAGO",
                    "dataHoraPedido": "2024-05-18T18:30:00Z"}
                """;

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(queueApi).sendMessageQueuePagamento(messageCaptor.capture());
        String actualMessage = messageCaptor.getValue();

        JSONAssert.assertEquals(expectedJson, actualMessage, JSONCompareMode.LENIENT);
    }

    @Test
    void receberConfirmacoes_success() throws Exception {
        PagamentoServiceQueueApi.MessageSummary message =
                new PagamentoServiceQueueApi.MessageSummary("""
                        {
                          "formaPagamento": "MERCADO_PAGO",
                          "dataHoraAtualizacao": "2024-11-11T17:42:38.000000000",
                          "codigoPagamentoCliente": "00020101021243650016COM.MERCADOLIBRE020",
                          "valor": "37.98",
                          "dataHoraCriacao": "2024-11-11T17:41:49.000000000",
                          "idPedidoSistemaExterno": "24902326150",
                          "id": "67326c0dbab6b22798c180a4",
                          "idPedido": 63,
                          "status": "FINALIZADO"
                        }
                        """, "AbcDEfg");

        when(queueApi.receiveMessagesQueueConfirmacao())
                .thenReturn(List.of(message));

        Consumer<Pagamento> callback = mock();

        pagamentoServiceImpl.receberConfirmacoes(callback);

        verify(callback).accept(new Pagamento(
                "67326c0dbab6b22798c180a4",
                63,
                new IdFormaPagamento("MERCADO_PAGO"),
                StatusPagamento.FINALIZADO,
                new ValorMonetario("37.98"),
                LocalDateTime.of(2024, 11, 11, 17, 41, 49),
                LocalDateTime.of(2024, 11, 11, 17, 42, 38),
                "00020101021243650016COM.MERCADOLIBRE020",
                "24902326150"
        ));

        verify(queueApi).deleteMessagesQueueConfirmacao(message);
    }

    @Test
    void receberConfirmacoes_callbackThrows() throws Exception {
        PagamentoServiceQueueApi.MessageSummary message =
                new PagamentoServiceQueueApi.MessageSummary("""
                        {
                          "formaPagamento": "MERCADO_PAGO",
                          "dataHoraAtualizacao": "2024-11-11T17:42:38.000000000",
                          "codigoPagamentoCliente": "00020101021243650016COM.MERCADOLIBRE020",
                          "valor": "37.98",
                          "dataHoraCriacao": "2024-11-11T17:41:49.000000000",
                          "idPedidoSistemaExterno": "24902326150",
                          "_id": "67326c0dbab6b22798c180a4",
                          "idPedido": 63,
                          "status": "FINALIZADO"
                        }
                        """, "AbcDEfg");

        when(queueApi.receiveMessagesQueueConfirmacao())
                .thenReturn(List.of(message));

        Consumer<Pagamento> callback = mock();
        doThrow(new RuntimeException("Something went wrong")).when(callback).accept(any());

        pagamentoServiceImpl.receberConfirmacoes(callback);

        verify(queueApi, never()).deleteMessagesQueueConfirmacao(any());
    }

}
