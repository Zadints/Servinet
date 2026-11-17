package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlAppRepository;
import org.example.servinet.domain.entities.AppGeneral;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteAppGeneral implements SqlAppRepository {

    private final DatabaseFactory databaseFactory;

    public SqliteAppGeneral(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public AppGeneral get() {
        String sql = "SELECT app_name FROM appGeneral LIMIT 1";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return new AppGeneral(
                        rs.getString("app_name"),
                        null
                );
            }

            return null;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener la configuración de la aplicación.",
                    e
            );
        }
    }

    @Override
    public void renameApp(String name) {
        if (name == null || name.isBlank() || name.length() > 12) {
            throw new IllegalArgumentException(
                    "El nombre debe tener entre 1 y 12 caracteres."
            );
        }

        String sql = "UPDATE appGeneral SET app_name = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            if (statement.executeUpdate() == 0) {
                throw new DatabaseException("No existe la configuración de la aplicación.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar el nombre de la aplicación.",
                    e
            );
        }
    }

    @Override
    public void changueImage(byte[] image) {
        String sql = "UPDATE appGeneral SET app_image = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            SqliteUtil.setNullableBytes(statement, 1, image);

            if (statement.executeUpdate() == 0) {
                throw new DatabaseException("No existe la configuración de la aplicación.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar la imagen de la aplicación.",
                    e
            );
        }
    }
}
