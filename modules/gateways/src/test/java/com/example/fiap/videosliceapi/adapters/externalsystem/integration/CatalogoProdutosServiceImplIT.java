package com.example.fiap.archburgers.adapters.externalsystem.integration;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.externalsystem.CatalogoProdutosServiceImpl;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.TipoItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogoProdutosServiceImplIT {

    @Test
    void findAll() throws Exception {
        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody(SAMPLE_DATA));

            server.start();
            HttpUrl baseUrl = server.url("/cardapio/");

            CatalogoProdutosServiceImpl service = new CatalogoProdutosServiceImpl(new StaticEnvironment(Map.of(
                    "archburgers.integration.cardapio.ApiUrl", baseUrl.url().toString()
            )));

            Collection<ItemCardapio> result = service.findAll();

            assertThat(result).containsExactlyInAnyOrder(
                    new ItemCardapio(1, TipoItemCardapio.LANCHE, "Cheeseburger",
                            "Hamburger com queijo", new ValorMonetario("25.90")),
                    new ItemCardapio(2, TipoItemCardapio.ACOMPANHAMENTO, "Batata frita M",
                            "Batatas fritas tamanho Médio", new ValorMonetario("11.50"))
            );
        }
    }

    @Language("json")
    private static final String SAMPLE_DATA = """
            [
            	{
            		"id": 1,
            		"tipo": "LANCHE",
            		"nome": "Cheeseburger",
            		"descricao": "Hamburger com queijo",
            		"valor": {
            			"raw": "25.90",
            			"formatted": "R$ 25,90"
            		}
            	},
            	{
            		"id": 2,
            		"tipo": "ACOMPANHAMENTO",
            		"nome": "Batata frita M",
            		"descricao": "Batatas fritas tamanho Médio",
            		"valor": {
            			"raw": "11.50",
            			"formatted": "R$ 11,50"
            		}
            	}
            ]""";
}