package com.example.fiap.archburgers.adapters.controllers;

import com.example.fiap.archburgers.adapters.auth.UsuarioLogadoTokenParser;
import com.example.fiap.archburgers.adapters.dto.*;
import com.example.fiap.archburgers.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.archburgers.adapters.testUtils.TestLocale;
import com.example.fiap.archburgers.domain.entities.Carrinho;
import com.example.fiap.archburgers.domain.entities.ItemPedido;
import com.example.fiap.archburgers.domain.external.ItemCardapio;
import com.example.fiap.archburgers.domain.usecaseparam.CriarCarrinhoParam;
import com.example.fiap.archburgers.domain.usecases.CarrinhoUseCases;
import com.example.fiap.archburgers.domain.valueobjects.CarrinhoDetalhe;
import com.example.fiap.archburgers.domain.valueobjects.TipoItemCardapio;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CarrinhoApiHandlerTest {

    @Mock
    private CarrinhoUseCases carrinhoUseCases;
    @Mock
    private UsuarioLogadoTokenParser usuarioLogadoTokenParser;

    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        TestLocale.setDefault();
    }

    @BeforeEach
    public void setup() {
        CarrinhoApiHandler carrinhoApiHandler = new CarrinhoApiHandler(carrinhoUseCases, usuarioLogadoTokenParser, new DummyTransactionManager());
        mockMvc = MockMvcBuilders.standaloneSetup(carrinhoApiHandler).build();
    }

    @Test
    public void findCarrinho_Success() throws Exception {
        Mockito.when(carrinhoUseCases.findCarrinho(11)).thenReturn(CARRINHO_11);

        mockMvc.perform(get("/carrinho/11"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)
                ));
    }

    @Test
    public void findCarrinho_NotFound() throws Exception {
        Mockito.when(carrinhoUseCases.findCarrinho(1)).thenReturn(null);

        mockMvc.perform(get("/carrinho/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    public void findCarrinho_InvalidId() throws Exception {
        mockMvc.perform(get("/carrinho/null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findCarrinho_InternalServerError() throws Exception {
        Mockito.when(carrinhoUseCases.findCarrinho(Mockito.anyInt())).thenThrow(new RuntimeException("Unexpected Error"));

        mockMvc.perform(get("/carrinho/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void iniciarCarrinho_Success() throws Exception {
        var param = new CriarCarrinhoParam("Roberto Carlos");
        com.example.fiap.archburgers.domain.auth.LoggedUser usuarioLogado = mock();

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        Mockito.when(carrinhoUseCases.criarCarrinho(param, usuarioLogado)).thenReturn(CARRINHO_11);

        mockMvc.perform(post("/carrinho")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)));
    }

    @Test
    public void iniciarCarrinho_BadRequest() throws Exception {
        var param = new CriarCarrinhoParam("Roberto Carlos");

        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any()))
                .thenThrow(IllegalArgumentException.class);

        mockMvc.perform(post("/carrinho")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void iniciarCarrinho_InternalServerError() throws Exception {
        var param = new CriarCarrinhoParam("Roberto Carlos");
        Mockito.when(usuarioLogadoTokenParser.verificarUsuarioLogado(any()))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(post("/carrinho")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void addItemCarrinho_Success() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(1001);

        Mockito.when(carrinhoUseCases.addItem(11, 1001)).thenReturn(CARRINHO_11);

        mockMvc.perform(post("/carrinho/11")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)));
    }

    @Test
    public void addItemCarrinho_BadRequest() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(null);

        mockMvc.perform(post("/carrinho/11")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemCarrinho_InternalServerError() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(1001);

        Mockito.when(carrinhoUseCases.addItem(11, 1001)).thenThrow(new RuntimeException("Unexpected Error"));

        mockMvc.perform(post("/carrinho/11")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteItemCarrinho_Success() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(1001);

        Mockito.when(carrinhoUseCases.deleteItem(11, 2)).thenReturn(CARRINHO_11);

        mockMvc.perform(delete("/carrinho/11/itens/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)));
    }

    @Test
    public void deleteItemCarrinho_BadRequest() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(null);

        Mockito.when(carrinhoUseCases.deleteItem(11, 2)).thenThrow(new IllegalArgumentException("Carrinho invalido"));

        mockMvc.perform(delete("/carrinho/11/itens/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteItemCarrinho_InternalServerError() throws Exception {
        AddItemCarrinhoDto param = new AddItemCarrinhoDto(1001);

        Mockito.when(carrinhoUseCases.deleteItem(11, 2)).thenThrow(new RuntimeException("Unexpected Error"));

        mockMvc.perform(delete("/carrinho/11/itens/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void setObservacoes_Success() throws Exception {
        CarrinhoObservacoesDto param = new CarrinhoObservacoesDto("Batata sem sal");

        Mockito.when(carrinhoUseCases.setObservacoes(11, "Batata sem sal")).thenReturn(CARRINHO_11);

        mockMvc.perform(put("/carrinho/11/obs")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)));
    }

    @Test
    public void setObservacoes_BadRequest() throws Exception {
        CarrinhoObservacoesDto param = new CarrinhoObservacoesDto("Batata sem sal");

        Mockito.when(carrinhoUseCases.setObservacoes(11, "Batata sem sal")).thenThrow(new IllegalArgumentException("Carrinho invalido"));

        mockMvc.perform(put("/carrinho/11/obs")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void setObservacoes_InternalServerError() throws Exception {
        CarrinhoObservacoesDto param = new CarrinhoObservacoesDto("Batata sem sal");

        Mockito.when(carrinhoUseCases.setObservacoes(11, "Batata sem sal")).thenThrow(new RuntimeException("Unexpected Error"));

        mockMvc.perform(put("/carrinho/11/obs")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isInternalServerError());
    }


    // //

    private static final CarrinhoDetalhe CARRINHO_11 = new CarrinhoDetalhe(
            new Carrinho(11, null, "Roberto Carlos",
                    List.of(new ItemPedido(1, 1001)),
                    null, LocalDateTime.of(2024, 11, 16, 12, 5)),
            Map.of(1001, new ItemCardapio(1001, TipoItemCardapio.LANCHE, "Hamburger", "Hamburger 100g",
                    new ValorMonetario("21.00")))
    );

    private static final CarrinhoDto CARRINHO_11_DTO = new CarrinhoDto(
            11, null, "Roberto Carlos",
            List.of(
                    new ItemPedidoDto(1, 1001, "LANCHE",
                            "Hamburger", "Hamburger 100g", new ValorMonetarioDto("21.00", "R$ 21,00"))
            ), null, new ValorMonetarioDto("21.00", "R$ 21,00"),
            1731769500000L
    );
}