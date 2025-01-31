package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecasedto.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class VideoEngineServiceImpl implements VideoEngineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoEngineServiceImpl.class);

    private final VideoEngineServiceQueueApi queueApi;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public VideoEngineServiceImpl(VideoEngineServiceQueueApi queueApi) {
        this.queueApi = queueApi;
    }

    @Override
    public void startProcess(Job job) throws IOException {
        VideoEngineRequest request = new VideoEngineRequest(job.id().toString(), job.inputFileUri(), job.sliceIntervalSeconds());
        queueApi.sendMessageRequestQueue(objectMapper.writeValueAsString(request));
    }

    @Override
    public void receiveAvailableResponseMessages(ResponseCallback callback) {
        List<VideoEngineServiceQueueApi.MessageSummary> messages = queueApi.receiveMessagesResponseQueue();

        messages.forEach(message -> {
            try {
                VideoEngineResponse response = objectMapper.readValue(message.body(), VideoEngineResponse.class);

                JobResponse jobResponse = response.toEntity();

                LOGGER.info("Response received from engine to job {}", response.id());

                callback.consume(jobResponse);

                queueApi.deleteMessagesResponseQueue(message);

            } catch (Exception e) {
                LOGGER.error("Error processing engine response: {} -- {}", e.getMessage(), message.body(), e);

                // TO-DO: Improve error handling: today the operator must look at the failures manually from logs.
                // Improve the storage of these failures, and/or implement a reprocess queue.
                queueApi.deleteMessagesResponseQueue(message);
            }
        });
    }

    private record VideoEngineRequest(
            String id,
            String path,
            int timeFrame
    ) {
    }

    private record VideoEngineResponse(
            String id,
            String status,
            String framesFilePath,
            String message
    ) {

        public JobResponse toEntity() {
            JobStatus mappedStatus = switch (status) {
                case "PROCESSED_OK" -> JobStatus.COMPLETE;
                case "PROCESSED_ERROR" -> JobStatus.FAILED;
                case "IN_PROCESS" -> JobStatus.PROCESSING;
                default -> throw new IllegalStateException("Unknown status from engine service: " + status);
            };

            return new JobResponse(
                    UUID.fromString(this.id),
                    mappedStatus,
                    framesFilePath,
                    message
            );
        }
    }
}
