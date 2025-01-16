package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoEngineServiceImplTest {

    @Mock
    private VideoEngineServiceQueueApi queueApi;

    private VideoEngineServiceImpl videoEngineServiceImpl;

    @BeforeEach
    void setUp() {
        videoEngineServiceImpl = new VideoEngineServiceImpl(queueApi);
    }

    @Test
    void startProcess_sendMessageToQueue() throws IOException {
        UUID jobId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String inputFileUri = "/inputs/input-file.mp4";
        int sliceIntervalSeconds = 10;
        Instant startTime = Instant.now();
        String userId = "user@example.com";

        Job job = Job.createJob(jobId, inputFileUri, sliceIntervalSeconds, startTime, userId);

        videoEngineServiceImpl.startProcess(job);

        @Language("JSON")
        String expectedMessage = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "inputFileUri": "/inputs/input-file.mp4",
                  "sliceIntervalSeconds": 10
                }
                """;

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(queueApi, times(1)).sendMessageRequestQueue(messageCaptor.capture());

        String actualMessage = messageCaptor.getValue();
        JSONAssert.assertEquals(expectedMessage, actualMessage, JSONCompareMode.STRICT);
    }
}