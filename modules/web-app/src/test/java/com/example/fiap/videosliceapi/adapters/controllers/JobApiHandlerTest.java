package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.DummyTokenParser;
import com.example.fiap.videosliceapi.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.videosliceapi.adapters.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.CreateJobParam;
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

import static org.mockito.Mockito.when;
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
}