package com.example.fiap.videosliceapi.adapters.testUtils;

import java.time.Instant;
import java.util.UUID;

public class TestConstants {

    public static final UUID ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID ID_2 = UUID.fromString("228cc54f-4ff0-41b3-b407-15b095f92614");

    // Tue Jan 07 2025 21:55:43 GMT-0300
    public static final Instant INSTANT_1 = Instant.ofEpochMilli(1736297743000L);

    // Tue Jan 07 2025 21:57:35 GMT-0300
    public static final Instant INSTANT_2 = Instant.ofEpochMilli(1736297855000L);

    public static final byte[] VIDEO_BYTES = new byte[] { 0x00, 0x01, 0x02, 0x03 };
}
