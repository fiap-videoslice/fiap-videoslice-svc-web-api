package com.example.fiap.archburgers.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.domain.entities.Carrinho;
import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CarrinhoRepositoryJdbcImplTest {

    private DatabaseConnection databaseConnection;
    private DatabaseConnection.ConnectionInstance connectionInstance;

    private CarrinhoRepositoryJdbcImpl carrinhoRepository;

    @BeforeEach
    void setUp() {
        databaseConnection = mock();
        connectionInstance = mock(DatabaseConnection.ConnectionInstance.class);
        when(databaseConnection.getConnection()).thenReturn(connectionInstance);

        carrinhoRepository = new CarrinhoRepositoryJdbcImpl(databaseConnection);
    }

    @Test
    void getCarrinhoSalvoByCliente_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.getCarrinhoSalvoByCliente(new IdCliente(12)));
    }

    @Test
    void getCarrinho_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.getCarrinho(1));
    }

    @Test
    void salvarCarrinhoVazio_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.salvarCarrinhoVazio(mock(Carrinho.class)));
    }

    @Test
    void salvarItemCarrinho_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.salvarItemCarrinho(mock(Carrinho.class), mock(ItemPedido.class)));
    }

    @Test
    void updateObservacaoCarrinho_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.updateObservacaoCarrinho(mock(Carrinho.class)));
    }

    @Test
    void deleteItensCarrinho_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.deleteItensCarrinho(mock(Carrinho.class)));
    }

    @Test
    void deleteCarrinho_dbError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> carrinhoRepository.deleteCarrinho(mock(Carrinho.class)));
    }


}