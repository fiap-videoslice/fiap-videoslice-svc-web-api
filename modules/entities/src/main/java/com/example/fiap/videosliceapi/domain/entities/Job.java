package com.example.fiap.videosliceapi.domain.entities;

import com.example.fiap.videosliceapi.domain.exception.DomainArgumentException;
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
                startTime,
                null,
                userId
        );
    }

    public Job startProcessing() {
        if (this.status != JobStatus.CREATED) {
            throw new DomainArgumentException("Invalid state to start processing: " + this.status);
        }

        return new Job(
                this.id,
                this.inputFileUri,
                this.sliceIntervalSeconds,
                JobStatus.PROCESSING,
                this.outputFileUri,
                this.errorMessage,
                this.startTime,
                this.endTime,
                this.userId
        );
    }

    public Job errorProcessing(String errorMessage, Instant endTime) {
        if (this.status != JobStatus.PROCESSING && this.status != JobStatus.CREATED) {
            throw new DomainArgumentException("Invalid state to mark error: " + this.status);
        }
        
        return new Job(
                this.id,
                this.inputFileUri,
                this.sliceIntervalSeconds,
                JobStatus.FAILED,
                this.outputFileUri,
                errorMessage,
                this.startTime,
                endTime,
                this.userId
        );
    }
    
    public Job completeProcessing(String outputFileUri, Instant endTime) {
        if (this.status != JobStatus.PROCESSING && this.status != JobStatus.CREATED) {
            throw new DomainArgumentException("Invalid state to complete: " + this.status);
        }
    
        return new Job(
                this.id,
                this.inputFileUri,
                this.sliceIntervalSeconds,
                JobStatus.COMPLETE,
                outputFileUri,
                this.errorMessage,
                this.startTime,
                endTime,
                this.userId
        );
    }
}
