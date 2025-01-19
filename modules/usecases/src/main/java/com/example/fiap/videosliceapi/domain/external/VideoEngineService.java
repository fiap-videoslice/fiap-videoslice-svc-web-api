package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;

import java.io.IOException;
import java.util.function.Consumer;

public interface VideoEngineService {
    void startProcess(Job job) throws IOException;

    void receiveAvailableResponseMessages(Consumer<JobResponse> callback) throws IOException;
}
