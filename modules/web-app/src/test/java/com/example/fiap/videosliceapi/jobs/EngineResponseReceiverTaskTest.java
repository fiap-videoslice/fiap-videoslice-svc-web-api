package com.example.fiap.videosliceapi.jobs;

import com.example.fiap.videosliceapi.adapters.datasource.TransactionManager;
import com.example.fiap.videosliceapi.adapters.testUtils.DummyTransactionManager;
import com.example.fiap.videosliceapi.domain.external.VideoEngineService;
import com.example.fiap.videosliceapi.domain.usecases.JobUseCases;
import com.example.fiap.videosliceapi.domain.valueobjects.JobResponse;
import com.example.fiap.videosliceapi.domain.valueobjects.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EngineResponseReceiverTaskTest {

    @Mock
    private VideoEngineService videoEngineService;
    @Mock
    private JobUseCases jobUseCases;

    private TransactionManager transactionManager;

    private EngineResponseReceiverTask engineResponseReceiverTask;

    @BeforeEach
    void setUp() {
        transactionManager = new DummyTransactionManager();

        engineResponseReceiverTask = new EngineResponseReceiverTask(videoEngineService, jobUseCases, transactionManager);
    }

    @Test
    public void readResponseSuccess() throws Exception {
        JobResponse response = new JobResponse(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                JobStatus.COMPLETE, "/outputs/frames.zip", null);

        doAnswer(invocation -> {
            Consumer<JobResponse> consumer = invocation.getArgument(0);
            consumer.accept(response);
            return null;
        }).when(videoEngineService).receiveAvailableResponseMessages(any());

        engineResponseReceiverTask.readEngineResponse();

        verify(jobUseCases).updateJobStatus(response);
    }

    @Test
    public void testReadConfirmacaoPagamentoException() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(videoEngineService).receiveAvailableResponseMessages(any());

        engineResponseReceiverTask.readEngineResponse();

        verify(videoEngineService).receiveAvailableResponseMessages(any());
        verifyNoInteractions(jobUseCases);
    }
}