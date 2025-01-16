package com.example.fiap.videosliceapi.domain.entities;

import com.example.fiap.videosliceapi.domain.valueobjects.JobProgress;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record Job(
        @NotNull UUID id,

        @NotNull String inputFileUri,
        int sliceIntervalSeconds,

        @NotNull JobStatus status,
        @Nullable JobProgress progress,

        @Nullable String outputFileUri,
        @Nullable String errorMessage,

        @NotNull Instant startTime,
        @Nullable Instant endTime,

        @NotNull String userId
) {

    /**
     * Creates a new Job instance that is yet to be started
     *
     * @param inputFileUri         the URI of the input file (required)
     * @param sliceIntervalSeconds the slice interval in seconds (required)
     * @param startTime            the start time of the job (required)
     * @param userId            the user identification (required)
     * @return a new Job instance
     */
    public static Job createJob(
            @NotNull UUID id,
            @NotNull String inputFileUri,
            int sliceIntervalSeconds,
            @NotNull Instant startTime,
            @NotNull String userId
    ) {
        return new Job(
                id,
                inputFileUri,
                sliceIntervalSeconds,
                JobStatus.CREATED,
                null,
                null,
                null,
                startTime,
                null,
                userId
        );
    }

}
