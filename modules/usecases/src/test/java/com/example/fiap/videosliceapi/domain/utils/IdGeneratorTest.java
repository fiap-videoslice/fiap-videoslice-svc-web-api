package com.example.fiap.videosliceapi.domain.utils;//import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class IdGeneratorTest {

    @Test
    void shouldGenerateUniqueId() {
        IdGenerator generator = new IdGenerator();
        UUID id1 = generator.newId();
        UUID id2 = generator.newId();

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldGenerateValidUuid() {
        IdGenerator generator = new IdGenerator();
        UUID id = generator.newId();

        assertThat(id).isNotNull();
        assertThat(id.toString()).matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}