package com.example.fiap.videoslice.adapters.externalsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class PagamentoServiceQueueApi implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoServiceQueueApi.class);

    private final String pedidosQueueUrl;
    private final String pagamentosConcluidosQueueUrl;

    private final SqsClient sqsClient;

    public PagamentoServiceQueueApi(Environment environment) {
        String sqsEndpoint = environment.getProperty("videoslice.integration.sqs.sqsEndpoint");
        if (sqsEndpoint == null)
            throw new IllegalArgumentException("videoslice.integration.sqs.sqsEndpoint not set");

        String pedidosQueueName = environment.getProperty("videoslice.integration.sqs.pagamentosEmAbertoQueueName");
        if (pedidosQueueName == null)
            throw new IllegalArgumentException("videoslice.integration.sqs.pagamentosEmAbertoQueueName not set");

        String pagamentosConcluidosQueueName = environment.getProperty("videoslice.integration.sqs.pagamentosConcluidosQueueName");
        if (pagamentosConcluidosQueueName == null)
            throw new IllegalArgumentException("videoslice.integration.sqs.pagamentosConcluidosQueueName not set");

        sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(sqsEndpoint))
                .build();

        String pedidosUrl;
        String confirmacaoUrl;
        try {
            pedidosUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(pedidosQueueName).build()).queueUrl();
            confirmacaoUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(pagamentosConcluidosQueueName).build()).queueUrl();
        } catch (QueueDoesNotExistException e) {
            LOGGER.error("Filas de comunicação com serviço de pagamento não existem! Sistema disponível apenas para testes, não será possível realizar pagamentos.");
            pedidosUrl = null;
            confirmacaoUrl = null;
        } catch (Exception e) {
            LOGGER.error("Erro ao obter filas de comunicação com serviço de pagamento! Sistema disponível apenas para testes, não será possível realizar pagamentos.");
            pedidosUrl = null;
            confirmacaoUrl = null;
        }

        this.pedidosQueueUrl = pedidosUrl;
        this.pagamentosConcluidosQueueUrl = confirmacaoUrl;
    }

    public void sendMessageQueuePagamento(String payload) {
        if (pedidosQueueUrl == null) {
            // Nesta condição a aplicação está em execução parcialmente apenas, para testes por exemplo.
            // não é possível completar os pedidos.
            throw new IllegalArgumentException("Sem comunicação com fila de pagamentos");
        }

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(pedidosQueueUrl)
                .messageBody(payload)
                .delaySeconds(0)
                .build();
        sqsClient.sendMessage(sendMsgRequest);
    }

    public List<MessageSummary> receiveMessagesQueueConfirmacao() {
        if (pedidosQueueUrl == null) {
            LOGGER.warn("Sem comunicação com fila de pagamentos");
            return Collections.emptyList();
        }

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(pagamentosConcluidosQueueUrl)
                .maxNumberOfMessages(10)
                .visibilityTimeout(2)
                .build();

        return sqsClient.receiveMessage(receiveMessageRequest).messages()
                .stream().map(message -> new MessageSummary(message.body(), message.receiptHandle()))
                .peek(messageSummary -> {
                    LOGGER.info("Mensagem recebida na fila de confirmação de pagamentos");
                    LOGGER.debug("Detalhes da mensagem recebida na fila de confirmação de pagamentos: {}", messageSummary);
                })
                .toList();
    }

    public void deleteMessagesQueueConfirmacao(MessageSummary message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(pagamentosConcluidosQueueUrl)
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
