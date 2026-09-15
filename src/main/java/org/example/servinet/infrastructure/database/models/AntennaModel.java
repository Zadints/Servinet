package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Antenna;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AntennaModel {

    public static void deleteAntennaDatabase(Antenna ant) {

        String sql = """
        DELETE FROM antennas
        WHERE uuid = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ant.getUuid());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar la antena " + ant.getUuid(), e
            );
        }
    }
    //Admin
//Cesar2014abc.
    public static List<Antenna> getAllAntennaDatabase() {

        String sql = """
        SELECT *
        FROM antennas
        """;

        Connection conn = LoadDb.getConnection();
        List<Antenna> antennas = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Antenna antenna = new Antenna(
                        rs.getString("uuid"),
                        rs.getShort("priority"),
                        rs.getString("name"),
                        rs.getBoolean("for_repair"),
                        rs.getBoolean("for_maintenance"),
                        rs.getTimestamp("date_last_maintenance").toLocalDateTime(),
                        rs.getInt("count_days_on"),
                        rs.getInt("count_days_off"),
                        ImageConverter.toImage(rs.getString("image")),
                        StatusAntenna.valueOf(rs.getString("status")),
                        rs.getTimestamp("date_create").toLocalDateTime()
                );

                antennas.add(antenna);
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener las antenas",
                    e
            );
        }

        return antennas;
    }

    public static void setAntennaDatabase(Antenna ant) {

        String sql = """
        
        INSERT INTO antennas (
             uuid,
             name,
             for_repair,
             for_maintenance,
             date_create,
             date_last_maintenance,
             image,
             status
         )
        VALUES (?, ?, ?, ?, ?, ?, ?,?);
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ant.getUuid());
            stmt.setString(2, ant.getName());
            stmt.setBoolean(3, ant.isForReair());
            stmt.setBoolean(4, ant.isForMaintenance());
            stmt.setTimestamp(5, Timestamp.valueOf(ant.getDateCreate()));
            stmt.setTimestamp(6, Timestamp.valueOf(ant.getDateLastMaintenance()));
            stmt.setInt(7, ant.getCountDaysOn());
            stmt.setInt(8, ant.getCountDaysOff());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos", e
            );
        }
    }
}
