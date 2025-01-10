package com.example.fiap.archburgers.adapters.externalsystem;

import com.example.fiap.archburgers.domain.valueobjects.IdFormaPagamento;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceWebApiTest {

    @Mock
    private Environment environment;

    @Test
    public void listFormasPagamentoThrowsExceptionTest() {
        when(environment.getProperty("archburgers.integration.pagamento.ApiUrl"))
                .thenReturn("");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PagamentoServiceWebApi(environment));
    }

    @Test
    public void listFormasPagamentoInvalidApiUrlTest() {
        when(environment.getProperty("archburgers.integration.pagamento.ApiUrl"))
                .thenReturn("##########");
        Assertions.assertThrows(RuntimeException.class, () -> new PagamentoServiceWebApi(environment));
    }

    @Test
    public void parseFormasPagamentoResponseValidJsonTest() throws Exception {
        when(environment.getProperty("archburgers.integration.pagamento.ApiUrl"))
                .thenReturn("http://localhost:8090/pagamento/opcoes");
        PagamentoServiceWebApi service = new PagamentoServiceWebApi(environment);

        List<IdFormaPagamento> result = service.parseFormasPagamentoResponse(SAMPLE_FORMA_PAGAMENTO_DATA);
        assertThat(result).containsExactlyInAnyOrder(
                new IdFormaPagamento("MERCADO_PAGO"),
                new IdFormaPagamento("DINHEIRO")
        );
    }

    @Test
    public void parseFormasPagamentoInvalidResponse1() throws Exception {
        when(environment.getProperty("archburgers.integration.pagamento.ApiUrl"))
                .thenReturn("http://localhost:8090/pagamento/opcoes");
        PagamentoServiceWebApi service = new PagamentoServiceWebApi(environment);

        assertThatThrownBy(() -> service.parseFormasPagamentoResponse("[{}]"))
                .hasMessageContaining("id should be object");
    }

    @Test
    public void parseFormasPagamentoInvalidResponse2() throws Exception {
        when(environment.getProperty("archburgers.integration.pagamento.ApiUrl"))
                .thenReturn("http://localhost:8090/pagamento/opcoes");
        PagamentoServiceWebApi service = new PagamentoServiceWebApi(environment);

        assertThatThrownBy(() -> service.parseFormasPagamentoResponse("[{\"id\":{}}]"))
                .hasMessageContaining("codigo is missing");
    }

    @Language("json")
    private static final String SAMPLE_FORMA_PAGAMENTO_DATA = """
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
              }
            ]
            """;
}