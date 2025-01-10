package com.example.fiap.archburgers.adapters.externalsystem;

import com.example.fiap.archburgers.domain.entities.Pedido;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PainelPedidosImplTest {

    /**
     * Nothing to test in this dummy implementation. Creating a test only for the coverage
     */
    @Test
    void testNotificarPedidoPronto() {
        // Initialize instances
        PainelPedidosImpl painelPedidosImpl = new PainelPedidosImpl();

        Pedido pedido = mock();
        when(pedido.id()).thenReturn(543);

        painelPedidosImpl.notificarPedidoPronto(pedido);
    }

}