package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.LogEntry;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogModel {

    public static void insertLog(String userUuid, String userName, String type, String information) {
        String sql = "INSERT INTO users_logs (user_uuid, user_name, log_type, information) VALUES (?, ?, ?, ?)";
        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userUuid);
            stmt.setString(2, userName);
            stmt.setString(3, type);
            stmt.setString(4, information);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo guardar el log", e);
        }
    }

    public static List<LogEntry> getLogs(String userUuid, int limit) {
        String sql = "SELECT TOP (?) user_name, log_type, information, create_at "
                + "FROM users_logs WHERE user_uuid = ? ORDER BY create_at DESC";
        Connection conn = LoadDb.getConnection();
        List<LogEntry> logs = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setString(2, userUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new LogEntry(
                            rs.getString("user_name"),
                            rs.getString("log_type"),
                            rs.getString("information"),
                            rs.getTimestamp("create_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los logs", e);
        }
        return logs;
    }


    public static Map<LocalDate, Integer> getActivityByDay(String userUuid, LocalDate from) {
        String sql = "SELECT CAST(create_at AS DATE) AS log_day, COUNT(*) AS total "
                + "FROM users_logs WHERE user_uuid = ? AND create_at >= ? "
                + "GROUP BY CAST(create_at AS DATE)";
        Connection conn = LoadDb.getConnection();
        Map<LocalDate, Integer> result = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userUuid);
            stmt.setTimestamp(2, Timestamp.valueOf(from.atStartOfDay()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getDate("log_day").toLocalDate(), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener la actividad", e);
        }
        return result;
    }
}