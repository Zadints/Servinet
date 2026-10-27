package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.usecase.GenerateIdUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.ui.ImageConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientModel {
    public static List<OrderReportDto> getAllClientsOrderDatabase() {

        String sql = """
        EXEC sp_GetOrders;
        """;

        Connection conn = LoadDb.getConnection();
        List<OrderReportDto> ordersReport = new ArrayList<>();

        if (conn == null) {
            return ordersReport;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordersReport.add(mapRowToOrderReport(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los clientes", e);
        }

        return ordersReport;
    }

    public static void setOrderDatabase(Client client, String whatsAppNum) {

        String sql = """
        EXEC sp_CreateOrder
            @order_id = ?,
            @client_dni = ?,
            @client_name = ?,
            @client_last_name = ?,
            @client_phone = ?,
            @client_address = ?,
            @client_email = ?,
            @client_whatsapp = ?,
            @plan_id = ?,
            @report_id = ?;
        """;


        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException("No hay conexión disponible con la base de datos", null);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, new GenerateIdUseCase(new UuidGenerator()).toString());
            stmt.setString(2, client.getDni());
            stmt.setString(3, client.getFirstName());
            stmt.setString(4, client.getLastName());
            stmt.setString(5, client.getPhone());
            stmt.setString(6, client.getAddress());
            stmt.setString(7, client.getEmail());
            stmt.setString(8, whatsAppNum);
            stmt.setObject(10, new GenerateIdUseCase(new UuidGenerator()).toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo crear el pedido "
            );
        }
    }

    public static List<ClientPlan> getAllPlansDatabase() {

        String sql = "EXEC sp_GetAllPlans";

        List<ClientPlan> plans = new ArrayList<>();

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos",
                    null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
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

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los planes",
                    e
            );
        }

        return plans;
    }

    public static void updateClientDatabase(Client client,String whatsApp) {

        String sql = """
        EXEC sp_updateClient
            @client_dni = ?,
            @client_name = ?,
            @client_last_name = ?,
            @client_phone = ?,
            @client_address = ?,
            @client_email = ?,
            @client_whatsapp = ?,
            @plan_id = ?;
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos",
                    null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getDni());
            stmt.setString(2, client.getFirstName());
            stmt.setString(3, client.getLastName());
            stmt.setString(4, client.getPhone());
            stmt.setString(5, client.getAddress());
            stmt.setString(6, client.getEmail());

            stmt.setString(7,whatsApp);

            stmt.setString(8, client.getPlan().getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el cliente",
                    e
            );
        }
    }

    public static void deleteClientDatabase(Client client) {

        String sql = """
        EXEC sp_UpdateClientStatus
            @client_dni = ?,
            @client_status = ?;
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos", null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getDni());
            stmt.setString(2, "RETIRADO");

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo retirar el cliente " + client.getDni(), e
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
        dto.setClientStatus(ClientStatus.valueOf(rs.getString("client_status")));
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

        dto.setReportStatus(OrderStatus.fromValue(rs.getString("r_status")));

        dto.setReportEnded(
                rs.getTimestamp("r_ended") != null
                        ? rs.getTimestamp("r_ended").toLocalDateTime()
                        : null
        );

        dto.setReportLatitude(rs.getBigDecimal("r_latitude"));
        dto.setReportLongitude(rs.getBigDecimal("r_longitude"));
        dto.setPaymentStatus(PlanStatusClient.valueOf(rs.getString("payment_status")));

        dto.setImgUuid(rs.getString("img_uuid"));
        dto.setImgRouter(ImageConverter.toImage(rs.getBytes("img_router")));
        dto.setImgMiniant(ImageConverter.toImage(rs.getBytes("img_miniant")));
        dto.setImgAdditional(ImageConverter.toImage(rs.getBytes("img_additional")));
        return dto;
    }
}