package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.sql.SqlLogsRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class SqlServerLogs implements SqlLogsRepository {

    private final DatabaseFactory databaseFactory;

    public SqlServerLogs(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }
    @Override
    public void insertLog(
            String userName,
            String type,
            String information) {

        String sql = "{call dbo.sp_InsertLog(?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, userName);
            stmt.setString(2, type);
            stmt.setString(3, information);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo guardar el log", e);
        }
    }
    @Override
    public Map<LogType, List<LogEntry>> getLogs(int limit) {

        String sql = "{call dbo.sp_GetLogs(?)}";
        Map<LogType, List<LogEntry>> logs = new EnumMap<>(LogType.class);

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("create_at");

                    LogType type = LogType.valueOf(
                            rs.getString("log_type")
                    );
                    LogEntry entry = new LogEntry(
                            rs.getString("user_name"),
                            type,
                            rs.getString("information"),
                            rs.getDate("create_at").toLocalDate().atStartOfDay()
                    );

                    logs.computeIfAbsent(type, k -> new ArrayList<>()
                    ).add(entry);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los logs", e);
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
                            rs.getDate("create_at").toLocalDate().atStartOfDay()
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

        String sql = "{call dbo.sp_GetActivityByDay(?, ?)}";
        Map<LocalDate, Integer> result = new HashMap<>();

        try (Connection conn =  databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, userUuid);
            stmt.setTimestamp(
                    2,
                    Timestamp.valueOf(from.atStartOfDay())
            );

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(
                            rs.getDate("log_day").toLocalDate(),
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