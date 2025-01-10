package com.example.fiap.archburgers.adapters.datasource;

import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseConnectionTest {

    @Test
    public void constructor_dbUrlIsMissing() {
        Environment environment = new StaticEnvironment(Map.of());

        assertThatThrownBy(() -> new DatabaseConnection(environment))
                .hasMessage("archburgers.datasource.dbUrl env is missing");
    }

    @Test
    public void constructor_dbUserIsMissing() {
        Environment environment = new StaticEnvironment(Map.of(
                "archburgers.datasource.dbUrl", "jdbc:postgresql://localhost/mydb"
        ));

        assertThatThrownBy(() -> new DatabaseConnection(environment))
                .hasMessage("archburgers.datasource.dbUser env is missing");
    }

    @Test
    public void constructor_dbPassIsMissing() {
        Environment environment = new StaticEnvironment(Map.of(
                "archburgers.datasource.dbUrl", "jdbc:postgresql://localhost/mydb",
                "archburgers.datasource.dbUser", "user"
        ));

        assertThatThrownBy(() -> new DatabaseConnection(environment))
                .hasMessage("archburgers.datasource.dbPass env is missing");
    }
}