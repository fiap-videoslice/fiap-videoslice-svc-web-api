package com.example.fiap.videosliceapi.domain.valueobjects;

import java.util.UUID;

public record JobResponse(
        UUID id,
        JobStatus status,
        String outputFileUri,
        String message
) {

}
