package com.example.fiap.videosliceapi.presenters;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;

class EmailNotificationPresenterTest {
    private EmailNotificationPresenter emailNotificationPresenter;

    @BeforeEach
    void setUp() {
        emailNotificationPresenter = new EmailNotificationPresenter();
    }

    @Test
    void shouldReturnCompleteNotificationTitleWhenJobIsComplete() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job completeJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());

        String title = emailNotificationPresenter.finishedJobNotificationTitle(completeJob);
        assertThat(title).isEqualTo("Your Video slice job is complete");
    }

    @Test
    void shouldReturnFailedNotificationTitleWhenJobIsFailed() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job failedJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .errorProcessing("Invalid video file", Instant.now());

        String title = emailNotificationPresenter.finishedJobNotificationTitle(failedJob);
        assertThat(title).isEqualTo("YOUR VIDEO SLICE JOB HAS FAILED");
    }

    @Test
    void shouldThrowExceptionWhenJobStatusIsUnexpected() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job newJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC");

        assertThrows(IllegalStateException.class, () -> emailNotificationPresenter.finishedJobNotificationTitle(newJob));
    }

    @Test
    void shouldReturnCompleteNotificationBodyWhenJobIsComplete() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job completeJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());
        DownloadLink downloadLink = new DownloadLink("https://download.example.com/video1-frames.zip", 60);

        String body = emailNotificationPresenter.finishedJobNotificationBody(completeJob, downloadLink);
        assertThat(body).contains("Your VideoSlice job id=<span style=\"font-weight: bold\">f4881488-1091-70b1-21fe-6dc5cce9c313</span> has completed successfully");
        assertThat(body).contains("https://download.example.com/video1-frames.zip");
        assertThat(body).contains("60 minutes");
    }

    @Test
    void shouldReturnFailedNotificationBodyWhenJobIsFailed() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job failedJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .errorProcessing("Invalid video file", Instant.now());

        String body = emailNotificationPresenter.finishedJobNotificationBody(failedJob, null);
        assertThat(body).contains("Your VideoSlice job id=<span style=\"font-weight: bold\">f4881488-1091-70b1-21fe-6dc5cce9c313</span> has failed");
        assertThat(body).contains("Invalid video file");
    }

    @Test
    void shouldThrowExceptionWhenJobStatusIsUnexpectedInBody() {
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");
        Job newJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC");

        assertThatThrownBy(() -> emailNotificationPresenter.finishedJobNotificationBody(newJob, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Notification not expected for status CREATED");
    }
}