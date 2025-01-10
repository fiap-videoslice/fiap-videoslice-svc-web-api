package com.example.fiap.videosliceapi.adapters.datasource;


import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class CarrinhoRepositoryJdbcImpl {
    @Language("SQL")
    private final String SQL_SELECT_CARRINHO_BY_ID = """
                select carrinho_id,id_cliente_identificado,nome_cliente_nao_identificado,observacoes,data_hora_criado
                from carrinho where carrinho_id = ?
            """;

    @Language("SQL")
    private final String SQL_SELECT_CARRINHO_BY_CLIENTE = """
                select carrinho_id,id_cliente_identificado,nome_cliente_nao_identificado,observacoes,data_hora_criado
                from carrinho where id_cliente_identificado = ?
            """;

    @Language("SQL")
    private static final String SQL_SELECT_ITEMS_BY_CARRINHO = """
                select ci.num_sequencia, ci.item_cardapio_id
                from carrinho_item ci
                where ci.carrinho_id = ?
                order by ci.num_sequencia
            """.stripIndent();

    @Language("SQL")
    private final String SQL_INSERT_CARRINHO_EMPTY = """
                insert into carrinho (id_cliente_identificado,nome_cliente_nao_identificado,observacoes,data_hora_criado)
                values (?,?,?,?) returning carrinho_id
            """;

    @Language("SQL")
    private final String SQL_INSERT_ITEM_CARRINHO = """
                insert into carrinho_item (carrinho_id, item_cardapio_id, num_sequencia)
                values (?,?,?)
            """;

    @Language("SQL")
    private final String SQL_DELETE_ITEM_CARRINHO = """
                delete from carrinho_item where carrinho_id = ?
            """;

    @Language("SQL")
    private final String SQL_DELETE_CARRINHO = """
                delete from carrinho where carrinho_id = ?
            """;

    @Language("SQL")
    private final String SQL_UPDATE_OBSERVACAO = """
                update carrinho set observacoes = ? where carrinho_id = ?
            """;

//    private final DatabaseConnection databaseConnection;

//    @Autowired
//    public CarrinhoRepositoryJdbcImpl(DatabaseConnection databaseConnection) {
//        this.databaseConnection = databaseConnection;
//    }
//
//
//    public Carrinho getCarrinhoSalvoByCliente(IdCliente idCliente) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_CARRINHO_BY_CLIENTE);
//             var stmtItens = connection.prepareStatement(SQL_SELECT_ITEMS_BY_CARRINHO)) {
//
//            stmt.setInt(1, idCliente.id());
//
//            return getCarrinhoRecordSet(stmt, stmtItens);
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public Carrinho getCarrinho(int idCarrinho) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_CARRINHO_BY_ID);
//             var stmtItens = connection.prepareStatement(SQL_SELECT_ITEMS_BY_CARRINHO)) {
//            stmt.setInt(1, idCarrinho);
//
//            return getCarrinhoRecordSet(stmt, stmtItens);
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public Carrinho salvarCarrinhoVazio(Carrinho carrinho) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_INSERT_CARRINHO_EMPTY)) {
//
//            if (carrinho.idClienteIdentificado() != null) {
//                stmt.setInt(1, carrinho.idClienteIdentificado().id());
//            } else {
//                stmt.setNull(1, Types.INTEGER);
//            }
//
//            if (carrinho.nomeClienteNaoIdentificado() != null) {
//                stmt.setString(2, carrinho.nomeClienteNaoIdentificado());
//            } else {
//                stmt.setNull(2, Types.VARCHAR);
//            }
//
//            stmt.setString(3, carrinho.observacoes());
//            stmt.setObject(4, carrinho.dataHoraCarrinhoCriado());
//
//            ResultSet rs = stmt.executeQuery();
//
//            if (!rs.next())
//                throw new IllegalStateException("Insert was expected to return ID");
//
//            int newId = rs.getInt(1);
//
//            return carrinho.withId(newId);
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public void salvarItemCarrinho(Carrinho carrinho, ItemPedido newItem) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_INSERT_ITEM_CARRINHO)) {
//
//            stmt.setInt(1, Objects.requireNonNull(carrinho.id(), "Must be persisted to add items"));
//            stmt.setInt(2, newItem.idItemCardapio());
//            stmt.setInt(3, newItem.numSequencia());
//
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public void updateObservacaoCarrinho(Carrinho carrinho) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_UPDATE_OBSERVACAO)) {
//
//            stmt.setString(1, carrinho.observacoes());
//            stmt.setInt(2, Objects.requireNonNull(carrinho.id(), "Must be persisted to update"));
//
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public void deleteItensCarrinho(Carrinho carrinho) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt1 = connection.prepareStatement(SQL_DELETE_ITEM_CARRINHO)) {
//
//            stmt1.setInt(1, Objects.requireNonNull(carrinho.id(), "Must be persisted to delete items"));
//            stmt1.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//
//    public void deleteCarrinho(Carrinho carrinho) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt1 = connection.prepareStatement(SQL_DELETE_ITEM_CARRINHO);
//             var stmt2 = connection.prepareStatement(SQL_DELETE_CARRINHO)) {
//
//            stmt1.setInt(1, Objects.requireNonNull(carrinho.id(), "Must be persisted to delete"));
//            stmt1.executeUpdate();
//
//            stmt2.setInt(1, Objects.requireNonNull(carrinho.id(), "Must be persisted to delete"));
//            stmt2.executeUpdate();
//
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    private static @Nullable Carrinho getCarrinhoRecordSet(PreparedStatement stmt, PreparedStatement itensStatement) throws SQLException {
//        ResultSet rs = stmt.executeQuery();
//
//        if (!rs.next())
//            return null;
//
//        int carrinhoId = rs.getInt("carrinho_id");
//
//        int rsIdCliente = rs.getInt("id_cliente_identificado");
//
//        List<ItemPedido> itens = new ArrayList<>();
//
//        itensStatement.setInt(1, carrinhoId);
//        try (ResultSet rsItens = itensStatement.executeQuery()) {
//            while (rsItens.next()) {
//                itens.add(new ItemPedido(
//                        rsItens.getInt("num_sequencia"),
//                        rsItens.getInt("item_cardapio_id")
//                ));
//            }
//        }
//
//        return new Carrinho(carrinhoId,
//                rsIdCliente > 0 ? new IdCliente(rsIdCliente) : null,
//                rs.getString("nome_cliente_nao_identificado"),
//                itens,
//                rs.getString("observacoes"),
//                rs.getTimestamp("data_hora_criado").toLocalDateTime());
//    }
}
