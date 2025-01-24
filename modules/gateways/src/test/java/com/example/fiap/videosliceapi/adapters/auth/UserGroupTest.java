package com.example.fiap.videosliceapi.adapters.auth;//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UserGroupTest {

    @Test
    void fromString() {
        assertThat(UserGroup.valueOf("User")).isEqualTo(UserGroup.User);
        assertThat(UserGroup.valueOf("Admin")).isEqualTo(UserGroup.Admin);
    }
}