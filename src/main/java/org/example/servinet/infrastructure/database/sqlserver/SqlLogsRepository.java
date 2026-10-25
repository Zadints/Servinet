package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.LogsRepository;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.InMemoryLogsRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlLogsRepository implements LogsRepository {

    private final DatabaseFactory databaseFactory;

    public SqlLogsRepository(DatabaseFactory databaseFactory) {
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

            stmt.setString(2, userName);
            stmt.setString(2, type);
            stmt.setString(3, information);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo guardar el log", e);
        }
    }
    @Override
    public InMemoryRepository<LogEntry> getLogs(
            String userUuid,
            int limit) {

        String sql = "{call dbo.sp_GetLogs(?, ?)}";
        List<LogEntry> logs = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, userUuid);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("create_at");

                    logs.add(new LogEntry(
                            rs.getString("user_name"),
                            rs.getString("log_type"),
                            rs.getString("information"),
                            timestamp.toLocalDateTime()
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los logs", e);
        }

        InMemoryRepository<LogEntry> memory = new InMemoryLogsRepository();
        memory.setAll(logs);
        return memory;
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