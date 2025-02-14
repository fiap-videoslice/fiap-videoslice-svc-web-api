package com.example.fiap.videosliceapi.domain.usecases;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.exception.DomainArgumentException;
import com.example.fiap.videosliceapi.domain.exception.DomainPermissionException;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.NotificationSender;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.usecasedto.CreateJobParam;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;
import com.example.fiap.videosliceapi.domain.usecasedto.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobUseCasesTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private VideoEngineService videoEngineService;
    @Mock
    private MediaStorage mediaStorage;
    @Mock
    private NotificationSender notificationSender;
    @Mock
    private Clock clock;
    @Mock
    private IdGenerator idGenerator;

    private JobUseCases jobUseCases;

    @BeforeEach
    void setUp() {
        jobUseCases = new JobUseCases(jobRepository, videoEngineService, mediaStorage,
                notificationSender, clock, idGenerator);
    }

    @Test
    void createNewJob() throws Exception {
        when(clock.now()).thenReturn(TestConstants.INSTANT_1);
        when(idGenerator.newId()).thenReturn(TestConstants.ID_1);

        when(mediaStorage.saveInputVideo(TestConstants.ID_1, TestConstants.VIDEO_BYTES)).thenReturn("/input/video_abcXyz.mp4");

        CreateJobParam param = new CreateJobParam(TestConstants.VIDEO_BYTES, 10);

        Job expectedNewJob = Job.createJob(TestConstants.ID_1, "/input/video_abcXyz.mp4",
                param.sliceIntervalSeconds(), TestConstants.INSTANT_1, "User_123_456");

        Job result = jobUseCases.createNewJob(param, "User_123_456");

        assertThat(result).isEqualTo(expectedNewJob);

        verify(jobRepository).saveNewJob(expectedNewJob);
        verify(videoEngineService).startProcess(expectedNewJob);
    }

    @Test
    void createNewJob_shouldRollbackStorageOnRepositoryException() {
        when(clock.now()).thenReturn(TestConstants.INSTANT_1);
        when(idGenerator.newId()).thenReturn(TestConstants.ID_1);
        when(mediaStorage.saveInputVideo(TestConstants.ID_1, TestConstants.VIDEO_BYTES)).thenReturn("/input/video_abcXyz.mp4");

        doThrow(new RuntimeException("Database error")).when(jobRepository).saveNewJob(any(Job.class));

        CreateJobParam param = new CreateJobParam(TestConstants.VIDEO_BYTES, 10);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> jobUseCases.createNewJob(param, "User_123_456"));

        assertThat(exception.getMessage()).isEqualTo("Database error");
        verify(mediaStorage).removeInputVideo(TestConstants.ID_1);
    }

    @Test
    void createNewJob_shouldRollbackStorageOnVideoEngineError() throws IOException {
        when(clock.now()).thenReturn(TestConstants.INSTANT_1);
        when(idGenerator.newId()).thenReturn(TestConstants.ID_1);
        when(mediaStorage.saveInputVideo(TestConstants.ID_1, TestConstants.VIDEO_BYTES)).thenReturn("/input/video_abcXyz.mp4");

        doThrow(new RuntimeException("Video engine unreachable")).when(videoEngineService).startProcess(any(Job.class));

        CreateJobParam param = new CreateJobParam(TestConstants.VIDEO_BYTES, 10);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> jobUseCases.createNewJob(param, "User_123_456"));

        assertThat(exception.getMessage()).contains("Video engine unreachable");
        verify(mediaStorage).removeInputVideo(TestConstants.ID_1);
    }

    @Test
    void listJobsFromUser() {
        List<Job> expectedJobs = List.of(
                Job.createJob(TestConstants.ID_1, "/input/video1.mp4", 10, TestConstants.INSTANT_1, "User_123_456"),
                new Job(TestConstants.ID_2, "/input/video2.mp4", 15,
                        JobStatus.COMPLETE,
                        "/output/video2.mp4", null, TestConstants.INSTANT_1,
                        TestConstants.INSTANT_2, "User_123_456")
        );

        when(jobRepository.findAllByUserId("User_123_456")).thenReturn(expectedJobs);

        List<Job> result = jobUseCases.listJobsFromUser("User_123_456");

        assertThat(result).isEqualTo(expectedJobs);
        verify(jobRepository).findAllByUserId("User_123_456");
    }

    @Test
    void listJobsFromUser_noJobsFound() {
        when(jobRepository.findAllByUserId("User_123_456")).thenReturn(List.of());

        List<Job> result = jobUseCases.listJobsFromUser("User_123_456");

        assertThat(result).isEmpty();
        verify(jobRepository).findAllByUserId("User_123_456");
    }

    @Test
    void updateJobStatus_toProcessing() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                TestConstants.INSTANT_1,
                "User_123_456");

        when(jobRepository.findById(TestConstants.ID_1, true)).thenReturn(job);

        JobResponse response = new JobResponse(TestConstants.ID_1, JobStatus.PROCESSING, null, null);

        jobUseCases.updateJobStatus(response);

        Job expectedJob = job.startProcessing();

        verify(jobRepository).updateMutableAttributes(expectedJob);

        verify(notificationSender, never()).sendFinishedJobNotification(any(), any());
    }

    @Test
    void updateJobStatus_toComplete() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                TestConstants.INSTANT_1,
                "User_123_456");

        when(jobRepository.findById(TestConstants.ID_1, true)).thenReturn(job);
        when(clock.now()).thenReturn(TestConstants.INSTANT_2);

        JobResponse response = new JobResponse(TestConstants.ID_1, JobStatus.COMPLETE, "/output/video1-frames.zip", null);

        DownloadLink expectedDownloadLink = new DownloadLink("https://download.example.com/video1-frames.zip", 60);
        when(mediaStorage.getOutputFileDownloadLink("/output/video1-frames.zip")).thenReturn(expectedDownloadLink);

        jobUseCases.updateJobStatus(response);

        Job expectedJob = job.completeProcessing("/output/video1-frames.zip", TestConstants.INSTANT_2);

        verify(jobRepository).updateMutableAttributes(expectedJob);

        verify(notificationSender).sendFinishedJobNotification(expectedJob, expectedDownloadLink);
    }

    @Test
    void updateJobStatus_toFailed() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                TestConstants.INSTANT_1,
                "User_123_456");

        when(jobRepository.findById(TestConstants.ID_1, true)).thenReturn(job);
        when(clock.now()).thenReturn(TestConstants.INSTANT_2);

        JobResponse response = new JobResponse(TestConstants.ID_1, JobStatus.FAILED, null, "Processing error");

        jobUseCases.updateJobStatus(response);

        Job expectedJob = job.errorProcessing("Processing error", TestConstants.INSTANT_2);

        verify(jobRepository).updateMutableAttributes(expectedJob);
        verify(notificationSender).sendFinishedJobNotification(expectedJob, null);

        /*
        SAGA demonstration of an 'undo' operation. When the video engine returns a failure we remove the input file
         */
        verify(mediaStorage).removeInputVideo(TestConstants.ID_1);
    }

    @Test
    void updateJobStatus_jobNotFound() {
        when(jobRepository.findById(TestConstants.ID_1, true)).thenReturn(null);

        JobResponse response = new JobResponse(TestConstants.ID_1, JobStatus.PROCESSING, null, null);

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> jobUseCases.updateJobStatus(response)
        );

        assertThat(exception.getMessage()).isEqualTo("Inconsistent response, job [123e4567-e89b-12d3-a456-426614174000] not found");
    }

    @Test
    void updateJobStatus_invalidStatusTransition() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                TestConstants.INSTANT_1,
                "User_123_456");

        when(jobRepository.findById(TestConstants.ID_1, true)).thenReturn(job);

        JobResponse response = new JobResponse(TestConstants.ID_1, JobStatus.CREATED, null, null);

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> jobUseCases.updateJobStatus(response)
        );

        assertThat(exception.getMessage()).isEqualTo("Invalid status for transition: CREATED");
    }

    @Test
    void getResultFileDownloadLink_success() throws DomainPermissionException {
        Job job = new Job(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                JobStatus.COMPLETE,
                "/output/video1-frames.zip",
                null,
                TestConstants.INSTANT_1,
                TestConstants.INSTANT_2,
                "User_123_456"
        );

        when(jobRepository.findById(TestConstants.ID_1, false)).thenReturn(job);

        DownloadLink expectedDownloadLink = new DownloadLink("https://download.example.com/video1-frames.zip", 60);
        when(mediaStorage.getOutputFileDownloadLink("/output/video1-frames.zip")).thenReturn(expectedDownloadLink);

        DownloadLink result = jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "User_123_456");

        assertThat(result).isEqualTo(expectedDownloadLink);
    }

    @Test
    void getResultFileDownloadLink_jobNotFound() {
        when(jobRepository.findById(TestConstants.ID_1, false)).thenReturn(null);

        DomainArgumentException exception = Assertions.assertThrows(DomainArgumentException.class,
                () -> jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "User_999_888"));

        assertThat(exception.getMessage()).isEqualTo("Job [123e4567-e89b-12d3-a456-426614174000] not found");
    }

    @Test
    void getResultFileDownloadLink_jobNotComplete() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                TestConstants.INSTANT_1,
                "User_123_456"
        );

        when(jobRepository.findById(TestConstants.ID_1, false)).thenReturn(job);

        DomainArgumentException exception = Assertions.assertThrows(DomainArgumentException.class,
                () -> jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "User_123_456"));

        assertThat(exception.getMessage()).isEqualTo("Job [123e4567-e89b-12d3-a456-426614174000] is not complete. Current status: CREATED");
    }

    @Test
    void getResultFileDownloadLink_userNotOwner_throwsException() {
        Job job = new Job(
                TestConstants.ID_1,
                "/input/video1.mp4",
                10,
                JobStatus.COMPLETE,
                "/output/video1-frames.zip",
                null,
                TestConstants.INSTANT_1,
                TestConstants.INSTANT_2,
                "User_123_456"
        );

        when(jobRepository.findById(TestConstants.ID_1, false)).thenReturn(job);

        DomainPermissionException exception = Assertions.assertThrows(
                DomainPermissionException.class,
                () -> jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "User_999_888")
        );

        assertThat(exception.getMessage()).isEqualTo("User is not the owner of the job");
    }
}