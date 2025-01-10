package com.example.fiap.archburgers.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.auth.UsuarioLogadoTokenParser;
import com.example.fiap.archburgers.adapters.dto.ItemPedidoDto;
import com.example.fiap.archburgers.adapters.dto.PedidoDto;
import com.example.fiap.archburgers.adapters.dto.ValorMonetarioDto;
import com.example.fiap.archburgers.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.archburgers.adapters.testUtils.TestLocale;
import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.entities.Pedido;
import com.example.fiap.archburgers.domain.exception.DomainPermissionException;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.usecaseparam.CriarPedidoParam;
import com.example.fiap.archburgers.domain.usecases.PedidoUseCases;
import com.example.fiap.archburgers.domain.valueobjects.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PedidoApiHandlerTest {


    @Mock
    private PedidoUseCases pedidoUseCases;
    @Mock
    private UsuarioLogadoTokenParser usuarioLogadoTokenParser;

    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @BeforeEach
    public void setup() {
        PedidoApiHandler pedidoApiHandler = new PedidoApiHandler(pedidoUseCases, usuarioLogadoTokenParser, new DummyTransactionManager());
        mockMvc = MockMvcBuilders.standaloneSetup(pedidoApiHandler).build();
    }

    @Test
    public void criarPedido_Success() throws Exception {
        var param = new CriarPedidoParam(7, "DINHEIRO");
        com.example.fiap.archburgers.domain.auth.LoggedUser usuarioLogado = mock();

        var newPedido = criarPedidoSampleData(11, StatusPedido.PAGAMENTO);
        var newPedidoDto = criarPedidoSampleDto(11, StatusPedido.PAGAMENTO);

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        Mockito.when(pedidoUseCases.criarPedido(param, usuarioLogado)).thenReturn(newPedido);

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(newPedidoDto)));
    }

    @Test
    public void criarPedido_BadRequest() throws Exception {
        var param = new CriarPedidoParam(7, "DINHEIRO");
        com.example.fiap.archburgers.domain.auth.LoggedUser usuarioLogado = mock();

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        Mockito.when(pedidoUseCases.criarPedido(param, usuarioLogado)).thenThrow(
                new IllegalArgumentException("Carrinho inexistente"));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void criarPedido_PermissionError() throws Exception {
        var param = new CriarPedidoParam(7, "DINHEIRO");
        com.example.fiap.archburgers.domain.auth.LoggedUser usuarioLogado = mock();

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        Mockito.when(pedidoUseCases.criarPedido(param, usuarioLogado)).thenThrow(
                new DomainPermissionException("Carrinho de outro cliente"));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void criarPedido_InternalServerError() throws Exception {
        var param = new CriarPedidoParam(7, "DINHEIRO");
        com.example.fiap.archburgers.domain.auth.LoggedUser usuarioLogado = mock();

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        Mockito.when(pedidoUseCases.criarPedido(param, usuarioLogado)).thenThrow(
                new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void listarPedidos_todosAtivos() throws Exception {
        var pedidoDetalhe = criarPedidoSampleData(11, StatusPedido.RECEBIDO);
        var pedidoDto = criarPedidoSampleDto(11, StatusPedido.RECEBIDO);

        when(pedidoUseCases.listarPedidosAtivos()).thenReturn(List.of(pedidoDetalhe));

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(pedidoDto))));
    }

    @Test
    public void listarPedidos_byStatus() throws Exception {
        var pedidoDetalhe = criarPedidoSampleData(11, StatusPedido.RECEBIDO);
        var pedidoDto = criarPedidoSampleDto(11, StatusPedido.RECEBIDO);

        when(pedidoUseCases.listarPedidosByStatus(StatusPedido.RECEBIDO)).thenReturn(List.of(pedidoDetalhe));

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos?status=RECEBIDO"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(pedidoDto))));
    }

    @Test
    public void listarPedidos_atrasados() throws Exception {
        var pedidoDetalhe = criarPedidoSampleData(11, StatusPedido.RECEBIDO);
        var pedidoDto = criarPedidoSampleDto(11, StatusPedido.RECEBIDO);

        when(pedidoUseCases.listarPedidosComAtraso()).thenReturn(List.of(pedidoDetalhe));

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos?atraso=true"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(pedidoDto))));
    }

    @Test
    public void listarPedidos_BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void listarPedidos_InternalServerError() throws Exception {
        when(pedidoUseCases.listarPedidosAtivos()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos"))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    @Test
    public void validarPedido_Success() throws Exception {
        var idPedido = 11;
        var pedidoDetalhe = criarPedidoSampleData(idPedido, StatusPedido.PREPARACAO);
        var pedidoDto = criarPedidoSampleDto(idPedido, StatusPedido.PREPARACAO);

        when(pedidoUseCases.validarPedido(eq(idPedido))).thenReturn(pedidoDetalhe);

        mockMvc.perform(post("/pedidos/{idPedido}/validar", idPedido))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(pedidoDto)));
    }

    @Test
    public void validarPedido_BadRequest() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.validarPedido(eq(idPedido))).thenThrow(new IllegalArgumentException("Pedido inválido"));

        mockMvc.perform(post("/pedidos/{idPedido}/validar", idPedido))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void validarPedido_InternalServerError() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.validarPedido(eq(idPedido))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/pedidos/{idPedido}/validar", idPedido))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void setPronto_Success() throws Exception {
        var idPedido = 11;
        var pedidoDetalhe = criarPedidoSampleData(idPedido, StatusPedido.PRONTO);
        var pedidoDto = criarPedidoSampleDto(idPedido, StatusPedido.PRONTO);

        when(pedidoUseCases.setPronto(eq(idPedido))).thenReturn(pedidoDetalhe);

        mockMvc.perform(post("/pedidos/{idPedido}/setPronto", idPedido))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(pedidoDto)));
    }

    @Test
    public void setPronto_BadRequest() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.setPronto(eq(idPedido))).thenThrow(new IllegalArgumentException("Pedido inválido"));

        mockMvc.perform(post("/pedidos/{idPedido}/setPronto", idPedido))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void setPronto_InternalServerError() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.setPronto(eq(idPedido))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/pedidos/{idPedido}/setPronto", idPedido))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void finalizar_Success() throws Exception {
        var idPedido = 11;
        var pedidoDetalhe = criarPedidoSampleData(idPedido, StatusPedido.FINALIZADO);
        var pedidoDto = criarPedidoSampleDto(idPedido, StatusPedido.FINALIZADO);

        when(pedidoUseCases.finalizarPedido(eq(idPedido))).thenReturn(pedidoDetalhe);

        mockMvc.perform(post("/pedidos/{idPedido}/finalizar", idPedido))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(pedidoDto)));
    }

    @Test
    public void finalizar_BadRequest() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.finalizarPedido(eq(idPedido))).thenThrow(new IllegalArgumentException("Pedido inválido"));

        mockMvc.perform(post("/pedidos/{idPedido}/finalizar", idPedido))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void finalizar_InternalServerError() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.finalizarPedido(eq(idPedido))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/pedidos/{idPedido}/finalizar", idPedido))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void cancelar_Success() throws Exception {
        var idPedido = 11;
        var pedidoDetalhe = criarPedidoSampleData(idPedido, StatusPedido.CANCELADO);
        var pedidoDto = criarPedidoSampleDto(idPedido, StatusPedido.CANCELADO);

        when(pedidoUseCases.cancelarPedido(eq(idPedido))).thenReturn(pedidoDetalhe);

        mockMvc.perform(post("/pedidos/{idPedido}/cancelar", idPedido))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(pedidoDto)));
    }

    @Test
    public void cancelar_BadRequest() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.cancelarPedido(eq(idPedido))).thenThrow(new IllegalArgumentException("Pedido inválido"));

        mockMvc.perform(post("/pedidos/{idPedido}/cancelar", idPedido))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cancelar_InternalServerError() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.cancelarPedido(eq(idPedido))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/pedidos/{idPedido}/cancelar", idPedido))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getPedido_Success() throws Exception {
        var idPedido = 11;
        var pedidoDetalhe = criarPedidoSampleData(idPedido, StatusPedido.PREPARACAO);
        var pedidoDto = criarPedidoSampleDto(idPedido, StatusPedido.PREPARACAO);

        when(pedidoUseCases.getPedido(eq(idPedido))).thenReturn(pedidoDetalhe);

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos/{id}", idPedido))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(pedidoDto)));
    }

    @Test
    public void getPedido_NotFound() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.getPedido(eq(idPedido))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos/{id}", idPedido))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPedido_InternalServerError() throws Exception {
        var idPedido = 11;

        when(pedidoUseCases.getPedido(eq(idPedido))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/pedidos/{id}", idPedido))
                .andExpect(status().isInternalServerError());
    }

    /// ////////////////

    private PedidoDetalhe criarPedidoSampleData(int id, StatusPedido status) {
        return new PedidoDetalhe(
                Pedido.pedidoRecuperado(id, null, "Roberto Carlos",
                        List.of(new ItemPedido(1, 1001)),
                        null, status, new IdFormaPagamento("DINHEIRO"),
                        LocalDateTime.of(2024, 11, 16, 12, 5)),
                Map.of(1001, new ItemCardapio(1001, TipoItemCardapio.LANCHE, "Hamburger", "Hamburger 100g",
                        new ValorMonetario("21.00")))
        );
    }

    private PedidoDto criarPedidoSampleDto(int id, StatusPedido status) {
        return new PedidoDto(
                id, null, "Roberto Carlos",
                List.of(
                        new ItemPedidoDto(1, 1001, "LANCHE",
                                "Hamburger", "Hamburger 100g", new ValorMonetarioDto("21.00", "R$ 21,00"))
                ), null, status.name(), "DINHEIRO",
                new ValorMonetarioDto("21.00", "R$ 21,00"),
                1731769500000L
        );
    }

}
