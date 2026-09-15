package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;

public class AppGeneralModel {

    public static void updateAppDatabase(String name) {

        String sql = """
        UPDATE appGeneral
        SET name = ?
        """;
        Connection conn = LoadDb.getConnection();
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
