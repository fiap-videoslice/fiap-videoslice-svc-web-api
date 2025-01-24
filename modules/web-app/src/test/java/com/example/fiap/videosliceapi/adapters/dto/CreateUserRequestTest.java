package com.example.fiap.videosliceapi.adapters.dto;//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateUserRequestTest {

    @Test
    void validate_ok() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", "User", "123456789");
        request.validate();
        // not thrown
    }

    @Test
    void validate_nullName_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                null, "user@example.com", "User", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must not be null or blank.");
    }

    @Test
    void validate_blankName_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "   ", "user@example.com", "User", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must not be null or blank.");
    }

    @Test
    void validate_nullEmail_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", null, "User", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null or blank.");
    }

    @Test
    void validate_blankEmail_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "   ", "User", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null or blank.");
    }

    @Test
    void validate_nullGroup_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", null, "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Group must not be null or blank.");
    }

    @Test
    void validate_blankGroup_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", "   ", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Group must not be null or blank.");
    }

    @Test
    void validate_invalidGroup_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", "InvalidGroup", "123456789");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("No enum constant .*UserGroup.InvalidGroup");
    }

    @Test
    void validate_nullPassword_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", "User", null);
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must not be null or blank.");
    }

    @Test
    void validate_blankPassword_throwsException() {
        CreateUserRequest request = new CreateUserRequest(
                "The User", "user@example.com", "User", "   ");
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must not be null or blank.");
    }


}