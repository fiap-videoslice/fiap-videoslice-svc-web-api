package com.example.fiap.videosliceapi.adapters.dto;

import com.example.fiap.videosliceapi.domain.entities.Job;

public record JobDto(
        String id,

        String inputFileUri,
        int sliceIntervalSeconds,

        String status,
        int progressCurrent,
        int progressTotal,

        String outputFileUri,
        String errorMessage,

        String startTime,
        String endTime,

        String userId
) {

    public static JobDto fromEntity(Job job) {
        return new JobDto(
                job.id().toString(),
                job.inputFileUri(),
                job.sliceIntervalSeconds(),
                job.status().toString(),
                job.progress() != null ? job.progress().current() : 0,
                job.progress() != null ? job.progress().total() : 0,
                job.outputFileUri(),
                job.errorMessage(),
                job.startTime().toString(),
                job.endTime() != null ? job.endTime().toString() : null,
                job.userId()
        );
    }
}
