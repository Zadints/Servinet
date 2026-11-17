package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlClientRepository;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqliteClient implements SqlClientRepository {

    private final DatabaseFactory databaseFactory;

    public SqliteClient(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public List<Client> getClientsWithOrders() {

        String sql = "SELECT c.client_dni, c.client_name, c.client_last_name, c.client_phone, "
                + "c.client_address, c.client_email, c.client_whatsapp, c.client_register, "
                + "c.client_status, c.longitud, c.latitud, "
                + "o.order_id, o.order_antenna_connect, o.order_plan_connect, "
                + "o.order_created, o.order_status "
                + "FROM clients c LEFT JOIN orders o ON o.order_client_dni = c.client_dni "
                + "ORDER BY c.client_register DESC, o.order_created DESC";

        Map<String, Client> clients = new LinkedHashMap<>();

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                String dni = rs.getString("client_dni");

                if (clients.containsKey(dni)) {
                    continue;
                }
                Order order = null;

                String orderId = rs.getString("order_id");

                if (orderId != null) {
                    order = new Order(
                            orderId,
                            rs.getString("order_antenna_connect"),
                            rs.getString("order_plan_connect"),
                            SqliteUtil.getDateTime(rs, "order_created"),
                            OrderStatus.valueOf(rs.getString("order_status"))
                    );
                }

                Client client = new Client(
                        dni,
                        rs.getString("client_name"),
                        rs.getString("client_last_name"),
                        rs.getString("client_phone"),
                        rs.getString("client_address"),
                        rs.getString("client_email"),
                        rs.getString("client_whatsapp"),
                        SqliteUtil.getDateTime(rs, "client_register"),
                        ClientStatus.valueOf(rs.getString("client_status")),
                        SqliteUtil.getNullableDouble(rs, "latitud"),
                        SqliteUtil.getNullableDouble(rs, "longitud"),
                        order,
                        null
                );

                clients.put(dni, client);
            }

            return new ArrayList<>(clients.values());

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener clientes con sus órdenes", e
            );
        }
    }

    @Override
    public void createClientAndOrder(Client client, Order order) {

        String clientSql = "INSERT INTO clients (client_dni, client_name, client_last_name, client_phone, "
                + "client_address, client_email, client_whatsapp, client_register, client_status, "
                + "longitud, latitud) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String orderSql = "INSERT INTO orders (order_id, order_client_dni, order_antenna_connect, "
                + "order_plan_connect, order_created, order_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(connection, c -> {

                try (PreparedStatement statement = c.prepareStatement(clientSql)) {
                    statement.setString(1, client.getDni());
                    statement.setString(2, client.getFirstName());
                    statement.setString(3, client.getLastName());
                    statement.setString(4, client.getPhone());
                    statement.setString(5, client.getAddress());
                    statement.setString(6, client.getEmail());
                    statement.setString(7, client.getWhatsapp());
                    statement.setString(8, SqliteUtil.toDb(client.getClientRegister()));
                    statement.setString(9, client.getClientStatus().name());
                    SqliteUtil.setNullableDouble(statement, 10, client.getLongitude());
                    SqliteUtil.setNullableDouble(statement, 11, client.getLatitude());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = c.prepareStatement(orderSql)) {
                    statement.setString(1, String.valueOf(order.getOrderId()));
                    statement.setString(2, client.getDni());
                    SqliteUtil.setNullableString(
                            statement, 3,
                            order.getAntennaUuid() == null ? null : String.valueOf(order.getAntennaUuid())
                    );
                    statement.setString(4, String.valueOf(order.getPlanUuid()));
                    statement.setString(5, SqliteUtil.toDb(order.getOrderCreated()));
                    statement.setString(6, order.getOrderStatus().name());
                    statement.executeUpdate();
                }
            });

        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException(
                    "No se pudo crear el pedido del cliente "
                            + client.getDni(),
                    e
            );
        }
    }

    @Override
    public List<ClientPlan> getAllPlansDatabase() {
        String sql = "SELECT plan_id, plan_name, plan_created, plan_speed, plan_price, "
                + "plan_ispromo, plan_isactive FROM plans";
        List<ClientPlan> plans = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClientPlan plan = new ClientPlan(
                        rs.getString("plan_id"),
                        rs.getString("plan_name"),
                        SqliteUtil.getDateTime(rs, "plan_created"),
                        rs.getInt("plan_speed"),
                        rs.getDouble("plan_price"),
                        rs.getBoolean("plan_ispromo"),
                        rs.getBoolean("plan_isactive")
                );
                plans.add(plan);
            }

            return plans;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los planes", e
            );
        }
    }

    @Override
    public void updateClientDatabase(Client client) {

        String sql = "UPDATE clients SET client_phone = COALESCE(?, client_phone), "
                + "client_address = COALESCE(?, client_address), "
                + "client_email = COALESCE(?, client_email), "
                + "client_whatsapp = COALESCE(?, client_whatsapp), "
                + "client_status = COALESCE(?, client_status) "
                + "WHERE client_dni = ?";

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                String planId = client.getOrder() != null && client.getOrder().getPlanUuid() != null
                        ? String.valueOf(client.getOrder().getPlanUuid())
                        : null;

                if (!SqliteUtil.exists(c, "SELECT 1 FROM clients WHERE client_dni = ?", client.getDni())) {
                    throw new DatabaseException("El cliente no existe.");
                }

                if (planId != null
                        && !SqliteUtil.exists(c, "SELECT 1 FROM plans WHERE plan_id = ?", planId)) {
                    throw new DatabaseException("El plan seleccionado no existe.");
                }

                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    setNullableString(stmt, 1, client.getPhone());
                    setNullableString(stmt, 2, client.getAddress());
                    setNullableString(stmt, 3, client.getEmail());
                    setNullableString(stmt, 4, client.getWhatsapp());
                    setNullableString(stmt, 5, client.getClientStatus().name());
                    stmt.setString(6, client.getDni());
                    stmt.executeUpdate();
                }

                if (planId != null) {
                    SqliteUtil.update(
                            c,
                            "UPDATE orders SET order_plan_connect = ? WHERE order_client_dni = ?",
                            planId, client.getDni()
                    );
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el cliente " + client.getDni(),
                    e
            );
        }
    }

    private void setNullableString(
            PreparedStatement stmt,
            int index,
            String value
    ) throws SQLException {
        SqliteUtil.setNullableString(stmt, index, value);
    }

    @Override
    public int getAllConnectedClients() {

        String sql = "SELECT COUNT(DISTINCT c.client_dni) AS total_connections "
                + "FROM clients c INNER JOIN orders o ON o.order_client_dni = c.client_dni "
                + "WHERE c.client_status IN ('ACTIVO', 'SUSPENDIDO') "
                + "AND o.order_antenna_connect IS NOT NULL";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt("total_connections") : 0;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener el total de clientes conectados: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public int getConnectedClientsByAntenna(String antennaUuid) {

        String sql = "SELECT COUNT(DISTINCT c.client_dni) AS total_connections "
                + "FROM clients c INNER JOIN orders o ON o.order_client_dni = c.client_dni "
                + "WHERE o.order_antenna_connect = ? "
                + "AND c.client_status IN ('ACTIVO', 'SUSPENDIDO')";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, antennaUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total_connections") : 0;
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener las conexiones de la antena: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public void addPlan(ClientPlan plan) {

        String sql = "INSERT INTO plans (plan_id, plan_name, plan_created, plan_speed, plan_price, "
                + "plan_ispromo, plan_isactive) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.fromString(plan.getId()).toString());
            stmt.setString(2, plan.getName());
            stmt.setString(3, SqliteUtil.toDb(plan.getCreatedAt()));
            stmt.setInt(4, plan.getSpeed());
            SqliteUtil.setDecimal(stmt, 5, BigDecimal.valueOf(plan.getPrice()));
            stmt.setBoolean(6, plan.getPromo());
            stmt.setBoolean(7, plan.getActive());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error al registrar el plan", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El identificador del plan no es válido");
        }
    }

    @Override
    public void deletePlanDatabase(String planId) {

        String sql = "DELETE FROM plans WHERE plan_id = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.fromString(planId).toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar el plan", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El UUID del plan no es válido");
        }
    }

    @Override
    public void deleteClientDatabase(Client client) {
        String sql = "UPDATE clients SET client_status = ? WHERE client_dni = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "RETIRADO");
            stmt.setString(2, client.getDni());

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("No se encontró un cliente con ese DNI.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo retirar el cliente " + client.getDni(),
                    e
            );
        }
    }
}
