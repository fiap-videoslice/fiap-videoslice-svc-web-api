package com.example.fiap.videosliceapi.adapters.controllers;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.adapters.testUtils.TestConstants;
import com.example.fiap.videosliceapi.domain.auth.LoggedUser;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecaseparam.CreateJobParam;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobApiHandlerTest {
    @Mock
    private JobUseCases jobUseCases;
    @Mock
    private LoggedUserTokenParser loggedUserTokenParser;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JobApiHandler jobApiHandler = new JobApiHandler(jobUseCases, loggedUserTokenParser);
        mockMvc = MockMvcBuilders.standaloneSetup(jobApiHandler).build();
    }

    @Test
    void startNewJob() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                TestConstants.VIDEO_BYTES
        );

        LoggedUser loggedUser = mockAuthHeader("MyTokenAbc");

        when(jobUseCases.createNewJob(new CreateJobParam(TestConstants.VIDEO_BYTES, 5), loggedUser)).thenReturn(
                Job.createJob(TestConstants.ID_1, "/input/video_1.mp4", 5, TestConstants.INSTANT_1, "user1@example.com")
        );

        mockMvc.perform(multipart("/jobs")
                        .file(file)
                        .param("sliceIntervalSeconds", "5")
                        .header("IdentityToken", "MyTokenAbc")
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

    private LoggedUser mockAuthHeader(String expectedTokenHeader) {
        LoggedUser loggedUser = mock();

        when(loggedUserTokenParser.verifyLoggedUser(any())).thenAnswer(invocationOnMock -> {
            HttpHeaders headers = invocationOnMock.getArgument(0);
            String actualTokenHeader = headers.getFirst("IdentityToken");
            if (!expectedTokenHeader.equals(actualTokenHeader)) {
                throw new RuntimeException("Unexpected IdentityToken: " + actualTokenHeader);
            }

            return loggedUser;
        });

        return loggedUser;
    }


    //andExpect(content().json(new ObjectMapper().writeValueAsString(CARRINHO_11_DTO)));
}