package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AwsClientUtilsTest {

    @Test
    void testMaybeOverrideEndpoint_WithValidEndpointOverridesBuilder() {
        Environment mockEnvironment = mock(Environment.class);
        SdkClientBuilder<SqsClientBuilder, SqsClient> originalBuilder = mock(SdkClientBuilder.class);
        SqsClientBuilder changedBuilder = mock(SqsClientBuilder.class);

        String validEndpoint = "http://localhost:4566";
        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(validEndpoint);
        when(originalBuilder.endpointOverride(any(URI.class))).thenReturn(changedBuilder);

        SdkClientBuilder<?, ?> result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        verify(mockEnvironment).getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");
        verify(originalBuilder).endpointOverride(URI.create(validEndpoint));
        assertThat(result).isSameAs(changedBuilder);
    }

    @Test
    void testMaybeOverrideEndpoint_WithNullEndpointDoesNotOverride() {
        // Arrange
        Environment mockEnvironment = mock(Environment.class);
        SdkClientBuilder<?, ?> originalBuilder = mock(SdkClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(null);

        // Act
        SdkClientBuilder<?, ?> result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        // Assert
        verify(mockEnvironment).getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");
        verify(originalBuilder, never()).endpointOverride(any(URI.class));
        assert result == originalBuilder;
    }

    @Test
    void testMaybeOverrideEndpoint_WithEmptyEndpointDoesNotOverride() {
        // Arrange
        Environment mockEnvironment = mock(Environment.class);
        SdkClientBuilder<?, ?> originalBuilder = mock(SdkClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn("");

        // Act
        SdkClientBuilder<?, ?> result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        // Assert
        verify(mockEnvironment).getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");
        verify(originalBuilder, never()).endpointOverride(any(URI.class));
        assert result == originalBuilder;
    }
}