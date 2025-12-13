package com.aetherguard.database;

import com.aetherguard.core.AetherGuard;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Database Manager - Premium persistent storage
 * Uses HikariCP for high-performance database connections
 */
public class DatabaseManager {
    private final AetherGuard plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(AetherGuard plugin) {
        this.plugin = plugin;
        initializeDatabase();
        createTables();
    }

    private void initializeDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:plugins/AetherGuard/data.db");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Player data table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    first_join INTEGER,
                    last_seen INTEGER,
                    total_playtime INTEGER DEFAULT 0,
                    violations INTEGER DEFAULT 0,
                    reputation REAL DEFAULT 50.0
                )
                """);

            // Violation history table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS violations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    check_name TEXT NOT NULL,
                    violation_level REAL,
                    timestamp INTEGER,
                    server TEXT,
                    details TEXT
                )
                """);

            // Pattern data for ML
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patterns (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pattern_type TEXT NOT NULL,
                    data TEXT NOT NULL,
                    confidence REAL,
                    timestamp INTEGER
                )
                """);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> savePlayerData(Player player) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT OR REPLACE INTO player_data
                (uuid, name, first_join, last_seen, total_playtime, violations, reputation)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setLong(3, System.currentTimeMillis()); // Simplified
                stmt.setLong(4, System.currentTimeMillis());
                stmt.setLong(5, 0); // Would track actual playtime
                stmt.setInt(6, 0); // Would get from violation manager
                stmt.setDouble(7, 50.0); // Default reputation

                stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save player data: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> logViolation(UUID uuid, String checkName, double level, String details) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO violations (uuid, check_name, violation_level, timestamp, server, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, checkName);
                stmt.setDouble(3, level);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.setString(5, "server1"); // Would be configurable
                stmt.setString(6, details);

                stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log violation: " + e.getMessage());
            }
        });
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}