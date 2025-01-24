package com.example.fiap.videosliceapi.adapters.dto;

import com.example.fiap.videosliceapi.domain.entities.Job;

public record JobDto(
        String id,

        int sliceIntervalSeconds,

        String status,
        String errorMessage,

        String startTime,
        String endTime
) {

    public static JobDto fromEntity(Job job) {
        return new JobDto(
                job.id().toString(),
                job.sliceIntervalSeconds(),
                job.status().toString(),
                job.errorMessage(),
                job.startTime().toString(),
                job.endTime() != null ? job.endTime().toString() : null
        );
    }
}
