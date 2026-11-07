package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.sql.AppRepository;
import org.example.servinet.domain.entities.AppGeneral;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.sql.*;

public class SqlAppGeneralRepository implements AppRepository {

    private final DatabaseFactory databaseFactory;

    public SqlAppGeneralRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public AppGeneral get() {
        String sql = "{call dbo.sp_GetAppGeneral()}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql);
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

        String sql = "{call dbo.sp_RenameApp(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, name);
            statement.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar el nombre de la aplicación.",
                    e
            );
        }
    }
    @Override
    public void changueImage(byte[] image) {
        String sql = "{call dbo.sp_ChangeAppImage(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            if (image != null) {
                statement.setBytes(1, image);
            } else {
                statement.setNull(1, Types.VARBINARY);
            }

            statement.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar la imagen de la aplicación.",
                    e
            );
        }
    }
}
