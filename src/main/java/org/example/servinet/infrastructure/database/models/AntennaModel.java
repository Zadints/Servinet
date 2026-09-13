package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Antenna;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.Role;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AntennaModel {

    public static User deleteAntennaDatabase(Antenna ant) {

        String sql = """
        SELECT *FROM users WHERE display = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    User user = new User(
                            rs.getString("uuid"),
                            rs.getString("email"),
                            Role.valueOf(rs.getString("rol")),
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("password_hash"),
                            rs.getString("display"),
                            rs.getString("perfil_img")
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el usuario" + name, e );
        }

        return null;
    }

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
                        new ArrayList<>(), // maintenance
                        rs.getBoolean("for_repair"),
                        rs.getBoolean("for_maintenance"),
                        rs.getTimestamp("date_create").toLocalDateTime(),
                        rs.getTimestamp("date_last_maintenance") != null
                                ? rs.getTimestamp("date_last_maintenance").toLocalDateTime()
                                : null,
                        rs.getInt("count_days_on"),
                        rs.getInt("count_days_off"),
                        ImageConverter.toImage(rs.getString("image")),
                        StatusAntenna.valueOf(rs.getString("status"))
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
