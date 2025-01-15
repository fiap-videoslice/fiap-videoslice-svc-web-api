package com.example.fiap.videosliceapi.adapters.controllers;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.adapters.datasource.TransactionManager;
import com.example.fiap.videosliceapi.adapters.dto.JobDto;
import com.example.fiap.videosliceapi.adapters.dto.NewJobResponse;
import com.example.fiap.videosliceapi.apiutils.WebUtils;
import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.exception.DomainArgumentException;
import com.example.fiap.videosliceapi.domain.exception.DomainPermissionException;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class JobApiHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobApiHandler.class);

    private final JobUseCases jobUseCases;
    private final LoggedUserTokenParser loggedUserTokenParser;
    private final TransactionManager transactionManager;

    @Autowired
    public JobApiHandler(JobUseCases jobUseCases,
                         LoggedUserTokenParser loggedUserTokenParser,
                         TransactionManager transactionManager) {
        this.jobUseCases = jobUseCases;
        this.loggedUserTokenParser = loggedUserTokenParser;
        this.transactionManager = transactionManager;
    }

    @Operation(summary = "List all jobs belonging to the logged user")
    @GetMapping(path = "/jobs", produces = "application/json")
    public ResponseEntity<List<JobDto>> listUserJobs(@RequestHeader HttpHeaders headers) {
        try {
            LoggedUser loggedUser = loggedUserTokenParser.verifyLoggedUser(headers);
            List<Job> jobs = jobUseCases.listJobsFromUser(loggedUser);
            return WebUtils.okResponse(jobs.stream().map(JobDto::fromEntity).toList());

        } catch (DomainPermissionException dae) {
            return WebUtils.errorResponse(HttpStatus.UNAUTHORIZED, dae.getMessage());
        } catch (DomainArgumentException iae) {
            return WebUtils.errorResponse(HttpStatus.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error while listing jobs: {}", e, e);
            return WebUtils.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error while listing jobs");
        }
    }

    @Operation(summary = "Starts a new Video Processing Job",
            description = "Uploads a video file and the sliceIntervalSeconds metadata, using a multi-part form")
    @PostMapping(path = "/jobs", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<NewJobResponse> startNewJob(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile videoFile,
            @RequestParam("sliceIntervalSeconds") String sliceIntervalSecondsParam) {

        if (videoFile == null || videoFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    NewJobResponse.fromError("Missing video file"));
        }

        Integer sliceIntervalSeconds = Integer.parseInt(sliceIntervalSecondsParam);

        Job newJob;

        try {
            byte[] fileContent = videoFile.getBytes();
            LoggedUser loggedUser = loggedUserTokenParser.verifyLoggedUser(headers);

            newJob = transactionManager.runInTransaction(() -> jobUseCases.createNewJob(
                    new CreateJobParam(fileContent, sliceIntervalSeconds), loggedUser));

        } catch (DomainPermissionException dae) {
            return WebUtils.errorResponse(HttpStatus.UNAUTHORIZED, dae.getMessage());
        } catch (DomainArgumentException iae) {
            return WebUtils.errorResponse(HttpStatus.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error while creating or starting job: {}", e, e);
            return WebUtils.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating or starting job");
        }

        return WebUtils.okResponse(NewJobResponse.fromEntity(newJob));
    }
}
