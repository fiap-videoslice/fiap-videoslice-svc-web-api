package com.example.fiap.archburgers.jobs;

import com.example.fiap.archburgers.domain.external.Pagamento;
import com.example.fiap.archburgers.domain.external.PagamentoService;
import com.example.fiap.archburgers.domain.usecases.PedidoUseCases;
import com.example.fiap.archburgers.domain.valueobjects.IdFormaPagamento;
import com.example.fiap.archburgers.domain.valueobjects.StatusPagamento;
import com.example.fiap.archburgers.domain.valueobjects.ValorMonetario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EngineResponseReceiverTaskTest {

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private PedidoUseCases pedidoUseCases;

    private ConfirmacaoPagamentoScheduledJob confirmacaoPagamentoScheduledJob;

    @BeforeEach
    void setUp() {
        confirmacaoPagamentoScheduledJob = new ConfirmacaoPagamentoScheduledJob(pagamentoService, pedidoUseCases);
    }

    @Test
    public void testReadConfirmacaoPagamentoSuccess() throws Exception {
        Pagamento pagamento = new Pagamento(
                "67326c0dbab6b22798c180a4",
                63,
                new IdFormaPagamento("MERCADO_PAGO"),
                StatusPagamento.FINALIZADO,
                new ValorMonetario("37.98"),
                LocalDateTime.of(2024, 11, 11, 17, 41, 49),
                LocalDateTime.of(2024, 11, 11, 17, 42, 38),
                "00020101021243650016COM.MERCADOLIBRE020",
                "24902326150"
        );

        doAnswer(invocation -> {
            Consumer<Pagamento> consumer = invocation.getArgument(0);
            consumer.accept(pagamento);
            return null;
        }).when(pagamentoService).receberConfirmacoes(any());

        confirmacaoPagamentoScheduledJob.readConfirmacaoPagamento();

        verify(pedidoUseCases).finalizarPagamento(pagamento);
    }

    @Test
    public void testReadConfirmacaoPagamentoException() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(pagamentoService).receberConfirmacoes(confirmacaoPagamentoScheduledJob);

        confirmacaoPagamentoScheduledJob.readConfirmacaoPagamento();

        verify(pagamentoService).receberConfirmacoes(confirmacaoPagamentoScheduledJob);
        verifyNoInteractions(pedidoUseCases);
    }
}