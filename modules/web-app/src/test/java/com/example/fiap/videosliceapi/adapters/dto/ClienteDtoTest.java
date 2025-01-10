package com.example.fiap.archburgers.adapters.dto;

import com.example.fiap.archburgers.domain.entities.Cliente;
import com.example.fiap.archburgers.domain.valueobjects.Cpf;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteDtoTest {

    @Test
    void testFromEntity_withAllFields() {
        IdCliente idCliente = new IdCliente(1);
        Cpf cpf = new Cpf("12332112340");
        Cliente cliente = new Cliente(idCliente, "Test Name", cpf, "test@example.com");

        ClienteDto clienteDto = ClienteDto.fromEntity(cliente);

        assertThat(clienteDto).isNotNull();
        assertThat(clienteDto.id()).isEqualTo(1);
        assertThat(clienteDto.nome()).isEqualTo("Test Name");
        assertThat(clienteDto.cpf()).isEqualTo("12332112340");
        assertThat(clienteDto.email()).isEqualTo("test@example.com");
    }

    @Test
    void testFromEntity_withNullId() {
        Cpf cpf = new Cpf("12332112340");
        Cliente cliente = new Cliente(null, "Test Name", cpf, "test@example.com");

        ClienteDto clienteDto = ClienteDto.fromEntity(cliente);

        assertThat(clienteDto).isNotNull();
        assertThat(clienteDto.id()).isNull();
        assertThat(clienteDto.nome()).isEqualTo("Test Name");
        assertThat(clienteDto.cpf()).isEqualTo("12332112340");
        assertThat(clienteDto.email()).isEqualTo("test@example.com");
    }
}