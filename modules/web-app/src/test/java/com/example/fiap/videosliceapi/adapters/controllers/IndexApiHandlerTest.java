package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IndexApiHandlerTest {
    private MockMvc mockMvc;

    private IndexApiHandler indexApiHandler;

    @BeforeEach
    void setUp() {
        indexApiHandler = new IndexApiHandler();

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
}