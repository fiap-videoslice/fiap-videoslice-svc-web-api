package com.example.fiap.videosliceapi.adapters.auth;//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class DummyTokenParserTest {

    private DummyTokenParser dummyTokenParser;

    @BeforeEach
    void setUp() {
        dummyTokenParser = new DummyTokenParser();
    }

    @Test
    void verifyLoggedUser_noIdentityTokenProvided() {
        HttpHeaders headers = new HttpHeaders();
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);
        assertThat(loggedUser.authenticated()).isFalse();
        assertThat(loggedUser.authError()).isEqualTo("IdentityToken is missing. Check DummyTokenParser for valid values");
    }

    @Test
    void verifyLoggedUser_validTokenUser1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("IdentityToken", "User1");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getName()).isEqualTo("Test User 1");
        assertThat(loggedUser.getEmail()).isEqualTo("user1@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.identityToken()).isEqualTo("User1");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_validTokenUser2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("IdentityToken", "User2");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getName()).isEqualTo("Test User 2");
        assertThat(loggedUser.getEmail()).isEqualTo("user2@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.identityToken()).isEqualTo("User2");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_validTokenAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("IdentityToken", "Admin");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getName()).isEqualTo("Test Admin");
        assertThat(loggedUser.getEmail()).isEqualTo("admin@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.Admin);
        assertThat(loggedUser.identityToken()).isEqualTo("Admin");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_invalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("IdentityToken", "InvalidToken");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isFalse();
        assertThat(loggedUser.getName()).isNull();
        assertThat(loggedUser.getEmail()).isNull();
        assertThat(loggedUser.getGroup()).isNull();
        assertThat(loggedUser.identityToken()).isEqualTo("InvalidToken");
        assertThat(loggedUser.authError()).isEqualTo("Invalid IdentityToken: InvalidToken. Check DummyTokenParser for valid values");
    }

    @Test
    void verifyLoggedUser_lowerCaseHeaderName() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("identitytoken", "User1");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getName()).isEqualTo("Test User 1");
        assertThat(loggedUser.getEmail()).isEqualTo("user1@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.identityToken()).isEqualTo("User1");
        assertThat(loggedUser.authError()).isNull();
    }
}