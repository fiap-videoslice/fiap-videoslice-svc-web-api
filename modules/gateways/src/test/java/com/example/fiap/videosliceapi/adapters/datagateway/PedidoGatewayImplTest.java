package com.example.fiap.archburgers.adapters.datagateway;

import com.example.fiap.archburgers.domain.datasource.PedidoDataSource;
import com.example.fiap.archburgers.domain.entities.Pedido;
import com.example.fiap.archburgers.domain.valueobjects.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PedidoGatewayImplTest {

    private PedidoGatewayImpl pedidoGatewayImpl;
    private PedidoDataSource pedidoDataSourceMock;

    @BeforeEach
    void setUp() {
        pedidoDataSourceMock = mock(PedidoDataSource.class);
        pedidoGatewayImpl = new PedidoGatewayImpl(pedidoDataSourceMock);
    }

    @Test
    void getPedido() {
        Pedido pedido = mock(Pedido.class);
        when(pedidoDataSourceMock.getPedido(1)).thenReturn(pedido);
                
        Pedido result = pedidoGatewayImpl.getPedido(1);
        
        assertThat(result).isSameAs(pedido);
    }

    @Test
    void savePedido() {
        Pedido newPedido = mock(Pedido.class);
        Pedido savedPedido = mock(Pedido.class);
        when(pedidoDataSourceMock.savePedido(newPedido)).thenReturn(savedPedido);

        Pedido result = pedidoGatewayImpl.savePedido(newPedido);

        assertThat(result).isSameAs(savedPedido);
    }

    @Test
    void updateStatus() {
        Pedido pedidoMock = mock(Pedido.class);
        doNothing().when(pedidoDataSourceMock).updateStatus(pedidoMock);

        pedidoGatewayImpl.updateStatus(pedidoMock);

        verify(pedidoDataSourceMock).updateStatus(pedidoMock);
    }

    @Test
    void listPedidos() {
        List<StatusPedido> filtroStatus = List.of(StatusPedido.RECEBIDO, StatusPedido.PREPARACAO);
        LocalDateTime olderThan = LocalDateTime.now().minusDays(7);
        List<Pedido> pedidos = List.of(mock(), mock(), mock());

        when(pedidoDataSourceMock.listPedidos(filtroStatus, olderThan)).thenReturn(pedidos);

        List<Pedido> result = pedidoGatewayImpl.listPedidos(filtroStatus, olderThan);

        assertThat(result).isSameAs(pedidos);
    }

    @Test
    void excluirPedido() {
        pedidoGatewayImpl.excluirPedido(12);

        verify(pedidoDataSourceMock).deletePedido(12);
    }
}
