package com.example.fiap.videosliceapi.adapters.externalsystem.integration;

import com.example.fiap.videosliceapi.adapters.externalsystem.VideoEngineServiceQueueApi;
import com.example.fiap.videosliceapi.testUtils.StaticEnvironment;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

class VideoEngineServiceQueueApiIT {
    private static String videoProcessRequestQueueUrl;
    private static String videoProcessResponseQueueUrl;

    private static LocalStackContainer localstack;

    private VideoEngineServiceQueueApi queueApi;

    @BeforeAll
    static void beforeAll() {
        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:3.5.0");

        localstack = new LocalStackContainer(localstackImage).withServices(SQS);
        localstack.start();

        try (SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .build()) {

            videoProcessRequestQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder().queueName("video_requests").build()).queueUrl();
            videoProcessResponseQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder().queueName("video_process_status").build()).queueUrl();
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

        queueApi = new VideoEngineServiceQueueApi(new StaticEnvironment(Map.of(
                "videosliceapi.integration.aws.overrideAwsEndpoint", localstack.getEndpoint().toString(),
                "videosliceapi.integration.sqs.videoProcessRequestQueueName", "video_requests",
                "videosliceapi.integration.sqs.videoProcessResponseQueueName", "video_process_status"
        )));
    }

    @AfterEach
    void tearDown() {
        queueApi.close();
    }

    @Test
    void sendMessageRequestQueue() throws Exception {
        String testMessage = "Teste_" + System.currentTimeMillis();

        queueApi.sendMessageRequestQueue(testMessage);

        try (SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .build()) {

            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(videoProcessRequestQueueUrl)
                    .maxNumberOfMessages(10)
                    .build();

            Thread.sleep(100L);
            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            assertThat(messages).hasSize(1);
            Message message = messages.getFirst();
            assertThat(message.body()).isEqualTo(testMessage);
        }
    }

    @Test
    public void receiveMessageQueueConfirmacao() throws InterruptedException {
        String testMessage1 = "Teste_1_" + System.currentTimeMillis();
        String testMessage2 = "Teste_2_" + System.currentTimeMillis();

        try (SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .build()) {

            for (String testMessage : List.of(testMessage1, testMessage2)) {
                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                        .queueUrl(videoProcessResponseQueueUrl)
                        .messageBody(testMessage)
                        .build();

                sqsClient.sendMessage(sendMessageRequest);
            }
        }

        List<VideoEngineServiceQueueApi.MessageSummary> messages = queueApi.receiveMessagesResponseQueue();

        assertThat(messages.stream().map(VideoEngineServiceQueueApi.MessageSummary::body))
                .containsExactlyInAnyOrder(testMessage1, testMessage2);

        for (VideoEngineServiceQueueApi.MessageSummary message : messages) {
            queueApi.deleteMessagesQueueConfirmacao(message);
        }

        // Waiting past the Visibility Timeout
        Thread.sleep(3000);

        messages = queueApi.receiveMessagesResponseQueue();

        assertThat(messages).isEmpty();
    }

    @Test
    public void integracaoDesativada_filaNaoExiste() {
        try (VideoEngineServiceQueueApi brokenQueueApi = new VideoEngineServiceQueueApi(new StaticEnvironment(Map.of(
                "videosliceapi.integration.aws.overrideAwsEndpoint", localstack.getEndpoint().toString(),
                "videosliceapi.integration.sqs.videoProcessRequestQueueName", "video_requests_NAO_EXISTE",
                "videosliceapi.integration.sqs.videoProcessResponseQueueName", "video_process_status"
        )))
        ) {
            assertThat(brokenQueueApi.receiveMessagesResponseQueue()).isEmpty();

            assertThatThrownBy(() -> brokenQueueApi.sendMessageRequestQueue("Teste"))
                    .hasMessageContaining("No communication with request queue");
        }
    }
}