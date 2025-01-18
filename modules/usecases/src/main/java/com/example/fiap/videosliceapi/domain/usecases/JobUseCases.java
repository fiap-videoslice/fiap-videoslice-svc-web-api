package com.example.fiap.videosliceapi.domain.usecases;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;

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

    public List<Job> listJobsFromUser(String userId) {
        return jobRepository.findAllByUserId(userId);
    }

    public Job createNewJob(CreateJobParam param, String userId) {
        UUID uuid = idGenerator.newId();

        String uri = mediaStorage.saveInputVideo(uuid, param.inputFileContents());

        Job newJob = Job.createJob(uuid, uri, param.sliceIntervalSeconds(), clock.now(),
                userId);

        jobRepository.saveNewJob(newJob);
        try {
            videoEngineService.startProcess(newJob);
        } catch (Exception e) {
            throw new RuntimeException(e);   // TODO - Change status of request
        }

        return newJob;
    }

    public void updateJobStatus(JobResponse response) {
        Job job = jobRepository.findById(response.id(), true);

        if (job == null) {
            throw new RuntimeException("Inconsistent response, job [" + response.id() + "] not found");
        }

        Job updated;
        if (response.status() == JobStatus.PROCESSING) {
            updated = job.startProcessing();
        } else if (response.status() == JobStatus.COMPLETE) {
            updated = job.completeProcessing(response.outputFileUri(), clock.now());
        } else if (response.status() == JobStatus.FAILED) {
            updated = job.errorProcessing(response.message(), clock.now());
        } else {
            throw new RuntimeException("Invalid status for transition: " + response.status());
        }

        jobRepository.updateMutableAttributes(updated);
    }
}
