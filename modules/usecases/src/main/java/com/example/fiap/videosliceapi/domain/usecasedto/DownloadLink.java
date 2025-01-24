package com.example.fiap.videosliceapi.domain.usecasedto;

public record DownloadLink(
        String url,
        long expirationMinutes
) {
}
