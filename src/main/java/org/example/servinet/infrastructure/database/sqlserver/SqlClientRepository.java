
package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SqlClientRepository implements ClientRepository {

    private final DatabaseFactory databaseFactory;

    public SqlClientRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public List<Client> getClientsWithOrders() {

        String sql = "{call dbo.sp_GetClientsWithOrders()}";
        Map<String, Client> clients = new LinkedHashMap<>();

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql);
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
                            rs.getTimestamp("order_created").toLocalDateTime(),
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
                        rs.getTimestamp("client_register").toLocalDateTime(),
                        ClientStatus.valueOf(rs.getString("client_status")),
                        rs.getObject("latitud", Double.class),
                        rs.getObject("longitud", Double.class),
                        order,
                        null // PaymentsScore se carga posteriormente en el retorno.
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

        String sql = "{call sp_CreateClientAndOrder(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, client.getDni());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getLastName());
            statement.setString(4, client.getPhone());
            statement.setString(5, client.getAddress());
            statement.setString(6, client.getEmail());

            statement.setString(7, client.getWhatsapp());
            statement.setTimestamp(8, Timestamp.valueOf(client.getClientRegister()));
            statement.setString(9, client.getClientStatus().name());
            statement.setBigDecimal(10, BigDecimal.valueOf(client.getLongitude()));
            statement.setBigDecimal(11, BigDecimal.valueOf(client.getLatitude()));


            statement.setObject(12, order.getOrderId());
            statement.setObject(13, order.getAntennaUuid());
            statement.setObject(14, order.getPlanUuid());
            statement.setTimestamp(15, Timestamp.valueOf(order.getOrderCreated()));
            statement.setString(16, order.getOrderStatus().name());
            statement.execute();

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
        String sql = "{call dbo.sp_GetAllPlans()}";
        List<ClientPlan> plans = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClientPlan plan = new ClientPlan(
                        rs.getString("plan_id"),
                        rs.getString("plan_name"),
                        rs.getTimestamp("plan_created").toLocalDateTime(),
                        rs.getInt("plan_speed"),
                        rs.getBigDecimal("plan_price").doubleValue(),
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

        String sql = "{call dbo.sp_updateClient(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, client.getDni());

            setNullableString(stmt, 2, client.getPhone());
            setNullableString(stmt, 3, client.getAddress());
            setNullableString(stmt, 4, client.getEmail());
            setNullableString(stmt, 5, client.getWhatsapp());
            setNullableString(stmt, 6,client.getClientStatus().name());

            stmt.setString(7, client.getOrder().getPlanUuid());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el cliente " + client.getDni(),
                    e
            );
        }
    }

    private void setNullableString(
            CallableStatement stmt,
            int index,
            String value
    ) throws SQLException {

        if (value != null) {
            stmt.setString(index, value);
        } else {
            stmt.setNull(index, Types.VARCHAR);
        }
    }

    @Override
    public int getAllConnectedClients() {

        String sql = "{call dbo.sp_GetAllConnectedClients()}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
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

        String sql = "{call dbo.sp_GetConnectedClientsByAntenna(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

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

        String sql = "{call dbo.sp_AddPlan(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setObject(1, UUID.fromString(plan.getId()));
            stmt.setString(2, plan.getName());
            stmt.setTimestamp(3, Timestamp.valueOf(plan.getCreatedAt()));
            stmt.setInt(4, plan.getSpeed());
            stmt.setBigDecimal(5, BigDecimal.valueOf(plan.getPrice()));
            stmt.setBoolean(6, plan.getPromo());
            stmt.setBoolean(7, plan.getActive());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException("Error al registrar el plan", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El identificador del plan no es válido");
        }
    }

    @Override
    public void deletePlanDatabase(String planId) {

        String sql = "{call dbo.sp_DeletePlan(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setObject(1, UUID.fromString(planId));
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar el plan", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El UUID del plan no es válido");
        }
    }


    @Override
    public void deleteClientDatabase(Client client) {
        String sql = "{call dbo.sp_UpdateClientStatus(?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, client.getDni());
            stmt.setString(2, "RETIRADO");

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo retirar el cliente " + client.getDni(),
                    e
            );
        }
    }

    /**
     * Convierte una columna DATETIME/DATETIME2 a LocalDateTime.
     * Devuelve null cuando la columna SQL contiene NULL.
     */
    private static LocalDateTime getLocalDateTime(
            ResultSet rs,
            String column
    ) throws SQLException {

        Timestamp timestamp = rs.getTimestamp(column);

        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }
}
