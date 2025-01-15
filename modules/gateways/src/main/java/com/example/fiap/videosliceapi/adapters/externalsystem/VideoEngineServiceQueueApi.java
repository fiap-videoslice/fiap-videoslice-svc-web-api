package com.example.fiap.videosliceapi.adapters.externalsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class VideoEngineServiceQueueApi implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoEngineServiceQueueApi.class);

    private final String videoProcessRequestQueueUrl;
    private final String videoProcessResponseQueueUrl;

    private final SqsClient sqsClient;

    public VideoEngineServiceQueueApi(Environment environment) {
        String requestQueueName = environment.getProperty("videosliceapi.integration.sqs.videoProcessRequestQueueName");
        if (requestQueueName == null)
            throw new IllegalArgumentException("videosliceapi.integration.sqs.videoProcessRequestQueueName not set");

        String videoProcessResponseQueueName = environment.getProperty("videosliceapi.integration.sqs.videoProcessResponseQueueName");
        if (videoProcessResponseQueueName == null)
            throw new IllegalArgumentException("videosliceapi.integration.sqs.videoProcessResponseQueueName not set");

        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.US_EAST_1);

        builder = AwsClientUtils.maybeOverrideEndpoint(builder, environment);

        sqsClient = builder.build();

        String requestUrl;
        String responseUrl;
        try {
            requestUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(requestQueueName).build()).queueUrl();
            responseUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(videoProcessResponseQueueName).build()).queueUrl();
        } catch (QueueDoesNotExistException e) {
            LOGGER.error("Queues to communicate with video engine not found! System available for query only, new requests will fail");
            requestUrl = null;
            responseUrl = null;
        } catch (Exception e) {
            LOGGER.error("Error obtaining request queues: {}", e, e);
            throw e;
        }

        this.videoProcessRequestQueueUrl = requestUrl;
        this.videoProcessResponseQueueUrl = responseUrl;
    }

    public void sendMessageRequestQueue(String payload) {
        if (videoProcessRequestQueueUrl == null) {
            // Nesta condição a aplicação está em execução parcialmente apenas, para testes por exemplo.
            // não é possível completar as solicitações.
            throw new IllegalArgumentException("No communication with request queue");
        }

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(videoProcessRequestQueueUrl)
                .messageBody(payload)
                .delaySeconds(0)
                .build();
        sqsClient.sendMessage(sendMsgRequest);
    }

    public List<MessageSummary> receiveMessagesResponseQueue() {
        if (videoProcessRequestQueueUrl == null) {
            LOGGER.warn("No communication with response queue");
            return Collections.emptyList();
        }

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(videoProcessResponseQueueUrl)
                .maxNumberOfMessages(10)
                .visibilityTimeout(2)
                .build();

        return sqsClient.receiveMessage(receiveMessageRequest).messages()
                .stream().map(message -> new MessageSummary(message.body(), message.receiptHandle()))
                .peek(messageSummary -> {
                    LOGGER.info("Message received in the response queue");
                    LOGGER.debug("Message received in the response queue - details: {}", messageSummary);
                })
                .toList();
    }

    public void deleteMessagesQueueConfirmacao(MessageSummary message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(videoProcessResponseQueueUrl)
                .receiptHandle(message.receipt)
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    @Override
    public void close() {
        sqsClient.close();
    }

    public record MessageSummary(String body, String receipt) {

    }
}
