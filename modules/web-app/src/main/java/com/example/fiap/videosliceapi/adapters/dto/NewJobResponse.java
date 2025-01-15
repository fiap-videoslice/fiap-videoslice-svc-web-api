package com.example.fiap.videosliceapi.adapters.dto;

import com.example.fiap.videosliceapi.domain.entities.Job;

public record NewJobResponse(
        String id,
        Integer sliceIntervalSeconds,
        String message
) {

    public static NewJobResponse fromEntity(Job job) {
        return new NewJobResponse(job.id().toString(), job.sliceIntervalSeconds(), "Job created");
    }

    public static NewJobResponse fromError(String message) {
        return new NewJobResponse(null, null, message);
    }
}
