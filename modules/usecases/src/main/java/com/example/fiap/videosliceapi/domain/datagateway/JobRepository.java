package com.example.fiap.videosliceapi.domain.datagateway;

import com.example.fiap.videosliceapi.domain.entities.Job;

import java.util.List;
import java.util.UUID;

public interface JobRepository {
    void saveNewJob(Job job);

    List<Job> findAllByUserId(String userId);

    Job findById(UUID id, boolean forUpdate);

    void updateMutableAttributes(Job job);
}
