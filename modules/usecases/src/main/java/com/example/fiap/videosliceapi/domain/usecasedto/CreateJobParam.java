package com.example.fiap.videosliceapi.domain.usecasedto;

import org.jetbrains.annotations.NotNull;

public record CreateJobParam(
        @NotNull byte[] inputFileContents,
        Integer sliceIntervalSeconds
) {
}
