package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

@Service
public class PagamentoServiceImpl  {
    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoServiceImpl.class);

//    private final PagamentoServiceWebApi webApi;
//    private final PagamentoServiceQueueApi queueApi;
//
//    private volatile List<IdFormaPagamento> formasPagamentoCache;
//    private volatile Instant formasPagamentoCacheTimestamp;
//    private final long webCacheMillis;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final DateTimeFormatter requestTimeFormatter = DateTimeFormatter.ISO_INSTANT;
//
//    @Autowired
//    public PagamentoServiceImpl(PagamentoServiceWebApi webApi, PagamentoServiceQueueApi queueApi) {
//        this.webApi = webApi;
//        this.queueApi = queueApi;
//        this.webCacheMillis = (5 * 60 * 1000);
//    }
//
//    @VisibleForTesting
//    PagamentoServiceImpl(PagamentoServiceWebApi webApi, PagamentoServiceQueueApi queueApi, long webCacheMillis) {
//        this.webApi = webApi;
//        this.queueApi = queueApi;
//        this.webCacheMillis = webCacheMillis;
//    }
//
//    @Override
//    public IdFormaPagamento validarFormaPagamento(String idFormaPagamento) throws Exception {
//        List<IdFormaPagamento> formasPagamento = getCachedFormasPagamento();
//        var formaPagamentoValida = formasPagamento.stream()
//                .filter(forma -> forma.codigo().equals(idFormaPagamento))
//                .findFirst();
//
//        if (formaPagamentoValida.isPresent()) {
//            return formaPagamentoValida.get();
//        } else {
//            throw new DomainArgumentException("Forma de pagamento desconhecida: " + idFormaPagamento);
//        }
//    }
//
//    @Override
//    public void iniciarPagamento(PedidoDetalhe pedidoDetalhe) throws Exception {
//        Pedido pedido = pedidoDetalhe.pedido();
//        var request = new PagamentoRequestDto(
//                pedido.id(),
//                pedido.idClienteIdentificado() != null ? pedido.idClienteIdentificado().id() : null,
//                pedido.nomeClienteNaoIdentificado(),
//                pedido.itens().stream().map(
//                        itemPedido -> {
//                            var itemCardapio = pedidoDetalhe.detalhesItens().get(itemPedido.idItemCardapio());
//
//                            return new PagamentoItemDto(itemPedido.numSequencia(), new PagamentoItemCardapioDto(
//                                    itemCardapio.id(),
//                                    itemCardapio.tipo().name(),
//                                    itemCardapio.nome(),
//                                    itemCardapio.descricao(),
//                                    itemCardapio.valor().asBigDecimal().toString()
//                            ));
//                        }
//                ).toList(),
//                pedido.observacoes(),
//                pedido.status().name(),
//                pedido.formaPagamento().codigo(),
//                requestTimeFormatter.format(pedido.dataHoraPedido().atZone(ZoneId.systemDefault()))
//        );
//
//        queueApi.sendMessageQueuePagamento(objectMapper.writeValueAsString(request));
//    }
//
//    @Override
//    public void receberConfirmacoes(Consumer<Pagamento> callback) {
//        List<PagamentoServiceQueueApi.MessageSummary> messages = queueApi.receiveMessagesQueueConfirmacao();
//
//        messages.forEach(message -> {
//            try {
//                PagamentoConfirmacaoDto confirmacao = objectMapper.readValue(message.body(), PagamentoConfirmacaoDto.class);
//
//                var pagamento = new Pagamento(
//                        confirmacao.id,
//                        confirmacao.idPedido(),
//                        new IdFormaPagamento(confirmacao.formaPagamento()),
//                        StatusPagamento.valueOf(confirmacao.status()),
//                        new ValorMonetario(confirmacao.valor()),
//                        LocalDateTime.parse(confirmacao.dataHoraCriacao()),
//                        LocalDateTime.parse(confirmacao.dataHoraAtualizacao()),
//                        confirmacao.codigoPagamentoCliente(),
//                        confirmacao.idPedidoSistemaExterno()
//                );
//
//                LOGGER.info("Mensagem de pagamento recebida para pedido {}", confirmacao.idPedido());
//
//                callback.accept(pagamento);
//                queueApi.deleteMessagesQueueConfirmacao(message);
//            } catch (Exception e) {
//                // Mensagem nao será excluída da fila retornando depois para nova tentativa .
//                // TO-DO: Atualmente número de tentativas não tem limite, pode ser necessário um controle de retries
//                LOGGER.error("Erro processando confirmacao de pagamento: {} -- {}", e.getMessage(), message, e);
//            }
//        });
//    }
//
//    protected List<IdFormaPagamento> getCachedFormasPagamento() throws Exception {
//        if (formasPagamentoCache == null || Duration.between(formasPagamentoCacheTimestamp, Instant.now()).toMillis() >= webCacheMillis) {
//            formasPagamentoCacheTimestamp = Instant.now();
//            formasPagamentoCache = webApi.listFormasPagamento();
//        }
//        return formasPagamentoCache;
//    }
//
//    private record PagamentoRequestDto(
//            Integer id,
//            Integer idClienteIdentificado,
//            String nomeClienteNaoIdentificado,
//
//            List<PagamentoItemDto> itens,
//            String observacoes,
//            String status,
//            String formaPagamento,
//
//            String dataHoraPedido
//    ) {
//
//    }
//
//    private record PagamentoItemDto(
//            int numSequencia,
//            PagamentoItemCardapioDto itemCardapio
//    ) {
//
//    }
//
//    private record PagamentoItemCardapioDto(
//            Integer id,
//            String tipo,
//            String nome,
//            String descricao,
//            String valor
//    ) {
//
//    }
//
//    private record PagamentoConfirmacaoDto(
//            String id,
//            Integer idPedido,
//            String formaPagamento,
//            String status,
//            String valor,
//            String dataHoraCriacao,
//            String dataHoraAtualizacao,
//            String codigoPagamentoCliente,
//            String idPedidoSistemaExterno
//    ) {
//
//    }

}
