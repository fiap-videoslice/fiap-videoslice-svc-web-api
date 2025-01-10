package com.example.fiap.archburgers.adapters.externalsystem.integration;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.adapters.externalsystem.PagamentoServiceQueueApi;
import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;


import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

class PagamentoServiceQueueApiIT {
    private PagamentoServiceQueueApi queueApi;
    private static String pagamentosEmAbertoQueueUrl;
    private static String pagamentosConcluidosQueueUrl;

    private static LocalStackContainer localstack;

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

            pagamentosEmAbertoQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder().queueName("pagamentosEmAberto").build()).queueUrl();
            pagamentosConcluidosQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder().queueName("pagamentos_concluidos").build()).queueUrl();
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

        queueApi = new PagamentoServiceQueueApi(new StaticEnvironment(Map.of(
                "archburgers.integration.sqs.sqsEndpoint", localstack.getEndpoint().toString(),
                "archburgers.integration.sqs.pagamentosEmAbertoQueueName", "pagamentosEmAberto",
                "archburgers.integration.sqs.pagamentosConcluidosQueueName", "pagamentos_concluidos"
        )));
    }

    @AfterEach
    void tearDown() {
        queueApi.close();
    }

    @Test
    void sendMessageQueuePagamento() throws Exception {
        String testMessage = "Teste_" + System.currentTimeMillis();

        queueApi.sendMessageQueuePagamento(testMessage);

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
                    .queueUrl(pagamentosEmAbertoQueueUrl)
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
                        .queueUrl(pagamentosConcluidosQueueUrl)
                        .messageBody(testMessage)
                        .build();

                sqsClient.sendMessage(sendMessageRequest);
            }
        }

        List<PagamentoServiceQueueApi.MessageSummary> messages = queueApi.receiveMessagesQueueConfirmacao();

        assertThat(messages.stream().map(PagamentoServiceQueueApi.MessageSummary::body))
                .containsExactlyInAnyOrder(testMessage1, testMessage2);

        for (PagamentoServiceQueueApi.MessageSummary message : messages) {
            queueApi.deleteMessagesQueueConfirmacao(message);
        }

        // Waiting past the Visibility Timeout
        Thread.sleep(3000);

        messages = queueApi.receiveMessagesQueueConfirmacao();

        assertThat(messages).isEmpty();
    }

    @Test
    public void integracaoDesativada_filaNaoExiste() {
        try (PagamentoServiceQueueApi brokenQueueApi = new PagamentoServiceQueueApi(new StaticEnvironment(Map.of(
                "archburgers.integration.sqs.sqsEndpoint", localstack.getEndpoint().toString(),
                "archburgers.integration.sqs.pagamentosEmAbertoQueueName", "pagamentosEmAberto_NAO_EXISTE",
                "archburgers.integration.sqs.pagamentosConcluidosQueueName", "pagamentos_concluidos"
        )))
        ) {
            assertThat(brokenQueueApi.receiveMessagesQueueConfirmacao()).isEmpty();

            assertThatThrownBy(() -> brokenQueueApi.sendMessageQueuePagamento("Teste"))
                    .hasMessageContaining("Sem comunicação com fila de pagamentos");
        }
    }

    @Test
    public void integracaoDesativada_erro() {
        try (PagamentoServiceQueueApi brokenQueueApi = new PagamentoServiceQueueApi(new StaticEnvironment(Map.of(
                "archburgers.integration.sqs.sqsEndpoint", "http://localhost:55999",
                "archburgers.integration.sqs.pagamentosEmAbertoQueueName", "pagamentosEmAberto",
                "archburgers.integration.sqs.pagamentosConcluidosQueueName", "pagamentos_concluidos"
        )))
        ) {
            assertThat(brokenQueueApi.receiveMessagesQueueConfirmacao()).isEmpty();

            assertThatThrownBy(() -> brokenQueueApi.sendMessageQueuePagamento("Teste"))
                    .hasMessageContaining("Sem comunicação com fila de pagamentos");
        }
    }
}