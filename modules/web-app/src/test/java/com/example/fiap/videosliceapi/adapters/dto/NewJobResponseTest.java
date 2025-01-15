package com.example.fiap.videosliceapi.adapters.dto;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.entities.Job;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NewJobResponseTest {

    @Test
    void fromEntity_shouldMapJobToNewJobResponse() {
        Job job = Job.createJob(
                TestConstants.ID_1,
                "/inputs/input-file.mp4",
                10,
                Instant.now(),
                "user@example.com"
        );

        NewJobResponse response = NewJobResponse.fromEntity(job);

        assertThat(response.id()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(response.sliceIntervalSeconds()).isEqualTo(10);
        assertThat(response.message()).isEqualTo("Job created");
    }

    @Test
    void fromError_shouldReturnNewJobResponseWithErrorMessage() {
        NewJobResponse response = NewJobResponse.fromError("An error occurred");

        assertThat(response.id()).isNull();
        assertThat(response.sliceIntervalSeconds()).isNull();
        assertThat(response.message()).isEqualTo("An error occurred");
    }
}