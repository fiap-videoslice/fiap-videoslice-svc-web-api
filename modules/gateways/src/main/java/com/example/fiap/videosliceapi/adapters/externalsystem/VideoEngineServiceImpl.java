package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VideoEngineServiceImpl implements VideoEngineService {
    private final VideoEngineServiceQueueApi queueApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoEngineServiceImpl(VideoEngineServiceQueueApi queueApi) {
        this.queueApi = queueApi;
    }

    @Override
    public void startProcess(Job job) throws IOException {
        VideoEngineRequest request = new VideoEngineRequest(job.id().toString(), job.inputFileUri(), job.sliceIntervalSeconds());
        queueApi.sendMessageRequestQueue(objectMapper.writeValueAsString(request));
    }

    private record VideoEngineRequest(
            String id,
            String inputFileUri,
            int sliceIntervalSeconds
    ) {

    }
}
