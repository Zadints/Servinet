package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.domain.exception.DatabaseException;

import java.sql.*;

public class AppGeneralModel {

    public static void updateAppDatabase(String name) {
        Connection conn = LoadDb.getConnection();
        String sql = """
            EXEC sp_ChangueAppName @new_name = ?;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el nombre de la app",
                    e
            );
        }
    }

    public static String getAppGeneralDatabase() {

        String sql = """
        SELECT name
        FROM appGeneral
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener la configuración de la aplicación", e
            );
        }

        return null;
    }
}
