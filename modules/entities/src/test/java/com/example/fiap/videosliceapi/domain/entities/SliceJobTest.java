package com.example.fiap.videosliceapi.domain.entities;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SliceJobTest {
    private static final Instant INSTANT_1 = Instant.ofEpochMilli(1736297743000L);

    @Test
    void createJob() {
        var newJob = SliceJob.createJob("/my-video.mp4", 3, INSTANT_1, "user@example");

        assertThat(newJob).isEqualTo(new SliceJob(
                null, "/my-video.mp4", 3,
                JobStatus.CREATED, null,
                null, INSTANT_1, null, "user@example"
        ));
    }

    @Test
    void withId() {
        var newJob = SliceJob.createJob("/my-video.mp4", 3, INSTANT_1, "user@example");

        var withId = newJob.withId(123);

        assertThat(withId).isEqualTo(new SliceJob(
                123, "/my-video.mp4", 3,
                JobStatus.CREATED, null,
                null, INSTANT_1, null, "user@example"
        ));
    }
}