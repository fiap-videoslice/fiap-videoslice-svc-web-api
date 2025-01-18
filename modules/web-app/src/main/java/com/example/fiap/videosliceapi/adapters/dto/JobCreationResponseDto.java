package com.example.fiap.videosliceapi.adapters.dto;

import com.example.fiap.videosliceapi.domain.entities.Job;

public record JobCreationResponseDto(
        String id,
        Integer sliceIntervalSeconds,
        String message
) {

    public static JobCreationResponseDto fromEntity(Job job) {
        return new JobCreationResponseDto(job.id().toString(), job.sliceIntervalSeconds(), "Job created");
    }

    public static JobCreationResponseDto fromError(String message) {
        return new JobCreationResponseDto(null, null, message);
    }
}
