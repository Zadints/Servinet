package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Client;
import org.example.servinet.core.domain.entities.ClientPlan;
import org.example.servinet.core.domain.enums.ClientStatus;
import org.example.servinet.core.domain.enums.PlanStatusClient;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientModel {

    public static List<Client> getAllClientsDatabase() {

        String sql = """
        SELECT *
        FROM cliente
        ORDER BY fecha_registro DESC
        """;

        Connection conn = LoadDb.getConnection();
        List<Client> clients = new ArrayList<>();

        if (conn == null) {
            return clients;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRowToClient(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los clientes", e);
        }

        return clients;
    }

    public static void setClientDatabase(Client client) {

        String sql = """
        INSERT INTO cliente (
             id_cliente, dni, correo, nombres, apellidos, telefono, zona, direccion,
             nombre_plan, fecha_instalacion, fecha_registro, estado_cliente, estado_pago, foto_router
         )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException("No hay conexión disponible con la base de datos", null);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getUuid());
            stmt.setString(2, client.getDni());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getFirstName());
            stmt.setString(5, client.getLastName());
            stmt.setString(6, client.getPhone());
            stmt.setString(7, client.getZone());
            stmt.setString(8, client.getAddress());
            stmt.setString(9, client.getPlan() != null ? client.getPlan().getId() : null);

            if (client.getInstallationDate() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(client.getInstallationDate()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }

            stmt.setTimestamp(11, Timestamp.valueOf(client.getDateCreate()));
            stmt.setString(12, client.getClientStatus().name());
            stmt.setString(13, client.getPlanStatus().name());
            stmt.setNull(14, Types.VARBINARY);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error al guardar el cliente en la base de datos", e);
        }
    }

    public static void updateClientDatabase(Client client) {

        String sql = """
        UPDATE cliente SET
            dni = ?, correo = ?, nombres = ?, apellidos = ?, telefono = ?,
            zona = ?, direccion = ?, nombre_plan = ?, fecha_instalacion = ?,
            estado_cliente = ?, estado_pago = ?
        WHERE id_cliente = ?
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException("No hay conexión disponible con la base de datos", null);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getDni());
            stmt.setString(2, client.getEmail());
            stmt.setString(3, client.getFirstName());
            stmt.setString(4, client.getLastName());
            stmt.setString(5, client.getPhone());
            stmt.setString(6, client.getZone());
            stmt.setString(7, client.getAddress());
            stmt.setString(8, client.getPlan() != null ? client.getPlan().getId() : null);

            if (client.getInstallationDate() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(client.getInstallationDate()));
            } else {
                stmt.setNull(9, Types.TIMESTAMP);
            }

            stmt.setString(10, client.getClientStatus().name());
            stmt.setString(11, client.getPlanStatus().name());
            stmt.setString(12, client.getUuid());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo actualizar el cliente " + client.getUuid(), e);
        }
    }

    public static void deleteClientDatabase(Client client) {

        String sql = """
        DELETE FROM cliente
        WHERE id_cliente = ?
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException("No hay conexión disponible con la base de datos", null);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getUuid());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el cliente " + client.getUuid(), e);
        }
    }

    private static Client mapRowToClient(ResultSet rs) throws SQLException {

        String nombrePlan = rs.getString("nombre_plan");
        ClientPlan plan = nombrePlan != null
                ? new ClientPlan(nombrePlan, nombrePlan, null, null, true)
                : null;

        byte[] fotoRouter = rs.getBytes("foto_router");

        return new Client(
                rs.getString("id_cliente"),
                rs.getString("dni"),
                rs.getString("correo"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("telefono"),
                rs.getString("zona"),
                rs.getString("direccion"),
                plan,
                rs.getTimestamp("fecha_instalacion") != null
                        ? rs.getTimestamp("fecha_instalacion").toLocalDateTime()
                        : null,
                rs.getTimestamp("fecha_registro").toLocalDateTime(),
                ClientStatus.valueOf(rs.getString("estado_cliente")),
                PlanStatusClient.valueOf(rs.getString("estado_pago")),
                fotoRouter != null ? ImageConverter.toImage(fotoRouter) : null
        );
    }
}