package com.example.fiap.archburgers.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.domain.entities.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidoRepositoryJdbcImplTest {

    private DatabaseConnection databaseConnection;
    private DatabaseConnection.ConnectionInstance connectionInstance;

    private PedidoRepositoryJdbcImpl pedidoRepository;

    @BeforeEach
    void setUp() {
        databaseConnection = mock();
        connectionInstance = mock(DatabaseConnection.ConnectionInstance.class);
        when(databaseConnection.getConnection()).thenReturn(connectionInstance);

        pedidoRepository = new PedidoRepositoryJdbcImpl(databaseConnection);
    }

    @Test
    void getPedido_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> pedidoRepository.getPedido(444));
    }

    // Simulate databaseError for the other methods
    @Test
    void savePedido_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> pedidoRepository.savePedido(mock(Pedido.class)));
    }

    @Test
    void listPedidos_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> pedidoRepository.listPedidos(Collections.emptyList(), LocalDateTime.now()));
    }

    @Test
    void updateStatus_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> pedidoRepository.updateStatus(mock(Pedido.class)));
    }

    @Test
    void deletePedido_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> pedidoRepository.deletePedido(123));
    }

}