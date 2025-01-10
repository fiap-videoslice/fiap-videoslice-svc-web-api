package com.example.fiap.archburgers.adapters.datagateway;

import com.example.fiap.archburgers.domain.datasource.CarrinhoDataSource;
import com.example.fiap.archburgers.domain.entities.Carrinho;
import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CarrinhoGatewayImplTest {
    private CarrinhoDataSource carrinhoDataSource;
    private CarrinhoGatewayImpl carrinhoGatewayImpl;

    @BeforeEach
    void setUp() {
        carrinhoDataSource = mock(CarrinhoDataSource.class);
        carrinhoGatewayImpl = new CarrinhoGatewayImpl(carrinhoDataSource);
    }

    @Test
    void getCarrinhoSalvoByCliente() {
        IdCliente idCliente = new IdCliente(123);
        Carrinho expectedCarrinho = mock();
        when(carrinhoDataSource.getCarrinhoSalvoByCliente(idCliente)).thenReturn(expectedCarrinho);

        Carrinho actualCarrinho = carrinhoGatewayImpl.getCarrinhoSalvoByCliente(idCliente);

        assertThat(actualCarrinho).isSameAs(expectedCarrinho);
    }

    @Test
    void getCarrinho() {
        int idCarrinho = 123;
        Carrinho expectedCarrinho = mock(Carrinho.class);
        when(carrinhoDataSource.getCarrinho(idCarrinho)).thenReturn(expectedCarrinho);

        Carrinho actualCarrinho = carrinhoGatewayImpl.getCarrinho(idCarrinho);

        assertThat(actualCarrinho).isSameAs(expectedCarrinho);
    }

    @Test
    void testSalvarCarrinhoVazio() {
        Carrinho emptyCart = mock(Carrinho.class);
        Carrinho saved = mock(Carrinho.class);

        when(carrinhoDataSource.salvarCarrinhoVazio(emptyCart)).thenReturn(saved);

        Carrinho actualCart = carrinhoGatewayImpl.salvarCarrinhoVazio(emptyCart);

        assertThat(actualCart).isSameAs(saved);
    }

    @Test
    void testSalvarItemCarrinho() {
        Carrinho cart = mock(Carrinho.class);
        ItemPedido item = mock(ItemPedido.class);

        doNothing().when(carrinhoDataSource).salvarItemCarrinho(cart, item);

        carrinhoGatewayImpl.salvarItemCarrinho(cart, item);

        verify(carrinhoDataSource, times(1)).salvarItemCarrinho(cart, item);
    }

    @Test
    void testDeleteCarrinho() {
        Carrinho cart = mock(Carrinho.class);

        doNothing().when(carrinhoDataSource).deleteCarrinho(cart);

        carrinhoGatewayImpl.deleteCarrinho(cart);

        verify(carrinhoDataSource, times(1)).deleteCarrinho(cart);
    }

    @Test
    void testUpdateObservacaoCarrinho() {
        Carrinho cart = mock(Carrinho.class);

        doNothing().when(carrinhoDataSource).updateObservacaoCarrinho(cart);

        carrinhoGatewayImpl.updateObservacaoCarrinho(cart);

        verify(carrinhoDataSource, times(1)).updateObservacaoCarrinho(cart);
    }

    @Test
    void testDeleteItensCarrinho() {
        Carrinho cart = mock(Carrinho.class);

        doNothing().when(carrinhoDataSource).deleteItensCarrinho(cart);

        carrinhoGatewayImpl.deleteItensCarrinho(cart);

        verify(carrinhoDataSource, times(1)).deleteItensCarrinho(cart);
    }
}
