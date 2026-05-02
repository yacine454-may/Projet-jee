package com.jeu.reflexion.config;

import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSchemaFixer {

    private final DataSource dataSource;

    public DatabaseSchemaFixer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migratePlayersTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            if (tableExists(connection, "PLAYERS")) {
                migratePlayers(connection, statement);
            }
            if (tableExists(connection, "SAVED_GAMES")) {
                migrateSavedGames(connection, statement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de corriger le schema de la base", e);
        }
    }

    private void migratePlayers(Connection connection, Statement statement) throws SQLException {
        Set<String> columns = loadColumns(connection, "PLAYERS");

        if (!columns.contains("BEST_SCORE")) {
            statement.execute("ALTER TABLE PLAYERS ADD COLUMN BEST_SCORE INTEGER DEFAULT 0 NOT NULL");
            columns.add("BEST_SCORE");
        }
        if (!columns.contains("BEST_LEVEL")) {
            statement.execute("ALTER TABLE PLAYERS ADD COLUMN BEST_LEVEL VARCHAR(255) DEFAULT '-' NOT NULL");
            columns.add("BEST_LEVEL");
        }

        if (columns.contains("BESTSCORE")) {
            statement.executeUpdate("UPDATE PLAYERS SET BEST_SCORE = COALESCE(BEST_SCORE, BESTSCORE, 0)");
        }
        if (columns.contains("BESTLEVEL")) {
            statement.executeUpdate("UPDATE PLAYERS SET BEST_LEVEL = COALESCE(NULLIF(TRIM(BEST_LEVEL), ''), BESTLEVEL, '-')");
        }

        statement.executeUpdate("UPDATE PLAYERS SET BEST_SCORE = 0 WHERE BEST_SCORE IS NULL");
        statement.executeUpdate("UPDATE PLAYERS SET BEST_LEVEL = '-' WHERE BEST_LEVEL IS NULL OR TRIM(BEST_LEVEL) = ''");
        statement.execute("ALTER TABLE PLAYERS ALTER COLUMN BEST_SCORE SET DEFAULT 0");
        statement.execute("ALTER TABLE PLAYERS ALTER COLUMN BEST_LEVEL SET DEFAULT '-'");

        if (columns.contains("BESTSCORE")) {
            statement.execute("ALTER TABLE PLAYERS DROP COLUMN BESTSCORE");
        }
        if (columns.contains("BESTLEVEL")) {
            statement.execute("ALTER TABLE PLAYERS DROP COLUMN BESTLEVEL");
        }
    }

    private void migrateSavedGames(Connection connection, Statement statement) throws SQLException {
        Set<String> columns = loadColumns(connection, "SAVED_GAMES");

        if (!columns.contains("WRONG_MOVES")) {
            statement.execute("ALTER TABLE SAVED_GAMES ADD COLUMN WRONG_MOVES INTEGER DEFAULT 0 NOT NULL");
            columns.add("WRONG_MOVES");
        }
        if (!columns.contains("BOARD_STATE")) {
            statement.execute("ALTER TABLE SAVED_GAMES ADD COLUMN BOARD_STATE TEXT");
            columns.add("BOARD_STATE");
        }
        if (!columns.contains("SAVED_AT")) {
            statement.execute("ALTER TABLE SAVED_GAMES ADD COLUMN SAVED_AT TIMESTAMP");
            columns.add("SAVED_AT");
        }

        if (columns.contains("WRONGMOVES")) {
            statement.executeUpdate("UPDATE SAVED_GAMES SET WRONG_MOVES = COALESCE(WRONG_MOVES, WRONGMOVES, 0)");
        }
        if (columns.contains("BOARDSTATE")) {
            statement.executeUpdate("UPDATE SAVED_GAMES SET BOARD_STATE = COALESCE(BOARD_STATE, BOARDSTATE)");
        }
        if (columns.contains("SAVEDAT")) {
            statement.executeUpdate("UPDATE SAVED_GAMES SET SAVED_AT = COALESCE(SAVED_AT, SAVEDAT)");
        }

        statement.executeUpdate("UPDATE SAVED_GAMES SET WRONG_MOVES = 0 WHERE WRONG_MOVES IS NULL");
        statement.execute("ALTER TABLE SAVED_GAMES ALTER COLUMN WRONG_MOVES SET DEFAULT 0");
        statement.execute("ALTER TABLE SAVED_GAMES ALTER COLUMN WRONG_MOVES SET NOT NULL");

        if (columns.contains("WRONGMOVES")) {
            statement.execute("ALTER TABLE SAVED_GAMES DROP COLUMN WRONGMOVES");
        }
        if (columns.contains("BOARDSTATE")) {
            statement.execute("ALTER TABLE SAVED_GAMES DROP COLUMN BOARDSTATE");
        }
        if (columns.contains("SAVEDAT")) {
            statement.execute("ALTER TABLE SAVED_GAMES DROP COLUMN SAVEDAT");
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private Set<String> loadColumns(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }
}
