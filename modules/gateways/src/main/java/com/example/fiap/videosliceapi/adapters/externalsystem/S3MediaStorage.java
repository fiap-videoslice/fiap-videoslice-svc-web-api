package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.external.MediaStorage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
public class S3MediaStorage implements MediaStorage, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3MediaStorage.class);

    private final S3Client s3Client;
    private final String requestBucketName;
    private final String resultBucketName;

    public S3MediaStorage(Environment environment) {
        requestBucketName = environment.getProperty("videosliceapi.integration.s3.videoProcessRequestBucketName");
        if (requestBucketName == null)
            throw new IllegalArgumentException("videosliceapi.integration.s3.videoProcessRequestBucketName not set");

        resultBucketName = environment.getProperty("videosliceapi.integration.s3.videoProcessResultBucketName");
        if (resultBucketName == null)
            throw new IllegalArgumentException("videosliceapi.integration.s3.videoProcessResultBucketName not set");

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.US_EAST_1);

        builder = AwsClientUtils.maybeOverrideEndpoint(builder, environment);

        s3Client = builder.build();
    }

    @Override
    public String saveInputVideo(UUID uuid, byte[] videoBytes) {
        String fileName = uuidToFileName(uuid);

        LOGGER.info("Saving file {} with {} bytes to bucket {}", fileName, videoBytes.length, requestBucketName);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(requestBucketName)
                        .key(fileName)
                        .contentLength((long) videoBytes.length)
                        .build(),
                RequestBody.fromBytes(videoBytes)
        );

        return fileName;
    }

    @Override
    public void removeInputVideo(UUID uuid) {
        String fileName = uuidToFileName(uuid);

        LOGGER.info("REMOVING file {} from bucket {}", fileName, requestBucketName);

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(requestBucketName)
                        .key(fileName)
                        .build()
        );
    }

    private static @NotNull String uuidToFileName(UUID uuid) {
        return "input-video-" + uuid + ".mp4";
    }

    @Override
    public void close() throws Exception {
        s3Client.close();
    }
}
