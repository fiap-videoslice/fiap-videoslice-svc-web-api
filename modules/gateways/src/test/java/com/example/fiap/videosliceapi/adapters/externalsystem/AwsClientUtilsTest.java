package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AwsClientUtilsTest {

    private static final String VALID_ENDPOINT = "http://localhost:4566";
    private static final URI VALID_ENDPOINT_URI = URI.create(VALID_ENDPOINT);

    @Test
    void testMaybeOverrideEndpoint_SqsClientBuilder_WithValidOverride() {
        Environment mockEnvironment = mock(Environment.class);
        SqsClientBuilder originalBuilder = mock(SqsClientBuilder.class);
        SqsClientBuilder changedBuilder = mock(SqsClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(VALID_ENDPOINT);
        when(originalBuilder.endpointOverride(VALID_ENDPOINT_URI)).thenReturn(changedBuilder);

        SqsClientBuilder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(changedBuilder);
    }

    @Test
    void testMaybeOverrideEndpoint_SqsClientBuilder_WithoutOverride() {
        Environment mockEnvironment = mock(Environment.class);
        SqsClientBuilder originalBuilder = mock(SqsClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(null);

        SqsClientBuilder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(originalBuilder);
    }

    @Test
    void testMaybeOverrideEndpoint_S3ClientBuilder_WithValidOverride() {
        Environment mockEnvironment = mock(Environment.class);
        S3ClientBuilder originalBuilder = mock(S3ClientBuilder.class);

        S3ClientBuilder changedBuilder1 = mock(S3ClientBuilder.class);
        S3ClientBuilder changedBuilderFinal = mock(S3ClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(VALID_ENDPOINT);

        when(originalBuilder.endpointOverride(VALID_ENDPOINT_URI)).thenReturn(changedBuilder1);
        when(changedBuilder1.forcePathStyle(true)).thenReturn(changedBuilderFinal);

        S3ClientBuilder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(changedBuilderFinal);
    }

    @Test
    void testMaybeOverrideEndpoint_S3ClientBuilder_WithoutOverride() {
        Environment mockEnvironment = mock(Environment.class);
        S3ClientBuilder originalBuilder = mock(S3ClientBuilder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(null);

        S3ClientBuilder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(originalBuilder);
    }

    @Test
    void testMaybeOverrideEndpoint_S3PresignerBuilder_WithValidOverride() {
        Environment mockEnvironment = mock(Environment.class);
        S3Presigner.Builder originalBuilder = mock(S3Presigner.Builder.class);
        S3Presigner.Builder changedBuilder = mock(S3Presigner.Builder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(VALID_ENDPOINT);
        when(originalBuilder.endpointOverride(VALID_ENDPOINT_URI)).thenReturn(changedBuilder);

        S3Presigner.Builder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(changedBuilder);
    }

    @Test
    void testMaybeOverrideEndpoint_S3PresignerBuilder_WithoutOverride() {
        Environment mockEnvironment = mock(Environment.class);
        S3Presigner.Builder originalBuilder = mock(S3Presigner.Builder.class);

        when(mockEnvironment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint"))
                .thenReturn(null);

        S3Presigner.Builder result = AwsClientUtils.maybeOverrideEndpoint(originalBuilder, mockEnvironment);

        assertThat(result).isSameAs(originalBuilder);
    }
}