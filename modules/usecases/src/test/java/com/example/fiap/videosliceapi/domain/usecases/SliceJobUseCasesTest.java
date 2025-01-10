package com.example.fiap.videosliceapi.domain.usecases;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.testUtils.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.fiap.videosliceapi.domain.entities.SliceJob;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.auth.LoggedUser;

@ExtendWith(MockitoExtension.class)
class SliceJobUseCasesTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private VideoEngineService videoEngineService;
    @Mock
    private LoggedUser loggedUser;

    private SliceJobUseCases sliceJobUseCases;

    @BeforeEach
    void setUp() {
        sliceJobUseCases = new SliceJobUseCases(jobRepository, videoEngineService);

        when(loggedUser.getEmail()).thenReturn("test-user@example.com");
    }

    @Test
    void createNewJob() {
        // Arrange
        CreateJobParam param = new CreateJobParam("fileUri", 10, TestConstants.INSTANT_1, "test-user");

        SliceJob expectedNewJob = SliceJob.createJob(param.inputFileUri(), param.sliceIntervalSeconds(), param.startTime(), loggedUser.getEmail());
        SliceJob savedJob = expectedNewJob.withId(123);

        when(jobRepository.saveNewJob(expectedNewJob)).thenReturn(savedJob);

        SliceJob result = sliceJobUseCases.createNewJob(param, loggedUser);

        assertThat(result).isEqualTo(savedJob);
        assertThat(result.userEmail()).isEqualTo("test-user@example.com");

        verify(jobRepository).saveNewJob(expectedNewJob);
        verify(videoEngineService).startProcess(savedJob);
    }
}