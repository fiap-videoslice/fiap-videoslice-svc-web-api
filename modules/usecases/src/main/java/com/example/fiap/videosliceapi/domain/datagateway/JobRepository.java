package com.example.fiap.videosliceapi.domain.datagateway;

import com.example.fiap.videosliceapi.domain.entities.Job;

import java.util.List;

public interface JobRepository {
    void saveNewJob(Job job);

    List<Job> findAllByUserId(String userId);
}
