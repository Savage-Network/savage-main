package com.minecraft.savagemc.core.backend.database.postgres;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.Database;
import com.minecraft.savagemc.core.backend.database.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

@Getter
public class PostgresDatabase implements Database {

    private final DatabaseCredentials credentials;
    private HikariDataSource dataSource;

    public PostgresDatabase(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void load() {
        Instant now = Instant.now();
        Savage.Savage.Logger().info("Conectando ao PostgreSQL...");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            Savage.Savage.Logger().severe("Driver PostgreSQL não encontrado no classpath.");
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase());
        config.setUsername(credentials.getUser());
        config.setPassword(credentials.getPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(config);
            createTables();
            Savage.Savage.Logger().info("Conexão com PostgreSQL efetuada com sucesso. (" + Duration.between(now, Instant.now()).toMillis() + "ms)");
        } catch (Exception e) {
            Savage.Savage.Logger().severe("Não foi possível conectar ao PostgreSQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // -------------------------
            // accounts
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "  id   VARCHAR(36)  PRIMARY KEY," +
                "  name VARCHAR(64)  NOT NULL," +
                "  ip   VARCHAR(64)," +
                "  data JSONB        NOT NULL" +
                ")"
            );
            // Garante que a coluna ip existe em tabelas criadas antes desta versão
            stmt.executeUpdate("ALTER TABLE accounts ADD COLUMN IF NOT EXISTS ip VARCHAR(64)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_name      ON accounts (LOWER(name))");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_ip        ON accounts (ip)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_last_login  ON accounts ((data->'context'->>'lastLogin'))");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_created_at  ON accounts ((data->'context'->>'createdAt'))");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_rank       ON accounts ((data->'context'->'rankInfo'->>'rank'))");

            // -------------------------
            // punishments
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS punishments (" +
                "  id     VARCHAR(36) PRIMARY KEY," +
                "  player VARCHAR(36) NOT NULL," +
                "  author VARCHAR(36) NOT NULL," +
                "  data   JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_punishments_player   ON punishments (player)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_punishments_author   ON punishments (author)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_punishments_category ON punishments ((data->>'category'))");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_punishments_valid    ON punishments ((data->>'valid'))");

            // -------------------------
            // clans
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS clans (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  name VARCHAR(64) NOT NULL," +
                "  tag  VARCHAR(16) NOT NULL," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_clans_name ON clans (LOWER(name))");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_clans_tag  ON clans (LOWER(tag))");

            // -------------------------
            // seasons
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS seasons (" +
                "  id   INT   PRIMARY KEY," +
                "  data JSONB NOT NULL" +
                ")"
            );

            // -------------------------
            // members_duels
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_duels (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_duels_elo ON members_duels ((data->>'elo'))");

            // -------------------------
            // members_pvp
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_pvp (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_pvp_kills ON members_pvp ((data->>'kills'))");

            // -------------------------
            // members_bedwars
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_bedwars (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_bedwars_wins ON members_bedwars ((data->>'wins'))");

            // -------------------------
            // members_aquatic
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_aquatic (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_aquatic_wins ON members_aquatic ((data->>'wins'))");

            // -------------------------
            // members_hunger_games
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_hunger_games (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_hg_wins ON members_hunger_games ((data->>'wins'))");

            // -------------------------
            // members_sky_wars
            // -------------------------
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members_sky_wars (" +
                "  id   VARCHAR(36) PRIMARY KEY," +
                "  data JSONB       NOT NULL" +
                ")"
            );
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_sw_wins ON members_sky_wars ((data->>'wins'))");
        }
    }

    @Override
    public void unload() {
        if (isAvailable())
            dataSource.close();
    }

    @Override
    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
}
