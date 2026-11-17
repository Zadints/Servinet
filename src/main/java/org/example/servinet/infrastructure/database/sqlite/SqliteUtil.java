package org.example.servinet.infrastructure.database.sqlite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

final class SqliteUtil {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private SqliteUtil() {
    }

    @FunctionalInterface
    interface SqlWork<T> {
        T run(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    interface SqlVoidWork {
        void run(Connection conn) throws SQLException;
    }

    static <T> T inTransaction(Connection conn, SqlWork<T> work) throws SQLException {
        boolean previous = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            T result = work.run(conn);
            conn.commit();
            return result;
        } catch (SQLException | RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw e;
        } finally {
            conn.setAutoCommit(previous);
        }
    }

    static void runInTransaction(Connection conn, SqlVoidWork work) throws SQLException {
        inTransaction(conn, c -> {
            work.run(c);
            return Boolean.TRUE;
        });
    }

    static int update(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    static boolean exists(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    static String now() {
        return toDb(LocalDateTime.now());
    }

    static String nowUtc() {
        return toDb(LocalDateTime.now(ZoneOffset.UTC));
    }

    static String toDb(LocalDateTime value) {
        return value == null ? null : FORMAT.format(value);
    }

    static LocalDateTime fromDb(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim().replace(' ', 'T'));
    }

    static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        return fromDb(rs.getString(column));
    }

    static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim().substring(0, 10));
    }

    static BigDecimal getDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    static void setDecimal(PreparedStatement stmt, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.REAL);
        } else {
            stmt.setDouble(index, value.doubleValue());
        }
    }

    static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    static void setNullableDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.REAL);
        } else {
            stmt.setDouble(index, value);
        }
    }

    static void setNullableBytes(PreparedStatement stmt, int index, byte[] value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.BLOB);
        } else {
            stmt.setBytes(index, value);
        }
    }

    static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }
}
