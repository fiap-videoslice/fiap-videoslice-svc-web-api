package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.JobResponse;

import java.io.IOException;

public interface VideoEngineService {
    void startProcess(Job job) throws IOException;

    void receiveAvailableResponseMessages(ResponseCallback callback) throws IOException;

    public interface ResponseCallback {
        void consume(JobResponse response) throws Exception;
    }
}
