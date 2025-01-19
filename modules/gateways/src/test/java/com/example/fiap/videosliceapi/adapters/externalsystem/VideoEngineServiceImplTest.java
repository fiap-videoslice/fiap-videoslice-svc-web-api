package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
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
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
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
        String userId = "User_ABC";

        Job job = Job.createJob(jobId, inputFileUri, sliceIntervalSeconds, startTime, userId);

        videoEngineServiceImpl.startProcess(job);

        @Language("JSON")
        String expectedMessage = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "path": "/inputs/input-file.mp4",
                  "timeFrame": 10
                }
                """;

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(queueApi, times(1)).sendMessageRequestQueue(messageCaptor.capture());

        String actualMessage = messageCaptor.getValue();
        JSONAssert.assertEquals(expectedMessage, actualMessage, JSONCompareMode.STRICT);
    }

    @Test
    void receiveAvailableResponseMessages_processValidMessages() throws IOException {
        var messageBody = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "status": "PROCESSED_OK",
                  "framesFilePath": "/outputs/output-file.mp4",
                  "path": "/input-file-not-used-in-response.mp4"
                }
                """;
        var messageSummary = new VideoEngineServiceQueueApi.MessageSummary(messageBody, "1");

        when(queueApi.receiveMessagesResponseQueue()).thenReturn(List.of(messageSummary));

        Consumer<JobResponse> callback = mock();

        videoEngineServiceImpl.receiveAvailableResponseMessages(callback);

        ArgumentCaptor<JobResponse> jobResponseCaptor = ArgumentCaptor.forClass(JobResponse.class);
        verify(callback).accept(jobResponseCaptor.capture());

        JobResponse jobResponse = jobResponseCaptor.getValue();
        assertThat(jobResponse).isNotNull();
        assertThat(jobResponse.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(jobResponse.status()).isEqualTo(JobStatus.COMPLETE);

        verify(queueApi).deleteMessagesResponseQueue(messageSummary);
    }

    @Test
    void receiveAvailableResponseMessages_processErrorMessages() throws IOException {
        var messageBody = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "status": "PROCESSED_ERROR",
                  "message": "The file is invalid"
                }
                """;
        var messageSummary = new VideoEngineServiceQueueApi.MessageSummary(messageBody, "1");

        when(queueApi.receiveMessagesResponseQueue()).thenReturn(List.of(messageSummary));

        Consumer<JobResponse> callback = mock();

        videoEngineServiceImpl.receiveAvailableResponseMessages(callback);

        ArgumentCaptor<JobResponse> jobResponseCaptor = ArgumentCaptor.forClass(JobResponse.class);
        verify(callback).accept(jobResponseCaptor.capture());

        JobResponse jobResponse = jobResponseCaptor.getValue();
        assertThat(jobResponse).isNotNull();
        assertThat(jobResponse.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(jobResponse.status()).isEqualTo(JobStatus.FAILED);
        assertThat(jobResponse.message()).isEqualTo("The file is invalid");

        verify(queueApi).deleteMessagesResponseQueue(messageSummary);
    }

    @Test
    void receiveAvailableResponseMessages_logAndContinueOnException() throws IOException {
        var invalidMessageBody = """
                !!Not_A_JSON_Message!!
                """;
        var invalidMessageSummary = new VideoEngineServiceQueueApi.MessageSummary(invalidMessageBody, "1");

        var validMessageBody = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "status": "IN_PROCESS",
                  "framesFilePath": "/outputs/output-file.mp4"
                }
                """;
        var validMessageSummary = new VideoEngineServiceQueueApi.MessageSummary(validMessageBody, "2");

        when(queueApi.receiveMessagesResponseQueue()).thenReturn(List.of(invalidMessageSummary, validMessageSummary));

        Consumer<JobResponse> callback = mock();

        videoEngineServiceImpl.receiveAvailableResponseMessages(callback);

        ArgumentCaptor<JobResponse> jobResponseCaptor = ArgumentCaptor.forClass(JobResponse.class);
        verify(callback, times(1)).accept(jobResponseCaptor.capture());

        JobResponse jobResponse = jobResponseCaptor.getValue();
        assertThat(jobResponse).isNotNull();
        assertThat(jobResponse.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(jobResponse.status()).isEqualTo(JobStatus.PROCESSING);

        verify(queueApi).deleteMessagesResponseQueue(validMessageSummary);
        verify(queueApi, never()).deleteMessagesResponseQueue(invalidMessageSummary);
    }
}