package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.DummyTokenParser;
import com.example.fiap.videosliceapi.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.videosliceapi.adapters.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.exception.DomainPermissionException;
import com.example.fiap.videosliceapi.domain.usecasedto.CreateJobParam;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobApiHandlerTest {
    @Mock
    private JobUseCases jobUseCases;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        JobApiHandler jobApiHandler = new JobApiHandler(jobUseCases,
                new DummyTokenParser(),
                new DummyTransactionManager());

        mockMvc = MockMvcBuilders.standaloneSetup(jobApiHandler).build();
    }

    @Test
    void startNewJob() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestConstants.VIDEO_BYTES
        );

        when(jobUseCases.createNewJob(new CreateJobParam(TestConstants.VIDEO_BYTES, 5), "Test-User-1")).thenReturn(
                Job.createJob(TestConstants.ID_1, "/input/video_1.mp4", 5, TestConstants.INSTANT_1, "Test-User-1")
        );

        mockMvc.perform(multipart("/jobs")
                        .file(file)
                        .param("sliceIntervalSeconds", "5")
                        .header("Authorization", "Dummy User1")
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "sliceIntervalSeconds": 5,
                          "message": "Job created"
                        }
                        """));
    }

    @Test
    void startNewJob_errorEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/jobs")
                        .file(file)
                        .param("sliceIntervalSeconds", "5")
                        .header("Authorization", "Dummy User1")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void startNewJob_errorUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestConstants.VIDEO_BYTES
        );

        mockMvc.perform(multipart("/jobs")
                        .file(file)
                        .param("sliceIntervalSeconds", "5")
                        .header("Authorization", "Dummy InvalidLoginToken")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void startNewJob_serverError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestConstants.VIDEO_BYTES
        );

        when(jobUseCases.createNewJob(new CreateJobParam(TestConstants.VIDEO_BYTES, 5), "Test-User-1"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(multipart("/jobs")
                        .file(file)
                        .param("sliceIntervalSeconds", "5")
                        .header("Authorization", "Dummy User1")
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getDownloadLink_validRequest_shouldReturnDownloadLink() throws Exception {
        String uuid = TestConstants.ID_1.toString();
        when(jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "Test-User-1"))
                .thenReturn(new DownloadLink("https://example.com/file.zip", 60));

        mockMvc.perform(get("/jobs/{uuid}/download", uuid)
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                          {"url": "https://example.com/file.zip", "expirationMinutes": 60}
                        """));
    }

    @Test
    void getDownloadLink_invalidUUID_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/jobs/{uuid}/download", "invalid-uuid")
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDownloadLink_unauthorized_shouldReturnUnauthorized() throws Exception {
        String uuid = TestConstants.ID_1.toString();
        mockMvc.perform(get("/jobs/{uuid}/download", uuid))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDownloadLink_serverError() throws Exception {
        String uuid = TestConstants.ID_1.toString();
        when(jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "Test-User-1"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/jobs/{uuid}/download", uuid)
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listUserJobs_validRequest_shouldReturnJobList() throws Exception {
        when(jobUseCases.listJobsFromUser("Test-User-1"))
                .thenReturn(List.of(
                        Job.createJob(TestConstants.ID_1, "/input/video_1.mp4", 5, TestConstants.INSTANT_1, "Test-User-1"),
                        Job.createJob(TestConstants.ID_2, "/input/video_2.mp4", 10, TestConstants.INSTANT_1, "Test-User-1")
                ));

        mockMvc.perform(get("/jobs")
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "sliceIntervalSeconds": 5,
                            "status": "CREATED",
                            "startTime": "2025-01-08T00:55:43Z"
                          },
                          {
                            "id": "228cc54f-4ff0-41b3-b407-15b095f92614",
                            "sliceIntervalSeconds": 10,
                            "status": "CREATED",
                            "startTime": "2025-01-08T00:55:43Z"
                          }
                        ]
                        """));
    }

    @Test
    void listUserJobs_unauthorized_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUserJobs_internalError_shouldReturnInternalServerError() throws Exception {
        when(jobUseCases.listJobsFromUser("Test-User-1"))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/jobs")
                        .header("Authorization", "Dummy User1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getDownloadLink_noFileAccess_shouldReturnForbidden() throws Exception {
        String uuid = TestConstants.ID_1.toString();

        when(jobUseCases.getResultFileDownloadLink(TestConstants.ID_1, "Test-User-2"))
                .thenThrow(new DomainPermissionException("You are not the owner"));

        mockMvc.perform(get("/jobs/{uuid}/download", uuid)
                        .header("Authorization", "Dummy User2"))
                .andExpect(status().isForbidden());
    }
}