
package com.example.fiap.archburgers.adapters.externalsystem;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoProdutosServiceImplTest {
    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testConstructorWithInvalidUrl() {
        Environment environment = new StaticEnvironment(Map.of(
                "archburgers.integration.cardapio.ApiUrl", "##########"
        ));

        assertThatThrownBy(() -> new CatalogoProdutosServiceImpl(environment))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("archburgers.integration.cardapio.ApiUrl is invalid: [##########]");
    }

    @Test
    public void testConstructorWithNullUrl() {
        Environment environment = new StaticEnvironment(Map.of(

        ));

        assertThatThrownBy(() -> new CatalogoProdutosServiceImpl(environment))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("archburgers.integration.cardapio.ApiUrl not set");
    }

    @Test
    public void valorMonetarioDeserializer() throws Exception {
        Environment environment = new StaticEnvironment(Map.of(
                "archburgers.integration.cardapio.ApiUrl", "http://localhost:8092/cardapio"
        ));

        CatalogoProdutosServiceImpl service = new CatalogoProdutosServiceImpl(environment);

        ValorMonetario result1 = service.mapper.readValue("{\"raw\": \"1.00\"}", ValorMonetario.class);
        assertThat(result1).isEqualTo(new ValorMonetario("1.00"));

        ValorMonetario result2 = service.mapper.readValue("{\"raw\": \"25.9\", \"formatted\": \"R$ 25,90\"}", ValorMonetario.class);
        assertThat(result2).isEqualTo(new ValorMonetario("25.90"));

        assertThatThrownBy(() -> service.mapper.readValue("{}", ValorMonetario.class))
                .hasMessageContaining("Invalid valor object. Missing 'raw' attr");

        assertThatThrownBy(() -> service.mapper.readValue("{\"raw\": []}", ValorMonetario.class))
                .hasMessageContaining("Invalid valor object. Invalid 'raw' attr");
    }
}
