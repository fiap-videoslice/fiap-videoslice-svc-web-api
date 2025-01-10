package com.example.fiap.archburgers.adapters.externalsystem.integration;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.externalsystem.PagamentoServiceWebApi;
import com.example.fiap.archburgers.domain.valueobjects.IdFormaPagamento;
import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagamentoServiceWebApiIT {
    private MockWebServer server;

    private PagamentoServiceWebApi webApi;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();

        HttpUrl formasPagamentoUrl = server.url("/pagamento/opcoes");

        webApi = new PagamentoServiceWebApi(new StaticEnvironment(Map.of(
                "archburgers.integration.pagamento.ApiUrl", formasPagamentoUrl.toString()
        )));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null)
            server.close();
    }

    @Test
    void listFormasPagamento() throws Exception {
        server.enqueue(new MockResponse().setBody(SAMPLE_DATA));

        List<IdFormaPagamento> result = webApi.listFormasPagamento();

        assertThat(result).containsExactlyInAnyOrder(
                new IdFormaPagamento("DINHEIRO"),
                new IdFormaPagamento("MERCADO_PAGO"),
                new IdFormaPagamento("CARTAO_MAQUINA")
        );
    }

    @Test
    void listFormasPagamento_serviceError() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> webApi.listFormasPagamento())
                .hasMessageContaining("Erro na solicitação de formas de pagamento");
    }

    @Test
    void listFormasPagamento_networkError() {
        webApi = new PagamentoServiceWebApi(new StaticEnvironment(Map.of(
                "archburgers.integration.pagamento.ApiUrl", "https://localhost:9999"
        )));

        assertThatThrownBy(() -> webApi.listFormasPagamento())
                .hasMessageContaining("Erro ao solicitar formas de pagamento");
    }

    @Language("json")
    private static final String SAMPLE_DATA = """
            [
              {
                "id": {
                  "codigo": "MERCADO_PAGO"
                },
                "descricao": "Pagamento pelo QrCode do aplicativo Mercado Pago"
              },
              {
                "id": {
                  "codigo": "DINHEIRO"
                },
                "descricao": "Pagamento em dinheiro direto ao caixa"
              },
              {
                "id": {
                  "codigo": "CARTAO_MAQUINA"
                },
                "descricao": "Pagamento na máquina da loja"
              }
            ]
            """;
}