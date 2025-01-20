package com.example.fiap.videosliceapi.domain.external;

import java.util.UUID;

public interface MediaStorage {
    /**
     * @return The uri of the saved file within this same storage mechanism
     */
    String saveInputVideo(UUID uuid, byte[] videoBytes);

    /**
     * Removes the video - to be used when the request is cancelled for example
     */
    void removeInputVideo(UUID uuid);
}
