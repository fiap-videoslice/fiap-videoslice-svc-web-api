package com.example.fiap.videosliceapi.jobs;

import com.example.fiap.videosliceapi.adapters.datasource.TransactionManager;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EngineResponseReceiverTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineResponseReceiverTask.class);

    private final VideoEngineService videoEngineService;
    private final JobUseCases jobUseCases;
    private final TransactionManager transactionManager;

    @Autowired
    public EngineResponseReceiverTask(VideoEngineService videoEngineService, JobUseCases jobUseCases, TransactionManager transactionManager) {
        this.videoEngineService = videoEngineService;
        this.jobUseCases = jobUseCases;
        this.transactionManager = transactionManager;
    }

    @Scheduled(fixedDelay = 5000)
    public void readEngineResponse() {
        try {
            videoEngineService.receiveAvailableResponseMessages(
                    response -> transactionManager.runInTransaction(() -> {
                        jobUseCases.updateJobStatus(response);
                        return null;
                    })
            );
        } catch (Exception e) {
            LOGGER.error("Error receiving responses! {}", e, e);
        }
    }
}
