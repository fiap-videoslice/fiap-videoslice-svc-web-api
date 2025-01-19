package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoEngineServiceQueueApiTest {

    /*
    The unit tests are mainly for parameter error tests. Valid configuration is verified in the Integration Test
     */

    @Test
    void whenConstructedWithNoRequestQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint")).thenReturn("http://localhost:4566");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.videoProcessRequestQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new VideoEngineServiceQueueApi(environment));
    }

    @Test
    void whenConstructedWithNoResponseQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint")).thenReturn("http://localhost:4566");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.videoProcessRequestQueueName")).thenReturn("validQueueName");
        Mockito.when(environment.getProperty("videosliceapi.integration.sqs.videoProcessResponseQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new VideoEngineServiceQueueApi(environment));
    }
}