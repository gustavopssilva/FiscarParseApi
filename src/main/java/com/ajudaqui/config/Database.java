package com.ajudaqui.config;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {

  private static final HikariDataSource dataSource;
  private static final String URL = getEnv("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
  private static final String USER = getEnv("DB_USER", "postgres");
  private static final String PASSWORD = getEnv("DB_PASSWORD", "123456");

  static {

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(URL);
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    // Controlando e limitando o poll de conexoes
    config.setMaximumPoolSize(5);
    config.setMinimumIdle(1);

    dataSource = new HikariDataSource(config);

  }

  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  private static String getEnv(String key, String defaultValue) {
    String value = System.getenv(key);
    return value != null ? value : defaultValue;
  }
}
