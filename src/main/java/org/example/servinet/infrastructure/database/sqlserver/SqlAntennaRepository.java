package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.Maintenance;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqlAntennaRepository implements ComplexRepository<Antenna> {

    private final DatabaseFactory databaseFactory;

    public SqlAntennaRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }


    @Override
    public void add(Antenna entity) {
        String sql = "{call dbo.sp_InsertAntenna(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        if (entity == null || entity.getUuid() == null
                || entity.getUuid().isBlank()) {
            throw new IllegalArgumentException(
                    "La antena y su UUID son obligatorios."
            );
        }

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, entity.getUuid());
            statement.setInt(2, entity.getPriority());
            statement.setString(3, entity.getName());
            statement.setBoolean(4, entity.isForReair());
            statement.setBoolean(5, entity.isForMaintenance());
            statement.setTimestamp(
                    6, Timestamp.valueOf(entity.getDateCreate())
            );

            if (entity.getDateLastMaintenance() != null) {
                statement.setTimestamp(
                        7, Timestamp.valueOf(entity.getDateLastMaintenance())
                );
            } else {
                statement.setNull(7, Types.TIMESTAMP);
            }

            statement.setInt(8, entity.getCountDaysOn());
            statement.setInt(9, entity.getCountDaysOff());
            statement.setString(10, entity.getStatus().name());

            if (entity.getLatitude() != null) {
                statement.setBigDecimal(
                        11, BigDecimal.valueOf(entity.getLatitude())
                );
            } else {
                statement.setNull(11, Types.DECIMAL);
            }

            statement.execute();

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
        String sql = "{call dbo.sp_GetAntennaImage(?)}";

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El UUID de la antena es obligatorio."
            );
        }

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

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
        String sql = "{call dbo.sp_GetAntennasWithMaintenance()}";

        Map<String, Antenna> antennas = new LinkedHashMap<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            boolean hasResultSet = stmt.execute();
            int resultIndex = 0;

            while (true) {
                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {

                        if (resultIndex == 0) {
                            while (rs.next()) {
                                java.math.BigDecimal latitude =
                                        rs.getBigDecimal("latitude");

                                java.math.BigDecimal longitude =
                                        rs.getBigDecimal("longitude");

                                Timestamp dateCreate =
                                        rs.getTimestamp("date_create");

                                Timestamp dateLastMaintenance =
                                        rs.getTimestamp("date_last_maintenance");

                                Antenna antenna = new Antenna(
                                        rs.getString("uuid"),
                                        rs.getShort("priority"),
                                        rs.getString("name"),
                                        new ArrayList<>(),
                                        rs.getBoolean("for_repair"),
                                        rs.getBoolean("for_maintenance"),
                                        dateCreate.toLocalDateTime(),
                                        dateLastMaintenance != null
                                                ? dateLastMaintenance.toLocalDateTime()
                                                : null,
                                        rs.getInt("count_days_on"),
                                        rs.getInt("count_days_off"),
                                        rs.getBytes("image"), //la imagen se carga bajo demanda mijines
                                        StatusAntenna.valueOf(rs.getString("status")),
                                        latitude != null ? latitude.doubleValue() : null,
                                        longitude != null ? longitude.doubleValue() : null
                                );

                                antennas.put(antenna.getUuid(), antenna);
                            }
                        }


                        else if (resultIndex == 1) {
                            while (rs.next()) {
                                String antennaUuid = rs.getString("antenna_uuid");

                                Antenna antenna = antennas.get(antennaUuid);

                                if (antenna == null) {
                                    continue;
                                }

                                Timestamp start = rs.getTimestamp("date_start");
                                Timestamp end = rs.getTimestamp("date_ended");

                                Maintenance maintenance = new Maintenance(
                                        rs.getString("uuid"),
                                        rs.getString("technical_uuid"),
                                        rs.getString("description"),
                                        start.toLocalDateTime(),
                                        end != null
                                                ? end.toLocalDateTime()
                                                : null,
                                        rs.getBytes("image")
                                );

                                antenna.addMaintenance(maintenance);
                            }
                        }
                    }

                    resultIndex++;
                } else if (stmt.getUpdateCount() == -1) {
                    break;
                }

                hasResultSet = stmt.getMoreResults();
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
        String sql = "{call dbo.sp_DeleteAntenna(?)}";


        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, uuid);
            statement.execute();


        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar la antena y sus mantenimientos.",
                    e
            );
        }
    }


    @Override
    public void update(Antenna entity) {
        String sql = "{call dbo.sp_UpdateAntenna(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        if (entity == null || entity.getUuid() == null
                || entity.getUuid().isBlank()) {
            throw new IllegalArgumentException(
                    "La antena y su UUID son obligatorios."
            );
        }

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, entity.getUuid());
            statement.setInt(2, entity.getPriority());
            statement.setString(3, entity.getName());
            statement.setBoolean(4, entity.isForReair());
            statement.setBoolean(5, entity.isForMaintenance());

            if (entity.getDateLastMaintenance() != null) {
                statement.setTimestamp(
                        6,
                        Timestamp.valueOf(entity.getDateLastMaintenance())
                );
            } else {
                statement.setNull(6, Types.TIMESTAMP);
            }

            statement.setInt(7, entity.getCountDaysOn());
            statement.setInt(8, entity.getCountDaysOff());
            statement.setString(9, entity.getStatus().name());

            if (entity.getLatitude() != null) {
                statement.setBigDecimal(
                        10, BigDecimal.valueOf(entity.getLatitude())
                );
            } else {
                statement.setNull(10, Types.DECIMAL);
            }

            if (entity.getLongitude() != null) {
                statement.setBigDecimal(
                        11, BigDecimal.valueOf(entity.getLongitude())
                );
            } else {
                statement.setNull(11, Types.DECIMAL);
            }

            statement.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar la antena.",
                    e
            );
        }
    }

}
