package com.example.fiap.archburgers.adapters.externalsystem;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceQueueApiTest {

    /*
    The unit tests are mainly for parameter error tests. Valid configuration is verified in the Integration Test
     */

    @Test
    void whenConstructedWithNoEndpoint_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("archburgers.integration.sqs.sqsEndpoint")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new PagamentoServiceQueueApi(environment));
    }

    @Test
    void whenConstructedWithNoPedidosQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("archburgers.integration.sqs.sqsEndpoint")).thenReturn("validSqsEndpoint");
        Mockito.when(environment.getProperty("archburgers.integration.sqs.pedidosQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new PagamentoServiceQueueApi(environment));
    }

    @Test
    void whenConstructedWithNoPagamentosConcluidosQueueName_expectException() {
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("archburgers.integration.sqs.sqsEndpoint")).thenReturn("validSqsEndpoint");
        Mockito.when(environment.getProperty("archburgers.integration.sqs.pagamentosEmAbertoQueueName")).thenReturn("validPedidosQueueName");
        Mockito.when(environment.getProperty("archburgers.integration.sqs.pagamentosConcluidosQueueName")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> new PagamentoServiceQueueApi(environment));
    }
}