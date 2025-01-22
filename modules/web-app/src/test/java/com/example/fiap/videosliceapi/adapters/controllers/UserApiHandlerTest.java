package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.DummyTokenParser;
import com.example.fiap.videosliceapi.adapters.auth.UserGroup;
import com.example.fiap.videosliceapi.adapters.dto.CreateUserRequest;
import com.example.fiap.videosliceapi.adapters.externalsystem.CognitoUserRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserApiHandlerTest {

    @Mock
    private CognitoUserRegistry cognitoUserRegistry;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserApiHandler userApiHandler = new UserApiHandler(cognitoUserRegistry, new DummyTokenParser());

        mockMvc = MockMvcBuilders.standaloneSetup(userApiHandler).build();
    }

    @Test
    void getLoggedUser() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "name": "Test User 1",
                          "email": "user1@fiap.example.com",
                          "group": "User",
                          "token": "User1"
                        }"""));
    }

    @Test
    void getLoggedUser_notAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Dummy InvalidToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_ok() throws Exception {
        when(cognitoUserRegistry.registerUser("New User", "newUser@fiap.example.com", UserGroup.User, "123456"))
                .thenReturn("User-abc-def");

        CreateUserRequest param = new CreateUserRequest("New User", "newUser@fiap.example.com", "User", "123456");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param))
                        .header("Authorization", "Dummy Admin"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": "User-abc-def"
                        }"""));
    }

    @Test
    void createUser_notAuthenticated() throws Exception {
        CreateUserRequest param = new CreateUserRequest("New User", "user@fiap.example.com", "User", "123456");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .header("Authorization", "Dummy InvalidToken")
                        .content(new ObjectMapper().writeValueAsString(param)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_insufficientPermissions() throws Exception {
        CreateUserRequest param = new CreateUserRequest("New User", "user@fiap.example.com", "User", "123456");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param))
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("This operation can only be performed by an Admin")));
    }

    @Test
    void createUser_invalidParameters() throws Exception {
        CreateUserRequest param = new CreateUserRequest("", "user@fiap.example.com", "InvalidGroup", "");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param))
                        .header("Authorization", "Dummy Admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_internalServerError() throws Exception {
        when(cognitoUserRegistry.registerUser("ServerError User", "user@fiap.example.com", UserGroup.User, "123456"))
                .thenThrow(new RuntimeException("Unexpected Error"));

        CreateUserRequest param = new CreateUserRequest("ServerError User", "user@fiap.example.com", "User", "123456");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(param))
                        .header("Authorization", "Dummy Admin")
                ).andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error while creating user")));
    }
}