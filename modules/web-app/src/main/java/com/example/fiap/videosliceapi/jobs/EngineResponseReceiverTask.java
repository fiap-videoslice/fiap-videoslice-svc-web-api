package com.example.fiap.videosliceapi.jobs;

import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EngineResponseReceiverTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineResponseReceiverTask.class);

    private final VideoEngineService videoEngineService;
    private final JobUseCases jobUseCases;

    public EngineResponseReceiverTask(VideoEngineService videoEngineService, JobUseCases jobUseCases) {
        this.videoEngineService = videoEngineService;
        this.jobUseCases = jobUseCases;
    }

    @Scheduled(fixedDelay = 5000)
    public void readEngineResponse() {
        try {
            videoEngineService.receiveAvailableResponseMessages(jobUseCases::updateJobStatus);
        } catch (Exception e) {
            LOGGER.error("Error receiving responses! {}", e, e);
        }
    }
}
