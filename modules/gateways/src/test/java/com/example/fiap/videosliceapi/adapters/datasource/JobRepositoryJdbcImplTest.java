package com.example.fiap.videosliceapi.adapters.datasource;//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

        assertThrows(RuntimeException.class, () -> jobRepository.saveNewJob(mock()));
    }

    @Test
    void findAllByUserEmail_databaseError() throws SQLException {
        when(connectionInstance.prepareStatement(anyString())).thenThrow(new SQLException("Something went wrong"));

        assertThrows(RuntimeException.class, () -> jobRepository.findAllByUserEmail("abc@example.com"));
    }
}