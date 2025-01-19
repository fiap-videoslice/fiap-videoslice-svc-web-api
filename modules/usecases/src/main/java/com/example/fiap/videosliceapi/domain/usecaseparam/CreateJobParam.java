package com.example.fiap.videosliceapi.domain.usecaseparam;

import org.jetbrains.annotations.NotNull;

public record CreateJobParam(
        @NotNull byte[] inputFileContents,
        Integer sliceIntervalSeconds
) {
}
