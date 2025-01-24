package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.utils.StringUtils;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

public class AwsClientUtils {
    public static SqsClientBuilder maybeOverrideEndpoint(
            SqsClientBuilder builder, Environment environment) {
        String sqsEndpoint = environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");

        if (!StringUtils.isEmpty(sqsEndpoint)) {
            return builder.endpointOverride(URI.create(sqsEndpoint));
        } else {
            return builder;
        }
    }

    public static S3ClientBuilder maybeOverrideEndpoint(
            S3ClientBuilder builder, Environment environment) {
        String sqsEndpoint = environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");

        if (!StringUtils.isEmpty(sqsEndpoint)) {
            // forcePathStyle also necessary for local environments like localstack
            return builder.endpointOverride(URI.create(sqsEndpoint))
                    .forcePathStyle(true);
        } else {
            return builder;
        }
    }

    public static S3Presigner.Builder maybeOverrideEndpoint(
            S3Presigner.Builder builder, Environment environment) {
        String sqsEndpoint = environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");

        if (!StringUtils.isEmpty(sqsEndpoint)) {
            return builder.endpointOverride(URI.create(sqsEndpoint));
        } else {
            return builder;
        }
    }
}
