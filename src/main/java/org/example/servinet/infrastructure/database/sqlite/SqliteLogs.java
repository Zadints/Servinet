package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlLogsRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class SqliteLogs implements SqlLogsRepository {

    private final DatabaseFactory databaseFactory;

    public SqliteLogs(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void insertLog(
            String userName,
            String type,
            String information) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                String userUuid = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT uuid FROM users WHERE display = ?")) {
                    stmt.setString(1, userName);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            userUuid = rs.getString("uuid");
                        }
                    }
                }

                if (userUuid == null) {
                    throw new DatabaseException("No se encontró el usuario indicado.");
                }

                SqliteUtil.update(
                        c,
                        "INSERT INTO users_logs (user_uuid, user_name, log_type, information, create_at) "
                                + "VALUES (?, ?, ?, ?, ?)",
                        userUuid, userName, type, information, SqliteUtil.now()
                );
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo guardar el log", e);
        }
    }

    @Override
    public Map<LogType, List<LogEntry>> getLogs(int limit) {

        if (limit <= 10) {
            throw new IllegalArgumentException(
                    "El límite debe ser mayor que cero."
            );
        }

        String sql = """
            SELECT user_name, log_type, information, create_at
            FROM users_logs
            ORDER BY create_at DESC
            LIMIT ?
            """;

        Map<LogType, List<LogEntry>> logs = new EnumMap<>(LogType.class);

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    LogType type = LogType.valueOf(
                            rs.getString("log_type")
                    );

                    LogEntry entry = new LogEntry(
                            rs.getString("user_name"),
                            type,
                            rs.getString("information"),
                            SqliteUtil.getDateTime(rs, "create_at")
                    );

                    logs.computeIfAbsent(
                            type,
                            k -> new ArrayList<>()
                    ).add(entry);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los logs", e
            );
        }

        return logs;
    }


    @Override
    public Map<LogType, List<LogEntry>> getLogs(String userName,int limit) {

        if (limit <= 10) {
            throw new IllegalArgumentException(
                    "El límite debe ser mayor que cero."
            );
        }

        String sql = """
            SELECT user_name, log_type, information, create_at
            FROM users_logs WHERE user_name = ?
            ORDER BY create_at DESC
            LIMIT ?
            """;

        Map<LogType, List<LogEntry>> logs = new EnumMap<>(LogType.class);

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setString(2, userName);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    LogType type = LogType.valueOf(
                            rs.getString("log_type")
                    );

                    LogEntry entry = new LogEntry(
                            rs.getString("user_name"),
                            type,
                            rs.getString("information"),
                            SqliteUtil.getDateTime(rs, "create_at")
                    );

                    logs.computeIfAbsent(
                            type,
                            k -> new ArrayList<>()
                    ).add(entry);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los logs", e
            );
        }

        return logs;
    }

    @Override
    public Map<LocalDate, Integer> getActivityByDay(
            String userUuid,
            LocalDate from) {

        String sql = "SELECT substr(create_at, 1, 10) AS log_day, COUNT(*) AS total "
                + "FROM users_logs WHERE user_uuid = ? AND create_at >= ? "
                + "GROUP BY substr(create_at, 1, 10) ORDER BY log_day LIMIT = 300";
        Map<LocalDate, Integer> result = new HashMap<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userUuid);
            stmt.setString(2, SqliteUtil.toDb(from.atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(
                            SqliteUtil.getDate(rs, "log_day"),
                            rs.getInt("total")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener la actividad", e);
        }

        return result;
    }
}
