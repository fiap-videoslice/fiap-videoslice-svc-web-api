package com.example.fiap.videosliceapi.domain.entities;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.exception.DomainArgumentException;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobTest {
    private static final Instant INSTANT_1 = Instant.ofEpochMilli(1736297743000L);
    private static final UUID ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void createJob() {
        var newJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "User_123_456");

        assertThat(newJob).isEqualTo(new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.CREATED,
                null, null, INSTANT_1, null, "User_123_456"
        ));
    }

    @Test
    void startProcessing_ValidState_SuccessfullyTransitionsToProcessing() {
        var newJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "User_123_456");
        var processingJob = newJob.startProcessing();

        assertThat(processingJob).isEqualTo(new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.PROCESSING,
                null, null, INSTANT_1, null, "User_123_456"
        ));
    }

    @Test
    void startProcessing_InvalidState_ThrowsException() {
        var alreadyProcessingJob = new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.PROCESSING,
                null, null, INSTANT_1, null, "User_123_456"
        );

        assertThrows(
                DomainArgumentException.class,
                alreadyProcessingJob::startProcessing
        );
    }

    @Test
    void errorProcessing_SuccessfullyTransitionsToFailed() {
        var processingJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "User_123_456")
                .startProcessing();
        var failedJob = processingJob.errorProcessing("An error occurred", INSTANT_1.plusSeconds(10));

        assertThat(failedJob).isEqualTo(new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.FAILED,
                null, "An error occurred", INSTANT_1, INSTANT_1.plusSeconds(10), "User_123_456"
        ));
    }

    @Test
    void errorProcessing_InvalidState_ThrowsException() {
        var completedJob = new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.COMPLETE,
                null, null, INSTANT_1, INSTANT_1.plusSeconds(120), "User_123_456"
        );

        assertThrows(
                DomainArgumentException.class,
                () -> completedJob.errorProcessing("Failed", INSTANT_1.plusSeconds(10))
        );
    }

    @Test
    void completeProcessing_ValidState_SuccessfullyTransitionsToComplete() {
        var processingJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "User_123_456")
                .startProcessing();
        var completedJob = processingJob.completeProcessing("/output-video.mp4", INSTANT_1.plusSeconds(120));

        assertThat(completedJob).isEqualTo(new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.COMPLETE,
                "/output-video.mp4", null, INSTANT_1, INSTANT_1.plusSeconds(120), "User_123_456"
        ));
    }

    @Test
    void completeProcessing_InvalidState_ThrowsException() {
        var failedJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "User_123_456")
                .errorProcessing("An error occurred", INSTANT_1.plusSeconds(30));

        assertThrows(
                DomainArgumentException.class,
                () -> failedJob.completeProcessing("/output-video.mp4", INSTANT_1.plusSeconds(120))
        );
    }
}