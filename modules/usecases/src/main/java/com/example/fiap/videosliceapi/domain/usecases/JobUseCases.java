package com.example.fiap.videosliceapi.domain.usecases;

import com.example.fiap.videosliceapi.domain.exception.DomainPermissionException;
import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JobUseCases {
    private final JobRepository jobRepository;
    private final VideoEngineService videoEngineService;
    private final MediaStorage mediaStorage;
    private final Clock clock;
    private final IdGenerator idGenerator;

    public JobUseCases(JobRepository jobRepository, VideoEngineService videoEngineService,
                       MediaStorage mediaStorage,
                       Clock clock, IdGenerator idGenerator) {
        this.jobRepository = jobRepository;
        this.videoEngineService = videoEngineService;
        this.mediaStorage = mediaStorage;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public List<Job> listJobsFromUser(LoggedUser loggedUser) {
        if (!loggedUser.authenticated()) {
            throw new DomainPermissionException("User must be authenticated to list jobs");
        }

        return jobRepository.findAllByUserEmail(loggedUser.getEmail());
    }

    public Job createNewJob(CreateJobParam param, LoggedUser loggedUser) {
        if (!loggedUser.authenticated()) {
            throw new DomainPermissionException("User must be authenticated to start a job");
        }

        UUID uuid = idGenerator.newId();

        String uri = mediaStorage.saveInputVideo(uuid, param.inputFileContents());

        Job newJob = Job.createJob(uuid, uri, param.sliceIntervalSeconds(), clock.now(),
                loggedUser.getEmail());

        jobRepository.saveNewJob(newJob);
        try {
            videoEngineService.startProcess(newJob);
        } catch (Exception e) {
            throw new RuntimeException(e);   // TODO - Change status of request
        }

        return newJob;
    }
}
