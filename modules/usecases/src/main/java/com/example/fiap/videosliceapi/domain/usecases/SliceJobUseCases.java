package com.example.fiap.videosliceapi.domain.usecases;

import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.SliceJob;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;

public class SliceJobUseCases {
    private final JobRepository jobRepository;
    private final VideoEngineService videoEngineService;

    public SliceJobUseCases(JobRepository jobRepository, VideoEngineService videoEngineService) {
        this.jobRepository = jobRepository;
        this.videoEngineService = videoEngineService;
    }

    public SliceJob createNewJob(CreateJobParam param, LoggedUser loggedUser) {
        SliceJob newJob = SliceJob.createJob(param.inputFileUri(), param.sliceIntervalSeconds(), param.startTime(),
                loggedUser.getEmail());

        SliceJob savedJob = jobRepository.saveNewJob(newJob);
        videoEngineService.startProcess(savedJob);

        return savedJob;
    }
}
