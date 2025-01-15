package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.springframework.core.env.Environment;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

public class AwsClientUtils {
    public static <B extends SdkClientBuilder<B, C>, C> B maybeOverrideEndpoint(
            SdkClientBuilder<B, C> builder, Environment environment) {
        String sqsEndpoint = environment.getProperty("videosliceapi.integration.aws.overrideAwsEndpoint");

        if (sqsEndpoint != null && !sqsEndpoint.isEmpty()) {
            B overriden = builder.endpointOverride(URI.create(sqsEndpoint));
            if (overriden instanceof S3ClientBuilder) {
                // Also necessary for local environments like localstack
                overriden = (B) ((S3ClientBuilder) overriden).forcePathStyle(true);
            }
            return overriden;
        } else {
            return (B) builder;
        }
    }
}
