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
        assertThat(loggedUser.authError()).isEqualTo("Authorization header is missing");
    }

    @Test
    void verifyLoggedUser_validTokenUser1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Dummy User1");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getUserId()).isEqualTo("Test-User-1");
        assertThat(loggedUser.getName()).isEqualTo("Test User 1");
        assertThat(loggedUser.getEmail()).isEqualTo("user1@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.idToken()).isEqualTo("User1");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_validTokenUser2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Dummy User2");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getUserId()).isEqualTo("Test-User-2");
        assertThat(loggedUser.getName()).isEqualTo("Test User 2");
        assertThat(loggedUser.getEmail()).isEqualTo("user2@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.idToken()).isEqualTo("User2");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_validTokenAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Dummy Admin");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getUserId()).isEqualTo("Test-User-Admin-1");
        assertThat(loggedUser.getName()).isEqualTo("Test Admin");
        assertThat(loggedUser.getEmail()).isEqualTo("admin@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.Admin);
        assertThat(loggedUser.idToken()).isEqualTo("Admin");
        assertThat(loggedUser.authError()).isNull();
    }

    @Test
    void verifyLoggedUser_invalidTokenFormat() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "InvalidToken");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isFalse();
        assertThat(loggedUser.getUserId()).isNull();
        assertThat(loggedUser.getName()).isNull();
        assertThat(loggedUser.getEmail()).isNull();
        assertThat(loggedUser.getGroup()).isNull();
        assertThat(loggedUser.idToken()).isNull();
        assertThat(loggedUser.authError()).isEqualTo("Authorization token has invalid format. Authorization: Dummy User1|User2|Admin");
    }

    @Test
    void verifyLoggedUser_invalidTokenValue() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Dummy InvalidIdentification");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isFalse();
        assertThat(loggedUser.getUserId()).isNull();
        assertThat(loggedUser.getName()).isNull();
        assertThat(loggedUser.getEmail()).isNull();
        assertThat(loggedUser.getGroup()).isNull();
        assertThat(loggedUser.idToken()).isEqualTo("InvalidIdentification");
        assertThat(loggedUser.authError()).isEqualTo("Authorization token is invalid. Authorization: Dummy User1|User2|Admin");
    }

    @Test
    void verifyLoggedUser_lowerCaseHeaderName() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", "Dummy User1");
        LoggedUser loggedUser = dummyTokenParser.verifyLoggedUser(headers);

        assertThat(loggedUser.authenticated()).isTrue();
        assertThat(loggedUser.getUserId()).isEqualTo("Test-User-1");
        assertThat(loggedUser.getName()).isEqualTo("Test User 1");
        assertThat(loggedUser.getEmail()).isEqualTo("user1@fiap.example.com");
        assertThat(loggedUser.getGroup()).isEqualTo(UserGroup.User);
        assertThat(loggedUser.idToken()).isEqualTo("User1");
        assertThat(loggedUser.authError()).isNull();
    }
}