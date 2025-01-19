package com.example.fiap.videosliceapi.tools.migration;

import com.example.fiap.videosliceapi.adapters.datasource.DatabaseConnection;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMigration implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigration.class);

    private final DatabaseConnection databaseConnection;

    @Autowired
    public DatabaseMigration(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void runMigrations() throws Exception {
        LOGGER.info("Starting Database migrations");

        try (var connection = databaseConnection.jdbcConnection()) {

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase("liquibase/dbchangelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        }

        LOGGER.info("Database migration complete");
    }

    @Override
    public void close() {
        // no-op. Currently the owner is expected to manage the connection pool
    }
}
