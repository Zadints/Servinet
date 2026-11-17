package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlComplexRepository;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.Maintenance;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqliteAntenna implements SqlComplexRepository<Antenna> {

    private final DatabaseFactory databaseFactory;

    public SqliteAntenna(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void add(Antenna entity) {
        if (entity == null || entity.getUuid() == null
                || entity.getUuid().isBlank()) {
            throw new IllegalArgumentException(
                    "La antena y su UUID son obligatorios."
            );
        }

        String sql = "INSERT INTO antennas (uuid, priority, name, for_repair, for_maintenance, "
                + "date_create, date_last_maintenance, count_days_on, count_days_off, status, "
                + "image, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, entity.getUuid());
            statement.setInt(2, entity.getPriority());
            statement.setString(3, entity.getName());
            statement.setBoolean(4, entity.isForReair());
            statement.setBoolean(5, entity.isForMaintenance());
            statement.setString(6, SqliteUtil.toDb(entity.getDateCreate()));
            SqliteUtil.setNullableString(
                    statement, 7, SqliteUtil.toDb(entity.getDateLastMaintenance())
            );
            statement.setInt(8, entity.getCountDaysOn());
            statement.setInt(9, entity.getCountDaysOff());
            statement.setString(10, entity.getStatus().name());
            SqliteUtil.setNullableBytes(statement, 11, entity.getImage());
            SqliteUtil.setNullableDouble(statement, 12, entity.getLatitude());
            SqliteUtil.setNullableDouble(statement, 13, entity.getLongitude());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo registrar la antena.", e
            );
        }
    }

    @Override
    public Antenna get(String id) {
        return null;
    }

    @Override
    public byte[] getImage(String id) {
        String sql = "SELECT image FROM antennas WHERE uuid = ?";

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El UUID de la antena es obligatorio."
            );
        }

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("image");
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener la imagen de la antena.",
                    e
            );
        }
    }

    @Override
    public List<Antenna> getAll() {
        String antennaSql = "SELECT uuid, priority, name, for_repair, for_maintenance, date_create, "
                + "date_last_maintenance, count_days_on, count_days_off, status, image, latitude, longitude "
                + "FROM antennas ORDER BY priority DESC, name ASC";

        String maintenanceSql = "SELECT m.uuid, m.antenna_uuid, m.technical_uuid, m.description, "
                + "m.date_start, m.date_ended, m.image "
                + "FROM maintenance m INNER JOIN antennas a ON a.uuid = m.antenna_uuid "
                + "ORDER BY m.date_start DESC, m.uuid ASC";

        Map<String, Antenna> antennas = new LinkedHashMap<>();

        try (Connection conn = databaseFactory.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(antennaSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Antenna antenna = new Antenna(
                            rs.getString("uuid"),
                            rs.getShort("priority"),
                            rs.getString("name"),
                            new ArrayList<>(),
                            rs.getBoolean("for_repair"),
                            rs.getBoolean("for_maintenance"),
                            SqliteUtil.getDateTime(rs, "date_create"),
                            SqliteUtil.getDateTime(rs, "date_last_maintenance"),
                            rs.getInt("count_days_on"),
                            rs.getInt("count_days_off"),
                            rs.getBytes("image"),
                            StatusAntenna.valueOf(rs.getString("status")),
                            SqliteUtil.getNullableDouble(rs, "latitude"),
                            SqliteUtil.getNullableDouble(rs, "longitude")
                    );

                    antennas.put(antenna.getUuid(), antenna);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(maintenanceSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Antenna antenna = antennas.get(rs.getString("antenna_uuid"));

                    if (antenna == null) {
                        continue;
                    }

                    Maintenance maintenance = new Maintenance(
                            rs.getString("uuid"),
                            rs.getString("technical_uuid"),
                            rs.getString("description"),
                            SqliteUtil.getDateTime(rs, "date_start"),
                            SqliteUtil.getDateTime(rs, "date_ended"),
                            rs.getBytes("image")
                    );

                    antenna.addMaintenance(maintenance);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener las antenas y sus mantenimientos", e
            );
        }

        return new ArrayList<>(antennas.values());
    }

    @Override
    public void delete(String uuid) {
        try (Connection connection = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(connection, c -> {
                if (!SqliteUtil.exists(c, "SELECT 1 FROM antennas WHERE uuid = ?", uuid)) {
                    throw new DatabaseException("La antena no existe.");
                }

                SqliteUtil.update(c, "DELETE FROM maintenance WHERE antenna_uuid = ?", uuid);
                SqliteUtil.update(c, "DELETE FROM antennas WHERE uuid = ?", uuid);
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar la antena y sus mantenimientos.",
                    e
            );
        }
    }

    @Override
    public void update(Antenna entity) {
        String sql = "UPDATE antennas SET priority = ?, name = ?, for_repair = ?, for_maintenance = ?, "
                + "date_last_maintenance = ?, count_days_on = ?, count_days_off = ?, status = ?, "
                + "latitude = ?, longitude = ? WHERE uuid = ?";

        if (entity == null || entity.getUuid() == null
                || entity.getUuid().isBlank()) {
            throw new IllegalArgumentException(
                    "La antena y su UUID son obligatorios."
            );
        }

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, entity.getPriority());
            statement.setString(2, entity.getName());
            statement.setBoolean(3, entity.isForReair());
            statement.setBoolean(4, entity.isForMaintenance());
            SqliteUtil.setNullableString(
                    statement, 5, SqliteUtil.toDb(entity.getDateLastMaintenance())
            );
            statement.setInt(6, entity.getCountDaysOn());
            statement.setInt(7, entity.getCountDaysOff());
            statement.setString(8, entity.getStatus().name());
            SqliteUtil.setNullableDouble(statement, 9, entity.getLatitude());
            SqliteUtil.setNullableDouble(statement, 10, entity.getLongitude());
            statement.setString(11, entity.getUuid());

            if (statement.executeUpdate() == 0) {
                throw new DatabaseException("La antena no existe.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage() + e.getCause());
            throw new DatabaseException(
                    "No se pudo actualizar la antena.",
                    e
            );
        }
    }

    @Override
    public void addRelation(String antennaUuid, String clientDni) {

        try (Connection conn = databaseFactory.getConnection()) {

            String antenna = UUID.fromString(antennaUuid).toString();

            SqliteUtil.runInTransaction(conn, c -> {
                if (!SqliteUtil.exists(c, "SELECT 1 FROM antennas WHERE uuid = ?", antenna)) {
                    throw new DatabaseException("La antena no existe.");
                }

                if (!SqliteUtil.exists(c, "SELECT 1 FROM clients WHERE client_dni = ?", clientDni)) {
                    throw new DatabaseException("El cliente no existe.");
                }

                if (SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM antennasClient WHERE antenna_uuid = ? AND client_dni = ?",
                        antenna, clientDni)) {
                    throw new DatabaseException("La relación entre antena y cliente ya existe.");
                }

                SqliteUtil.update(
                        c,
                        "INSERT INTO antennasClient (antenna_uuid, client_dni) VALUES (?, ?)",
                        antenna, clientDni
                );
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al registrar la relación entre antena y cliente", e
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El UUID de la antena no es válido");
        }
    }

    @Override
    public List<String> getClientDnisByAntenna(String antennaUuid) {
        String sql = "SELECT client_dni FROM antennasClient WHERE antenna_uuid = ?";
        List<String> clients = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, antennaUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(rs.getString("client_dni"));
                }
            }

            return clients;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener los clientes de la antena", e
            );
        }
    }

    @Override
    public boolean toggleMaintenance(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("El UUID de la antena es obligatorio.");
        }

        try (Connection connection = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(connection, c -> {
                int changed = SqliteUtil.update(
                        c,
                        "UPDATE antennas SET for_maintenance = CASE WHEN for_maintenance = 1 THEN 0 ELSE 1 END "
                                + "WHERE uuid = ?",
                        uuid
                );

                if (changed == 0) {
                    throw new DatabaseException("La antena no existe.");
                }

                try (PreparedStatement statement = c.prepareStatement(
                        "SELECT for_maintenance FROM antennas WHERE uuid = ?")) {
                    statement.setString(1, uuid);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getBoolean("for_maintenance");
                        }
                    }
                }

                throw new DatabaseException("No se pudo obtener el estado de mantenimiento.");
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar el indicador de mantenimiento.", e
            );
        }
    }

    @Override
    public void updateStatus(String uuid, StatusAntenna status) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("El UUID de la antena es obligatorio.");
        }

        if (status == null) {
            throw new IllegalArgumentException("El estado de la antena es obligatorio.");
        }

        String sql = "UPDATE antennas SET status = ? WHERE uuid = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setString(2, uuid);

            if (statement.executeUpdate() == 0) {
                throw new DatabaseException("La antena no existe.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el estado de la antena.", e
            );
        }
    }

}
