package com.example.fiap.videosliceapi.adapters.externalsystem.integration;

import com.example.fiap.videosliceapi.adapters.externalsystem.S3MediaStorage;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.testUtils.StaticEnvironment;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class S3MediaStorageIT {
    private static final String REQUESTS_BUCKET = "videoslice-job-requests";
    private static final String RESULTS_BUCKET = "videoslice-job-results";

    private static LocalStackContainer localstack;

    private S3MediaStorage s3MediaStorage;

    @BeforeAll
    static void beforeAll() {
        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:3.5.0");

        localstack = new LocalStackContainer(localstackImage).withServices(LocalStackContainer.Service.S3);
        localstack.start();

        try (S3Client s3Client = createTestClient()) {
            s3Client.createBucket(builder -> builder.bucket(REQUESTS_BUCKET));
            s3Client.createBucket(builder -> builder.bucket(RESULTS_BUCKET));
        }
    }

    @AfterAll
    static void afterAll() {
        localstack.stop();
    }

    @BeforeEach
    void setUp() {
        System.setProperty("aws.accessKeyId", localstack.getAccessKey());
        System.setProperty("aws.secretAccessKey", localstack.getSecretKey());

        s3MediaStorage = new S3MediaStorage(new StaticEnvironment(Map.of(
                "videosliceapi.integration.aws.overrideAwsEndpoint", localstack.getEndpoint().toString(),
                "videosliceapi.integration.s3.videoProcessRequestBucketName", REQUESTS_BUCKET,
                "videosliceapi.integration.s3.videoProcessResultBucketName", RESULTS_BUCKET
        )));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (s3MediaStorage != null)
            s3MediaStorage.close();
    }

    @Test
    void saveInputVideo() {
        s3MediaStorage.saveInputVideo(UUID.fromString("2e08ad6d-1c29-4d50-950e-7b7011c9f484"),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        try (S3Client s3Client = createTestClient()) {
            ResponseBytes<GetObjectResponse> actualSaved = s3Client.getObjectAsBytes(builder -> builder.bucket(REQUESTS_BUCKET)
                    .key("input-video-2e08ad6d-1c29-4d50-950e-7b7011c9f484.mp4"));

            assertThat(actualSaved.asByteArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        }
    }

    @Test
    void saveInputVideo_largerFile() {
        byte[] bytes = new byte[1024 * 1024 * 20];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        s3MediaStorage.saveInputVideo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), bytes);

        byte[] saved;

        try (S3Client s3Client = createTestClient()) {
            ResponseBytes<GetObjectResponse> actualSaved = s3Client.getObjectAsBytes(builder -> builder.bucket(REQUESTS_BUCKET)
                    .key("input-video-123e4567-e89b-12d3-a456-426614174000.mp4"));

            saved = actualSaved.asByteArray();
        }

        assertThat(saved).hasSize(bytes.length);
        assertThat(saved).containsExactly(bytes);
    }

    @Test
    void deleteInputVideo() {
        s3MediaStorage.saveInputVideo(UUID.fromString("07bcc180-96d5-4fad-a726-6254b6976392"),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        String expectedFilename = "input-video-07bcc180-96d5-4fad-a726-6254b6976392.mp4";

        try (S3Client s3Client = createTestClient()) {
            ResponseBytes<GetObjectResponse> actualSaved = s3Client.getObjectAsBytes(
                    builder -> builder.bucket(REQUESTS_BUCKET).key(expectedFilename));
            assertThat(actualSaved.response().sdkHttpResponse().isSuccessful()).isTrue();
        }

        s3MediaStorage.removeInputVideo(UUID.fromString("07bcc180-96d5-4fad-a726-6254b6976392"));

        try (S3Client s3Client = createTestClient()) {
            assertThatThrownBy(
                    () -> s3Client.getObjectAsBytes(
                            builder -> builder.bucket(REQUESTS_BUCKET).key(expectedFilename))
            ).isInstanceOf(NoSuchKeyException.class);
        }
    }

    @Test
    void deleteInputVideo_nonExistingVideoStillNoError() {
        s3MediaStorage.removeInputVideo(UUID.fromString("9f6011ea-4181-45ae-bae0-1353eba3a276")); // does not exist
    }

    @Test
    void deleteInputVideo_error() throws Exception {
        StaticEnvironment environment = new StaticEnvironment(Map.of(
                "videosliceapi.integration.aws.overrideAwsEndpoint", localstack.getEndpoint().toString(),
                "videosliceapi.integration.s3.videoProcessRequestBucketName", "WrongRequestBucket",
                "videosliceapi.integration.s3.videoProcessResultBucketName", "WrongResultBucket"
        ));
        try (S3MediaStorage invalidConfigurationMediaStorage = new S3MediaStorage(environment)) {
            assertThatThrownBy(() -> invalidConfigurationMediaStorage.removeInputVideo(UUID.randomUUID()))
                    .hasMessageContaining("bucket does not exist");
        }
    }

    @Test
    void getOutputFileDownloadLink() {
        byte[] videoBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        try (S3Client s3Client = createTestClient()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(RESULTS_BUCKET)
                            .key("downloadable-file.dat")
                            .contentLength((long) videoBytes.length)
                            .build(),
                    RequestBody.fromBytes(videoBytes)
            );
        }

        DownloadLink link = s3MediaStorage.getOutputFileDownloadLink("downloadable-file.dat");

        // Just some basic sanity-check to ensure that the call worked
        assertThat(link).isNotNull();
        assertThat(link.url()).isNotNull();
        assertThat(link.url().startsWith("http://") || link.url().startsWith("https://")).isTrue();

        assertThat(link.expirationMinutes()).isEqualTo(20L);
    }

    private static S3Client createTestClient() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(localstack.getEndpoint())
                .forcePathStyle(true)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .build();
    }
}
