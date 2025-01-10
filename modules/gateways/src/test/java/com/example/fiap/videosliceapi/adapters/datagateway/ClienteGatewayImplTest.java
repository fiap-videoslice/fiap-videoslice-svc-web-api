package com.example.fiap.archburgers.adapters.datagateway;

import com.example.fiap.archburgers.domain.datasource.ClienteDataSource;
import com.example.fiap.archburgers.domain.entities.Cliente;
import com.example.fiap.archburgers.domain.valueobjects.Cpf;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ClienteGatewayImplTest {

    @Mock
    private ClienteDataSource clienteDataSource;

    private ClienteGatewayImpl clienteGateway;

    @BeforeEach
    public void setup() {
        clienteGateway = new ClienteGatewayImpl(clienteDataSource);
    }

    @Test
    public void testGetClienteByCpf() {
        Cpf cpf = new Cpf("12332112340");
        Cliente existingCliente = new Cliente(new IdCliente(123), "Nosso Cliente", cpf, "cli.ente@example.com");

        when(clienteDataSource.getClienteByCpf(cpf)).thenReturn(existingCliente);

        Cliente clienteResponse = clienteGateway.getClienteByCpf(cpf);

        assertThat(clienteResponse).isSameAs(existingCliente);
    }

    @Test
    public void testGetClienteById() {
        int id = 123;
        Cliente existingCliente = new Cliente(new IdCliente(id), "Nosso Cliente", new Cpf("12332112340"), "cli.ente@example.com");

        when(clienteDataSource.getClienteById(id)).thenReturn(existingCliente);

        Cliente clienteResponse = clienteGateway.getClienteById(id);

        assertThat(clienteResponse).isSameAs(existingCliente);
    }

    @Test
    public void testSalvarCliente() {
        Cliente newCliente = new Cliente(null, "Novo Cliente", new Cpf("99988877714"), "novo.ente@example.com");
        Cliente savedCliente = new Cliente(new IdCliente(456), "Novo Cliente", new Cpf("99988877714"), "novo.ente@example.com");

        when(clienteDataSource.salvarCliente(newCliente)).thenReturn(savedCliente);

        Cliente clienteResponse = clienteGateway.salvarCliente(newCliente);

        assertThat(clienteResponse).isSameAs(savedCliente);
    }

    @Test
    public void testListarTodosClientes() {
        Cliente cliente1 = new Cliente(new IdCliente(123), "Cliente 1", new Cpf("12332112340"), "cliente1@example.com");
        Cliente cliente2 = new Cliente(new IdCliente(456), "Cliente 2", new Cpf("99988877714"), "cliente2@example.com");
        List<Cliente> existingClientes = List.of(cliente1, cliente2);

        when(clienteDataSource.listarTodosClientes()).thenReturn(existingClientes);

        List<Cliente> clienteResponse = clienteGateway.listarTodosClientes();

        assertThat(clienteResponse).isSameAs(existingClientes);
    }
}
