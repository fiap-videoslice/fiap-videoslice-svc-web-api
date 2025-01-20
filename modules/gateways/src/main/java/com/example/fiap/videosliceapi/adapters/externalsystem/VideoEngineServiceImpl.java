package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;
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
                // Message is not removed from the queue, coming back after a few seconds
                // TO-DO : We currently do not have a retry limit, a proper control with a thrash queue must be implemented
                LOGGER.error("Error processing engine response: {} -- {}", e.getMessage(), message.body(), e);
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
