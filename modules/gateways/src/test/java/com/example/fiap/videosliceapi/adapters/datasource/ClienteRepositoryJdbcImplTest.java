package com.example.fiap.archburgers.adapters.datasource;

import com.example.fiap.archburgers.domain.entities.Cliente;
import com.example.fiap.archburgers.domain.valueobjects.Cpf;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClienteRepositoryJdbcImplTest {

    private DatabaseConnection databaseConnection;
    private DatabaseConnection.ConnectionInstance connectionInstance;

    private ClienteRepositoryJdbcImpl clienteRepository;

    @BeforeEach
    void setUp() {
        databaseConnection = mock();
        connectionInstance = mock(DatabaseConnection.ConnectionInstance.class);
        when(databaseConnection.getConnection()).thenReturn(connectionInstance);

        clienteRepository = new ClienteRepositoryJdbcImpl(databaseConnection);
    }

    @Test
    void getClienteByCpf_connectionError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> clienteRepository.getClienteByCpf(new Cpf("12332112340")));
    }

    @Test
    void getClienteById_connectionError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> clienteRepository.getClienteById(123));
    }

    @Test
    void salvarCliente_connectionError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> clienteRepository.salvarCliente(mock(Cliente.class)));
    }

    @Test
    void listarTodosClientes_connectionError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> clienteRepository.listarTodosClientes());
    }

}