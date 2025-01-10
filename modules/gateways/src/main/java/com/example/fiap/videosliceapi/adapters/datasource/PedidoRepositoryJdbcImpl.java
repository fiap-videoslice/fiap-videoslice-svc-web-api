package com.example.fiap.videosliceapi.adapters.datasource;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the Repository based on a relational database via JDBC
 */
@Repository
public class PedidoRepositoryJdbcImpl {
//    @Language("SQL")
//    private static final String SQL_SELECT_PEDIDO_BY_ID = """
//            select pedido_id,id_cliente_identificado,nome_cliente_nao_identificado,
//               observacoes,status,forma_pagamento,data_hora_pedido
//            from pedido where pedido_id = ?
//            """;
//
//    /*
//     *  Regras de ordenação:
//     *  Pronto > Em Preparação > Recebido > Em Pagamento
//     *  Mais antigos primeiro
//     */
//    @Language("SQL")
//    private static final String SQL_SELECT_PEDIDOS_BY_STATUS = """
//            select pedido_id,id_cliente_identificado,nome_cliente_nao_identificado,
//               observacoes,status,forma_pagamento,data_hora_pedido
//            from pedido where status IN (/*PARAM_PLACEHOLDERS*/)
//                        /*FILTRO_DATA_HORA_OPCIONAL*/
//            order by
//                case WHEN status = 'PRONTO' then 0
//                     WHEN status = 'PREPARACAO' then 1
//                     WHEN status = 'RECEBIDO' then 2
//                     WHEN status = 'PAGAMENTO' then 3
//                     WHEN status = 'FINALIZADO' then 4
//                else 5 END asc,
//                data_hora_pedido asc
//            """;
//
//    @Language("SQL")
//    private static final String SQL_SELECT_ITEMS_BY_PEDIDO = """
//                select pi.num_sequencia, pi.item_cardapio_id
//                from pedido_item pi
//                where pi.pedido_id = ?
//                order by pi.num_sequencia
//            """.stripIndent();
//
//    @Language("SQL")
//    private static final String SQL_INSERT_PEDIDO = """
//            insert into pedido (id_cliente_identificado,nome_cliente_nao_identificado,
//               observacoes,status,forma_pagamento,data_hora_pedido)
//            values (?,?,?,?,?,?) returning pedido_id;
//            """;
//
//    @Language("SQL")
//    private static final String SQL_DELETE_PEDIDO = """
//            DELETE from pedido where pedido_id = ?;
//            """;
//
//    @Language("SQL")
//    private static final String SQL_INSERT_ITEM = """
//            insert into pedido_item (pedido_id, item_cardapio_id, num_sequencia)
//            values (?,?,?);
//            """;
//
//    @Language("SQL")
//    private static final String SQL_DELETE_ITEM = """
//            DELETE from pedido_item where pedido_id = ?;
//            """;
//
//    @Language("SQL")
//    private static final String SQL_UPDATE_STATUS = """
//            update pedido set status = ? where pedido_id = ?;
//            """;
//
//    private final DatabaseConnection databaseConnection;
//
//    public PedidoRepositoryJdbcImpl(DatabaseConnection databaseConnection) {
//        this.databaseConnection = databaseConnection;
//    }
//
//    @Override
//    public Pedido getPedido(int idPedido) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_PEDIDO_BY_ID);
//             var stmtItens = connection.prepareStatement(SQL_SELECT_ITEMS_BY_PEDIDO)) {
//            stmt.setInt(1, idPedido);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next())
//                    return null;
//
//                return pedidoFromResultSet(rs, stmtItens);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public Pedido savePedido(Pedido pedido) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_INSERT_PEDIDO);
//             var stmtItem = connection.prepareStatement(SQL_INSERT_ITEM)) {
//
//            if (pedido.idClienteIdentificado() != null) {
//                stmt.setInt(1, pedido.idClienteIdentificado().id());
//            } else {
//                stmt.setNull(1, Types.INTEGER);
//            }
//
//            stmt.setString(2, pedido.nomeClienteNaoIdentificado());
//            stmt.setString(3, pedido.observacoes());
//            stmt.setString(4, pedido.status().name());
//            stmt.setString(5, pedido.formaPagamento().codigo());
//            stmt.setObject(6, pedido.dataHoraPedido());
//
//            int pedidoId;
//
//            try (var rs = stmt.executeQuery()) {
//                if (!rs.next()) {
//                    throw new IllegalStateException("Unexpected state, query should return");
//                }
//
//                pedidoId = rs.getInt(1);
//            }
//
//            for (ItemPedido item : pedido.itens()) {
//                stmtItem.clearParameters();
//                stmtItem.setInt(1, pedidoId);
//                stmtItem.setInt(2, item.idItemCardapio());
//                stmtItem.setInt(3, item.numSequencia());
//                stmtItem.executeUpdate();
//            }
//
//            return pedido.withId(pedidoId);
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public List<Pedido> listPedidos(List<StatusPedido> filtroStatus,
//                                    @Nullable LocalDateTime olderThan) {
//        var sql =  SQL_SELECT_PEDIDOS_BY_STATUS.replace("/*PARAM_PLACEHOLDERS*/",
//                filtroStatus.stream().map(s -> "?").collect(Collectors.joining(",")));
//
//        if (olderThan != null) {
//            sql = sql.replace("/*FILTRO_DATA_HORA_OPCIONAL*/", " and data_hora_pedido <= ?");
//        }
//
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(sql);
//             var stmtItens = connection.prepareStatement(SQL_SELECT_ITEMS_BY_PEDIDO)) {
//
//            for (int i = 0; i < filtroStatus.size(); i++) {
//                stmt.setString(i + 1, filtroStatus.get(i).name());
//            }
//
//            if (olderThan != null) {
//                stmt.setObject(filtroStatus.size() + 1, olderThan);
//            }
//
//            List<Pedido> result = new ArrayList<>();
//
//            try(ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    result.add(pedidoFromResultSet(rs, stmtItens));
//                }
//            }
//
//            return result;
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void updateStatus(Pedido pedido) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_UPDATE_STATUS)) {
//
//            stmt.setString(1, pedido.status().name());
//            stmt.setInt(2, Objects.requireNonNull(pedido.id(), "Must have ID to update"));
//
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    private static @NotNull Pedido pedidoFromResultSet(ResultSet rs, PreparedStatement itemStatement) throws SQLException {
//        int pedidoId = rs.getInt("pedido_id");
//
//        var idClienteIdentificado = rs.getInt("id_cliente_identificado");
//
//        IdFormaPagamento formaPagamento = new IdFormaPagamento(rs.getString("forma_pagamento"));
//
//        StatusPedido status = StatusPedido.valueOf(rs.getString("status"));
//
//        List<ItemPedido> itens = new ArrayList<>();
//
//        itemStatement.setInt(1, pedidoId);
//        try (ResultSet rsItens = itemStatement.executeQuery()) {
//            while (rsItens.next()) {
//                itens.add(new ItemPedido(
//                        rsItens.getInt("num_sequencia"),
//                        rsItens.getInt("item_cardapio_id")
//                ));
//            }
//        }
//
//        return Pedido.pedidoRecuperado(
//                pedidoId,
//                idClienteIdentificado > 0 ? new IdCliente(idClienteIdentificado) : null,
//                rs.getString("nome_cliente_nao_identificado"),
//                itens,
//                rs.getString("observacoes"),
//                status,
//                formaPagamento,
//                rs.getTimestamp("data_hora_pedido").toLocalDateTime()
//        );
//    }
//
//    @Override
//    public void deletePedido(int idPedido) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_DELETE_PEDIDO);
//             var stmtItem = connection.prepareStatement(SQL_DELETE_ITEM)) {
//
//            stmt.setInt(1, idPedido);
//            stmtItem.setInt(1, idPedido);
//
//            stmtItem.executeUpdate();
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
}
