package com.example.fiap.archburgers.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.auth.UsuarioLogadoTokenParser;
import com.example.fiap.archburgers.adapters.dto.ClienteDto;
import com.example.fiap.archburgers.adapters.dto.ClienteWithTokenDto;
import com.example.fiap.archburgers.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.archburgers.domain.entities.Cliente;
import com.example.fiap.archburgers.domain.exception.DomainPermissionException;
import com.example.fiap.archburgers.domain.usecaseparam.CadastrarClienteParam;
import com.example.fiap.archburgers.domain.usecases.ClienteUseCases;
import com.example.fiap.archburgers.domain.valueobjects.Cpf;
import com.example.fiap.archburgers.domain.valueobjects.IdCliente;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteApiHandlerTest {
    @Mock
    private ClienteUseCases clienteUseCases;
    @Mock
    private UsuarioLogadoTokenParser usuarioLogadoTokenParser;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ClienteApiHandler clienteApiHandler = new ClienteApiHandler(clienteUseCases, usuarioLogadoTokenParser,
                new DummyTransactionManager());

        mockMvc = MockMvcBuilders.standaloneSetup(clienteApiHandler).build();
    }

    @Test
    void getClienteConectado() throws Exception {
        var cliente = new Cliente(new IdCliente(1), "Alice", new Cpf("12332112340"), "alice@example.com");

        var usuarioLogado = mock(com.example.fiap.archburgers.domain.auth.LoggedUser.class);
        when(usuarioLogado.identityToken()).thenReturn("AbcEfgHijKlm");

        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
        when(clienteUseCases.getClienteByCredencial(usuarioLogado)).thenReturn(cliente);

        mockMvc.perform(get("/cliente/conectado"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(new ClienteWithTokenDto(
                                1, "Alice", "12332112340", "alice@example.com", "AbcEfgHijKlm"))
                ));
    }

    @Test
    void getClienteConectado_noPermission() throws Exception {
        var usuarioLogado = mock(com.example.fiap.archburgers.domain.auth.LoggedUser.class);
        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);

        when(clienteUseCases.getClienteByCredencial(usuarioLogado)).thenThrow(new DomainPermissionException("Nao autenticado"));

        mockMvc.perform(get("/cliente/conectado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getClienteConectado_serverError() throws Exception {
        var usuarioLogado = mock(com.example.fiap.archburgers.domain.auth.LoggedUser.class);
        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);

        when(clienteUseCases.getClienteByCredencial(usuarioLogado)).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/cliente/conectado"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void listClientes() throws Exception {
        List<Cliente> allClientes = List.of(
                new Cliente(new IdCliente(1), "Alice", new Cpf("12332112340"), "alice@example.com"),
                new Cliente(new IdCliente(2), "Bob", new Cpf("99988877714"), "bob@example.com")
        );
        List<ClienteDto> allClientesPresentation = List.of(
                new ClienteDto(1, "Alice", "12332112340", "alice@example.com"),
                new ClienteDto(2, "Bob", "99988877714", "bob@example.com")
        );

        when(clienteUseCases.listTodosClientes()).thenReturn(allClientes);

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(allClientesPresentation)
                ));
    }

    @Test
    void salvarCliente() throws Exception {
        Cliente savedCliente = new Cliente(new IdCliente(55), "Alice", new Cpf("12332112340"), "alice@example.com");
        ClienteDto savedClientePresentation = new ClienteDto(55, "Alice", "12332112340", "alice@example.com");

        when(clienteUseCases.cadastrarCliente(new CadastrarClienteParam(
                "Alice", "12332112340", "alice@example.com", "xxYYzz"))).thenReturn(savedCliente);

        var paramMap = Map.of(
                "nome", "Alice",
                "cpf", "12332112340",
                "email", "alice@example.com",
                "senha", "xxYYzz"
        );

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(paramMap)))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(savedClientePresentation)
                ));
    }

    @Test
    void salvarCliente_requestError() throws Exception {
        when(clienteUseCases.cadastrarCliente(any()))
                .thenThrow(new IllegalArgumentException("Missing the arguments"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void salvarCliente_serverError() throws Exception {
        when(clienteUseCases.cadastrarCliente(any()))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is5xxServerError());
    }
}