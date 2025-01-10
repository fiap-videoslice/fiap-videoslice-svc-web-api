package com.example.fiap.archburgers.adapters.dto;

import com.example.fiap.archburgers.domain.entities.Cliente;
import com.example.fiap.archburgers.domain.valueobjects.Cpf;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClienteWithTokenDtoTest {

    @Test
    public void testFromEntity() {
        IdCliente id = new IdCliente(1);
        Cpf cpf = new Cpf("12332112340");

        Cliente cliente = new Cliente(id, "John Doe", cpf, "john.doe@example.com");
        String token = "someToken123";

        ClienteWithTokenDto dto = ClienteWithTokenDto.fromEntity(cliente, token);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nome()).isEqualTo("John Doe");
        assertThat(dto.cpf()).isEqualTo("12332112340");
        assertThat(dto.email()).isEqualTo("john.doe@example.com");
        assertThat(dto.token()).isEqualTo("someToken123");
    }

    @Test
    public void testFromEntity_nullId() {
        Cpf cpf = new Cpf("12332112340");

        Cliente cliente = new Cliente(null, "John Doe", cpf, "john.doe@example.com");
        String token = "someToken123";

        ClienteWithTokenDto dto = ClienteWithTokenDto.fromEntity(cliente, token);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNull();
        assertThat(dto.nome()).isEqualTo("John Doe");
        assertThat(dto.cpf()).isEqualTo("12332112340");
        assertThat(dto.email()).isEqualTo("john.doe@example.com");
        assertThat(dto.token()).isEqualTo("someToken123");
    }
}