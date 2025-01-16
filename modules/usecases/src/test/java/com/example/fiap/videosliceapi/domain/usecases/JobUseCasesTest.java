package com.example.fiap.videosliceapi.domain.usecases;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;
import com.example.fiap.videosliceapi.domain.valueobjects.JobProgress;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobUseCasesTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private VideoEngineService videoEngineService;
    @Mock
    private MediaStorage mediaStorage;
    @Mock
    private Clock clock;
    @Mock
    private IdGenerator idGenerator;

    private JobUseCases jobUseCases;

    @BeforeEach
    void setUp() {
        jobUseCases = new JobUseCases(jobRepository, videoEngineService, mediaStorage, clock, idGenerator);
    }

    @Test
    void createNewJob() throws Exception {
        when(clock.now()).thenReturn(TestConstants.INSTANT_1);
        when(idGenerator.newId()).thenReturn(TestConstants.ID_1);

        when(mediaStorage.saveInputVideo(TestConstants.ID_1, TestConstants.VIDEO_BYTES)).thenReturn("/input/video_abcXyz.mp4");

        CreateJobParam param = new CreateJobParam(TestConstants.VIDEO_BYTES, 10);

        Job expectedNewJob = Job.createJob(TestConstants.ID_1, "/input/video_abcXyz.mp4",
                param.sliceIntervalSeconds(), TestConstants.INSTANT_1, "test-user@example.com");

        Job result = jobUseCases.createNewJob(param, "test-user@example.com");

        assertThat(result).isEqualTo(expectedNewJob);

        verify(jobRepository).saveNewJob(expectedNewJob);
        verify(videoEngineService).startProcess(expectedNewJob);
    }

    @Test
    void listJobsFromUser() {
        List<Job> expectedJobs = List.of(
                Job.createJob(TestConstants.ID_1, "/input/video1.mp4", 10, TestConstants.INSTANT_1, "test-user@example.com"),
                new Job(TestConstants.ID_2, "/input/video2.mp4", 15,
                        JobStatus.FINISHED, new JobProgress(100, 100),
                        "/output/video2.mp4", null, TestConstants.INSTANT_1,
                        TestConstants.INSTANT_2, "test-user@example.com")
        );

        when(jobRepository.findAllByUserId("test-user@example.com")).thenReturn(expectedJobs);

        List<Job> result = jobUseCases.listJobsFromUser("test-user@example.com");

        assertThat(result).isEqualTo(expectedJobs);
        verify(jobRepository).findAllByUserId("test-user@example.com");
    }

    @Test
    void listJobsFromUser_noJobsFound() {
        when(jobRepository.findAllByUserId("test-user@example.com")).thenReturn(List.of());

        List<Job> result = jobUseCases.listJobsFromUser("test-user@example.com");

        assertThat(result).isEmpty();
        verify(jobRepository).findAllByUserId("test-user@example.com");
    }
}