package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IndexApiHandlerTest {
    @Mock
    private LoggedUserTokenParser loggedUserTokenParser;

    private MockMvc mockMvc;

    private IndexApiHandler indexApiHandler;

    @BeforeEach
    void setUp() {
        indexApiHandler = new IndexApiHandler(loggedUserTokenParser);

        mockMvc = MockMvcBuilders.standaloneSetup(indexApiHandler).build();
    }

    @Test
    void index() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void healthcheck() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk());
    }

//    @Test
//    void getClienteConectado_autenticado() throws Exception {
//        com.example.fiap.videosliceapi.adapters.auth.LoggedUser usuarioLogado = mock(com.example.fiap.videosliceapi.adapters.auth.LoggedUser.class);
//        when(usuarioLogado.autenticado()).thenReturn(true);
//
//        when(usuarioLogado.getNome()).thenReturn("Alice");
//        when(usuarioLogado.getEmail()).thenReturn("alice@example.com");
//        when(usuarioLogado.getCpf()).thenReturn("11122233344");
//        when(usuarioLogado.getGrupo()).thenReturn(null);
//        when(usuarioLogado.identityToken()).thenReturn("AbcDefGhi");
//
//        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
//        mockMvc.perform(get("/usuario/conectado"))
//                .andExpect(status().isOk())
//                .andExpect(content().json("""
//                    {
//                      "nome": "Alice",
//                      "email": "alice@example.com",
//                      "cpf": "11122233344",
//                      "grupo": "",
//                      "token": "AbcDefGhi"
//                    }
//                """
//                ));
//    }
//
//    @Test
//    void getClienteConectado_naoAutenticado() throws Exception {
//        com.example.fiap.videosliceapi.adapters.auth.LoggedUser usuarioLogado = mock(com.example.fiap.videosliceapi.adapters.auth.LoggedUser.class);
//        when(usuarioLogado.autenticado()).thenReturn(false);
//
//        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(usuarioLogado);
//        mockMvc.perform(get("/usuario/conectado"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void getClienteConectado_error() throws Exception {
//        com.example.fiap.videosliceapi.adapters.auth.LoggedUser loggedUser = mock(com.example.fiap.videosliceapi.adapters.auth.LoggedUser.class);
//        when(loggedUser.autenticado()).thenThrow(new RuntimeException("Something went wrong"));
//
//        when(usuarioLogadoTokenParser.verificarUsuarioLogado(any())).thenReturn(loggedUser);
//        mockMvc.perform(get("/usuario/conectado"))
//                .andExpect(status().is5xxServerError());
//    }
}