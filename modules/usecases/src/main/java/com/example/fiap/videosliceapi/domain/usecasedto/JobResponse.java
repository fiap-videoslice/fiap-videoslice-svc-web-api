package com.example.fiap.videosliceapi.domain.usecasedto;

import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;

import java.util.UUID;

public record JobResponse(
        UUID id,
        JobStatus status,
        String outputFileUri,
        String message
) {

}
