package com.example.fiap.videosliceapi.domain.usecases;

import com.example.fiap.videosliceapi.domain.datagateway.JobRepository;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import com.example.fiap.videosliceapi.domain.external.NotificationSender;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecasedto.CreateJobParam;
import com.example.fiap.videosliceapi.domain.utils.Clock;
import com.example.fiap.videosliceapi.domain.utils.IdGenerator;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class JobUseCases {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobUseCases.class);

    private final JobRepository jobRepository;
    private final VideoEngineService videoEngineService;
    private final MediaStorage mediaStorage;
    private final NotificationSender notificationSender;
    private final Clock clock;
    private final IdGenerator idGenerator;

    public JobUseCases(JobRepository jobRepository, VideoEngineService videoEngineService,
                       MediaStorage mediaStorage, NotificationSender notificationSender,
                       Clock clock, IdGenerator idGenerator) {
        this.jobRepository = jobRepository;
        this.videoEngineService = videoEngineService;
        this.mediaStorage = mediaStorage;
        this.notificationSender = notificationSender;
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

        try {
            jobRepository.saveNewJob(newJob);
        } catch (Exception e) {
            LOGGER.error("Error saving new job request: {}", e, e);

            // Transaction control:
            // The media storage cannot be automatically controlled by the same transaction mechanism.
            // "Rollback" of the previous step needs to be explicitly called

            mediaStorage.removeInputVideo(uuid);
            throw e;
        }

        try {
            videoEngineService.startProcess(newJob);
        } catch (Exception e) {
            LOGGER.error("Error sending request to video engine: {}", e, e);

            // Transaction control:
            // Explicit "rollback" of the first step - save to video storage
            // The second step (database save) is rolled back automatically on exception
            mediaStorage.removeInputVideo(uuid);

            throw new RuntimeException(e);
        }

        return newJob;
    }

    public void updateJobStatus(JobResponse response) {
        Job job = jobRepository.findById(response.id(), true);

        if (job == null) {
            throw new RuntimeException("Inconsistent response, job [" + response.id() + "] not found");
        }

        boolean isFinishedStatus = false;

        Job updated;
        if (response.status() == JobStatus.PROCESSING) {
            updated = job.startProcessing();
        } else if (response.status() == JobStatus.COMPLETE) {
            updated = job.completeProcessing(response.outputFileUri(), clock.now());
            isFinishedStatus = true;

        } else if (response.status() == JobStatus.FAILED) {
            updated = job.errorProcessing(response.message(), clock.now());
            isFinishedStatus = true;

            /*
            SAGA demonstration of an 'undo' operation. When the video engine returns a failure we remove the input file
             */
            mediaStorage.removeInputVideo(job.id());

        } else {
            throw new RuntimeException("Invalid status for transition: " + response.status());
        }

        jobRepository.updateMutableAttributes(updated);

        if (isFinishedStatus) {
            notificationSender.sendFinishedJobNotification(updated);
        }
    }
}
