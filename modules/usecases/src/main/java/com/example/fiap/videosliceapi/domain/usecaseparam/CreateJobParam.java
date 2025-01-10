package com.example.fiap.videosliceapi.domain.usecaseparam;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record CreateJobParam(
        @NotNull String inputFileUri,
        int sliceIntervalSeconds,
        @NotNull Instant startTime,
        @NotNull String userIdentification
) {
}
