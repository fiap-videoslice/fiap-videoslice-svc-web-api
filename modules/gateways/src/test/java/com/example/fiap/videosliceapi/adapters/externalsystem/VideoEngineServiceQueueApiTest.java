package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.example.fiap.videosliceapi.adapters.externalsystem.VideoEngineServiceQueueApi;

import static org.junit.jupiter.api.Assertions.*;

class VideoEngineServiceQueueApiTest {

    /*
    The unit tests are mainly for parameter error tests. Valid configuration is verified in the Integration Test
     */

    @Test
    void whenConstructedWithNoEndpoint_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new VideoEngineServiceQueueApi(environment));
    }

    @Test
    void whenConstructedWithNoPedidosQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint")).thenReturn("validSqsEndpoint");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.pedidosQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new VideoEngineServiceQueueApi(environment));
    }

    @Test
    void whenConstructedWithNovideoProcessResponseQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint")).thenReturn("validSqsEndpoint");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.videoProcessRequestQueueName")).thenReturn("validPedidosQueueName");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.videoProcessResponseQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new VideoEngineServiceQueueApi(environment));
    }
}