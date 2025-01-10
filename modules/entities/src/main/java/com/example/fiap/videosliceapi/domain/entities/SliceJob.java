package com.example.fiap.videosliceapi.domain.entities;

import com.example.fiap.videosliceapi.domain.valueobjects.JobProgress;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record SliceJob(
        @Nullable Integer id,

        @NotNull String inputFileUri,
        int sliceIntervalSeconds,

        @NotNull JobStatus status,
        @Nullable JobProgress progress,

        @Nullable String outputFileUri,
        @Nullable String errorMessage,

        @NotNull Instant startTime,
        @Nullable Instant endTime,

        @NotNull String userEmail
) {

    public SliceJob withId(@NotNull Integer newId) {
        return new SliceJob(
                newId,
                this.inputFileUri,
                this.sliceIntervalSeconds,
                this.status,
                this.progress,
                this.outputFileUri,
                this.errorMessage,
                this.startTime,
                this.endTime,
                this.userEmail
        );
    }

    /**
     * Creates a new Job instance that is yet to be started
     *
     * @param inputFileUri         the URI of the input file (required)
     * @param sliceIntervalSeconds the slice interval in seconds (required)
     * @param startTime            the start time of the job (required)
     * @param userIdentification   the user identification (required)
     * @return a new Job instance
     */
    public static SliceJob createJob(
            @NotNull String inputFileUri,
            int sliceIntervalSeconds,
            @NotNull Instant startTime,
            @NotNull String userIdentification
    ) {
        return new SliceJob(
                null,  // id
                inputFileUri,
                sliceIntervalSeconds,
                JobStatus.CREATED,
                null,  // progress
                null,  // outputFileUri
                null,
                startTime,
                null,  // endTime
                userIdentification
        );
    }

}
