package com.example.fiap.videosliceapi.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.entities.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRepositoryJdbcImplTest {

    @Mock
    private DatabaseConnection databaseConnection;
    @Mock
    private DatabaseConnection.ConnectionInstance connectionInstance;

    private JobRepositoryJdbcImpl jobRepository;

    @BeforeEach
    void setUp() {
        when(databaseConnection.getConnection()).thenReturn(connectionInstance);

        jobRepository = new JobRepositoryJdbcImpl(databaseConnection);
    }

    @Test
    void saveNewJob_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        Job job = Job.createJob(UUID.randomUUID(),
                "/inputs/file.mp4", 10, Instant.now(), "User_ABC");

        assertThatThrownBy(() -> jobRepository.saveNewJob(job))
                .hasMessageContaining("Database error: Something went wrong");
    }

    @Test
    void findAllByUserId_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThatThrownBy(() -> jobRepository.findAllByUserId("User_ABC"))
                .hasMessageContaining("Database error: Something went wrong");
    }
}