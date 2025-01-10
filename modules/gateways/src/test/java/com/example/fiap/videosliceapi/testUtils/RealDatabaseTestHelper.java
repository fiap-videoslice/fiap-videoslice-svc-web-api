package com.example.fiap.archburgers.testUtils;

import com.example.fiap.archburgers.adapters.datasource.DatabaseConnection;
import com.example.fiap.archburgers.tools.migration.DatabaseMigration;
import org.jetbrains.annotations.VisibleForTesting;
import org.testcontainers.containers.PostgreSQLContainer;

public class RealDatabaseTestHelper {
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:12-alpine"
    );

    public void beforeAll() throws Exception {
        postgres.start();

        try (DatabaseMigration migration = new DatabaseMigration(getConnectionPool())) {
            migration.runMigrations();
        }
    }

    public void afterAll() {
        postgres.stop();
    }

    public DatabaseConnection getConnectionPool() throws Exception {
        return new DatabaseConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    @VisibleForTesting
    public String getJdbcUrl() {
        return postgres.getJdbcUrl();
    }

    @VisibleForTesting
    public String getJdbcUsername() {
        return postgres.getUsername();
    }

    @VisibleForTesting
    public String getJdbcPassword() {
        return postgres.getPassword();
    }
}
