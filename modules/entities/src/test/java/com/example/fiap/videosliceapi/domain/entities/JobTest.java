package com.example.fiap.videosliceapi.domain.entities;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobTest {
    private static final Instant INSTANT_1 = Instant.ofEpochMilli(1736297743000L);
    private static final UUID ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void createJob() {
        var newJob = Job.createJob(ID_1, "/my-video.mp4", 3, INSTANT_1, "user@example");

        assertThat(newJob).isEqualTo(new Job(
                ID_1, "/my-video.mp4", 3,
                JobStatus.CREATED, null,
                null, null, INSTANT_1, null, "user@example"
        ));
    }
}