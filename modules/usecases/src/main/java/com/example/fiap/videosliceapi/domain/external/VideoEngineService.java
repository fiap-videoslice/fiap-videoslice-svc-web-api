package com.example.fiap.videosliceapi.domain.external;

import com.example.fiap.videosliceapi.domain.entities.Job;

import java.io.IOException;

public interface VideoEngineService {
    void startProcess(Job job) throws IOException;
}
