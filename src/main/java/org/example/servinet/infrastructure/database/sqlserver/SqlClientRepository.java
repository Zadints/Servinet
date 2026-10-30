
package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.ui.events.ImageConverter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlClientRepository implements ClientRepository {

    private final DatabaseFactory databaseFactory;

    public SqlClientRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public List<OrderReportDto> getAllClientsOrderDatabase() {
        String sql = "{call dbo.sp_GetOrders()}";
        List<OrderReportDto> ordersReport = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordersReport.add(mapRowToOrderReport(rs));
            }

            return ordersReport;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los pedidos de los clientes", e
            );
        }
    }

    public void setOrderDatabase(Client client, String whatsAppNum) {
        String sql = "{call dbo.sp_CreateOrder(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // 1. order_id
            // Usa aquí el método real de tu GenerateIdUseCase.
            stmt.setString(1, UUID.randomUUID().toString());

            // 2-8. Datos del cliente
            stmt.setString(2, client.getDni());
            stmt.setString(3, client.getFirstName());
            stmt.setString(4, client.getLastName());
            stmt.setString(5, client.getPhone());
            stmt.setString(6, client.getAddress());
            stmt.setString(7, client.getEmail());
            stmt.setString(8, whatsAppNum);

            // 9. plan_id
            if (client.getPlan() != null) {
                stmt.setString(9, client.getPlan().getId());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            // 10. report_id
            stmt.setString(10, UUID.randomUUID().toString());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo crear el pedido del cliente "
                            + client.getDni(),
                    e
            );
        }
    }

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
                        rs.getInt("plan_speed"),
                        rs.getBigDecimal("plan_price").doubleValue(),
                        rs.getBoolean("plan_ispromo")
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

    public void updateClientDatabase(Client client, String whatsApp) {
        String sql = "{call dbo.sp_updateClient(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, client.getDni());
            stmt.setString(2, client.getFirstName());
            stmt.setString(3, client.getLastName());
            stmt.setString(4, client.getPhone());
            stmt.setString(5, client.getAddress());
            stmt.setString(6, client.getEmail());
            stmt.setString(7, whatsApp);

            if (client.getPlan() != null) {
                stmt.setString(8, client.getPlan().getId());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el cliente " + client.getDni(),
                    e
            );
        }
    }

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



    private static OrderReportDto mapRowToOrderReport(ResultSet rs)
            throws SQLException {

        OrderReportDto dto = new OrderReportDto();

        // ORDERS
        dto.setOrderId(rs.getString("order_id"));
        dto.setOrderClientDni(rs.getString("order_client_dni"));
        dto.setOrderPlanId(rs.getString("order_plan_id"));
        dto.setOrderReportId(rs.getObject("orders_report", UUID.class));

        dto.setOrderCreate(
                rs.getTimestamp("order_create").toLocalDateTime()
        );

        dto.setOrderStartInstall(
                rs.getTimestamp("order_start_install") != null
                        ? rs.getTimestamp("order_start_install").toLocalDateTime()
                        : null
        );

        dto.setOrderEndInstall(
                rs.getTimestamp("order_end_install") != null
                        ? rs.getTimestamp("order_end_install").toLocalDateTime()
                        : null
        );

        dto.setOrderElapsedMinutes(
                (Integer) rs.getObject("order_elapsed_minutes")
        );

        dto.setOrderStatus(rs.getString("order_status"));

        // CLIENTS
        dto.setClientDni(rs.getString("client_dni"));
        dto.setClientName(rs.getString("client_name"));
        dto.setClientLastName(rs.getString("client_last_name"));
        dto.setClientPhone(rs.getString("client_phone"));
        dto.setClientAddress(rs.getString("client_address"));
        dto.setClientEmail(rs.getString("client_email"));
        dto.setClientWhatsapp(rs.getString("client_whatsapp"));
        dto.setClientStatus(
                ClientStatus.valueOf(rs.getString("client_status"))
        );

        dto.setClientRegister(
                rs.getTimestamp("client_register").toLocalDateTime()
        );

        // PLANS
        dto.setPlanId(rs.getString("plan_id"));
        dto.setPlanName(rs.getString("plan_name"));

        dto.setPlanCreated(
                rs.getTimestamp("plan_created").toLocalDateTime()
        );

        dto.setPlanSpeed((Integer) rs.getObject("plan_speed"));
        dto.setPlanPrice(rs.getBigDecimal("plan_price"));
        dto.setPlanIspromo(rs.getBoolean("plan_ispromo"));

        // ORDERS REPORT
        dto.setReportId(rs.getObject("r_id", UUID.class));
        dto.setTechnicianId(rs.getObject("technician_id", UUID.class));
        dto.setReportObservation(rs.getString("r_observation"));

        dto.setReportAntennaConnect(
                rs.getObject("r_antenna_connect", UUID.class)
        );

        dto.setReportCreate(
                rs.getTimestamp("r_create").toLocalDateTime()
        );

        dto.setReportStatus(
                OrderStatus.fromValue(rs.getString("r_status"))
        );

        dto.setReportEnded(
                rs.getTimestamp("r_ended") != null
                        ? rs.getTimestamp("r_ended").toLocalDateTime()
                        : null
        );

        dto.setReportLatitude(rs.getBigDecimal("r_latitude"));
        dto.setReportLongitude(rs.getBigDecimal("r_longitude"));

        dto.setPaymentStatus(
                PlanStatusClient.valueOf(rs.getString("payment_status"))
        );

        dto.setImgUuid(rs.getString("img_uuid"));
        dto.setImgRouter(ImageConverter.toImage(rs.getBytes("img_router")));
        dto.setImgMiniant(ImageConverter.toImage(rs.getBytes("img_miniant")));
        dto.setImgAdditional(
                ImageConverter.toImage(rs.getBytes("img_additional"))
        );

        return dto;
    }
}
