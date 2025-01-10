package com.example.fiap.videosliceapi.adapters.datasource;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClienteRepositoryJdbcImpl {
    @Language("SQL")
    private final static String SQL_SELECT_CLIENTE_BY_CPF = "select cliente_id, nome, cpf, email from cliente where cpf = ?";

    @Language("SQL")
    private final static String SQL_SELECT_CLIENTE_BY_ID = "select cliente_id, nome, cpf, email from cliente where cliente_id = ?";

    @Language("SQL")
    private final static String SQL_SELECT_ALL_CLIENTES = "select cliente_id, nome, cpf, email from cliente";

    @Language("SQL")
    private final static String SQL_INSERT_CLIENTE = "insert into cliente (nome, cpf, email) values (?, ?, ?) returning cliente_id";

//    private final DatabaseConnection databaseConnection;
//
//    public ClienteRepositoryJdbcImpl(DatabaseConnection databaseConnection) {
//        this.databaseConnection = databaseConnection;
//    }
//
//    @Override
//    public @Nullable Cliente getClienteByCpf(@NotNull Cpf cpf) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_CLIENTE_BY_CPF)) {
//            stmt.setString(1, cpf.cpfNum());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//
//                if (!rs.next())
//                    return null;
//
//                return new Cliente(
//                        new IdCliente(rs.getInt("cliente_id")),
//                        rs.getString("nome"),
//                        new Cpf(rs.getString("cpf")),
//                        rs.getString("email"));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public Cliente getClienteById(int id) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_CLIENTE_BY_ID)) {
//            stmt.setInt(1, id);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next())
//                    return null;
//
//                return new Cliente(
//                        new IdCliente(rs.getInt("cliente_id")),
//                        rs.getString("nome"),
//                        new Cpf(rs.getString("cpf")),
//                        rs.getString("email"));
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public Cliente salvarCliente(@NotNull Cliente cliente) {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_INSERT_CLIENTE)) {
//            stmt.setString(1, cliente.nome());
//            stmt.setString(2, cliente.cpf().cpfNum());
//            stmt.setString(3, cliente.email());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next())
//                    throw new IllegalStateException("Query was expected to return. " + SQL_INSERT_CLIENTE);
//
//                return new Cliente(
//                        new IdCliente(rs.getInt(1)),
//                        cliente.nome(),
//                        cliente.cpf(),
//                        cliente.email());
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public List<Cliente> listarTodosClientes() {
//        try (var connection = databaseConnection.getConnection();
//             var stmt = connection.prepareStatement(SQL_SELECT_ALL_CLIENTES)) {
//
//            List<Cliente> result = new ArrayList<>();
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    result.add(new Cliente(
//                            new IdCliente(rs.getInt("cliente_id")),
//                            rs.getString("nome"),
//                            new Cpf(rs.getString("cpf")),
//                            rs.getString("email"))
//                    );
//                }
//            }
//
//            return result;
//        } catch (SQLException e) {
//            throw new RuntimeException("(" + this.getClass().getSimpleName() + ") Database error: " + e.getMessage(), e);
//        }
//    }
}
