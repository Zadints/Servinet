package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.port.out.sql.SqlTechnicalRepository;
import org.example.servinet.domain.entities.InstallationReport;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.entities.client.OrderReport;
import org.example.servinet.domain.entities.technicians.TechnicalLogs;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqliteTechnical implements SqlTechnicalRepository {

    private static final String ACTION = "INTSTALACION";

    private static final String ORDER_REPORT_SELECT =
            "SELECT o.order_id, o.order_client_dni, o.order_antenna_connect, o.order_plan_connect, "
                    + "o.order_created, o.order_status, "
                    + "r.report_client_dni, r.report_observation, r.img_router, r.img_conexion, "
                    + "r.img_mini_antenna, r.router_password, r.report_create, r.report_status, r.report_ended ";

    private final DatabaseFactory databaseFactory;

    public SqliteTechnical(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    private OrderContainer mapOrderContainer(ResultSet rs) throws SQLException {
        String status = rs.getString("report_status");

        OrderStatus reportStatus = status != null
                ? OrderStatus.valueOf(status)
                : OrderStatus.PENDING;

        return new OrderContainer(
                new OrderReport(
                        rs.getString("report_client_dni"),
                        rs.getString("report_observation"),
                        rs.getBytes("img_router"),
                        rs.getBytes("img_conexion"),
                        rs.getBytes("img_mini_antenna"),
                        rs.getString("router_password"),
                        SqliteUtil.getDateTime(rs, "report_create"),
                        reportStatus,
                        SqliteUtil.getDateTime(rs, "report_ended")
                ),
                new Order(
                        rs.getString("order_id"),
                        rs.getString("order_antenna_connect"),
                        rs.getString("order_plan_connect"),
                        SqliteUtil.getDateTime(rs, "order_created"),
                        OrderStatus.valueOf(rs.getString("order_status"))
                )
        );
    }

    @Override
    public List<OrderContainer> getTechnicianLogs(String staffUuid) {

        String sql = ORDER_REPORT_SELECT
                + "FROM tecnicos_logs tl "
                + "INNER JOIN orders o ON o.order_id = tl.order_id "
                + "LEFT JOIN orders_report r ON r.order_id = o.order_id "
                + "WHERE tl.staff_uuid = ? ORDER BY tl.servicio_uuid DESC";
        List<OrderContainer> orders = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderContainer(rs));
                }
            }

            return orders;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener las órdenes del técnico", e
            );
        }
    }

    @Override
    public List<TechnicalStatic> getTechnicianStatistics() {
        String sql = "SELECT staff_uuid, COUNT(*) AS total_instalaciones FROM tecnicos_logs "
                + "WHERE accion_tipo = '" + ACTION + "' GROUP BY staff_uuid "
                + "ORDER BY total_instalaciones DESC";

        List<TechnicalStatic> statistics = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statistics.add(new TechnicalStatic(
                        rs.getString("staff_uuid"),
                        rs.getInt("total_instalaciones")
                ));
            }

            return statistics;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener estadísticas de los técnicos", e
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException(
                    "El UUID del técnico no es válido"
            );
        }
    }

    @Override
    public List<OrderContainer> getTechnicianLogsByClient(String clientDni) {
        List<OrderContainer> logs = new ArrayList<>();

        String sql = ORDER_REPORT_SELECT
                + "FROM orders o LEFT JOIN orders_report r ON r.order_id = o.order_id "
                + "WHERE o.order_client_dni = ? ORDER BY o.order_created DESC";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clientDni);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapOrderContainer(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener los registros del cliente", e);
        }

        return logs;
    }

    @Override
    public void acceptInstallation(
            String staffUuid,
            String orderId,
            String clientDni
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM tecnicos_logs WHERE staff_uuid = ? AND current_order = 1",
                        staffUuid)) {
                    throw new DatabaseException("El técnico ya tiene una instalación activa.");
                }

                if (SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM tecnicos_logs WHERE order_id = ? AND current_order = 1",
                        orderId)) {
                    throw new DatabaseException("Este pedido ya fue aceptado por otro técnico.");
                }

                SqliteUtil.update(
                        c,
                        "INSERT INTO tecnicos_logs (servicio_uuid, staff_uuid, client_dni, accion_tipo, "
                                + "order_id, description_tipo, current_order) VALUES (?, ?, ?, ?, ?, ?, 1)",
                        UUID.randomUUID().toString(), staffUuid, clientDni, ACTION,
                        orderId, "Instalación aceptada por el técnico"
                );
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo aceptar la instalación: " + e.getMessage()
            );
        }
    }

    @Override
    public Optional<TechnicalLogs> getActiveInstallation(String staffUuid) {

        String sql = "SELECT servicio_uuid, staff_uuid, client_dni, accion_tipo, order_id, "
                + "description_tipo, current_order FROM tecnicos_logs "
                + "WHERE staff_uuid = ? AND current_order = 1";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffUuid);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new TechnicalLogs(
                        rs.getString("servicio_uuid"),
                        rs.getString("staff_uuid"),
                        rs.getString("client_dni"),
                        rs.getString("accion_tipo"),
                        rs.getString("order_id"),
                        rs.getString("description_tipo"),
                        rs.getBoolean("current_order")
                ));
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener la instalación activa: " + e.getMessage()
            );
        }
    }

    @Override
    public void finishInstallation(
            String staffUuid,
            InstallationReport report
    ) {

        String reportSql = "INSERT INTO orders_report (order_id, report_client_dni, report_observation, "
                + "img_router, img_conexion, img_mini_antenna, router_password, report_create, "
                + "report_status, report_ended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'INSTALLED_ACTIVE', ?)";

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (!SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM tecnicos_logs WHERE staff_uuid = ? AND order_id = ? "
                                + "AND client_dni = ? AND accion_tipo = ? AND current_order = 1",
                        staffUuid, report.getOrderId(), report.getReportClientDni(), ACTION)) {
                    throw new DatabaseException("No tienes esta instalación asignada.");
                }

                if (!SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM orders WHERE order_id = ? AND order_client_dni = ?",
                        report.getOrderId(), report.getReportClientDni())) {
                    throw new DatabaseException("El pedido no corresponde al cliente.");
                }

                if (SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM orders_report WHERE order_id = ?",
                        report.getOrderId())) {
                    throw new DatabaseException("Este pedido ya tiene un reporte registrado.");
                }

                String now = SqliteUtil.now();

                try (PreparedStatement stmt = c.prepareStatement(reportSql)) {
                    stmt.setString(1, report.getOrderId());
                    stmt.setString(2, report.getReportClientDni());
                    SqliteUtil.setNullableString(stmt, 3, report.getReportObservation());
                    setImage(stmt, 4, report.getImgRouter());
                    setImage(stmt, 5, report.getImgConexion());
                    setImage(stmt, 6, report.getImgMiniAntenna());
                    stmt.setString(7, report.getRouterPassword());
                    stmt.setString(8, now);
                    stmt.setString(9, now);
                    stmt.executeUpdate();
                }

                int released = SqliteUtil.update(
                        c,
                        "UPDATE tecnicos_logs SET current_order = 0, description_tipo = 'Instalación finalizada' "
                                + "WHERE staff_uuid = ? AND order_id = ? AND current_order = 1",
                        staffUuid, report.getOrderId()
                );

                if (released != 1) {
                    throw new DatabaseException("No se pudo liberar la instalación.");
                }

                int activated = SqliteUtil.update(
                        c,
                        "UPDATE clients SET client_status = 'ACTIVO' "
                                + "WHERE client_dni = ? AND client_status = 'EN_INSTALACION'",
                        report.getReportClientDni()
                );

                if (activated != 1) {
                    throw new DatabaseException("No se pudo activar al cliente.");
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo finalizar la instalación: " + e.getMessage()
            );
        }
    }

    private void setImage(
            PreparedStatement stmt,
            int index,
            byte[] image
    ) throws SQLException {
        SqliteUtil.setNullableBytes(stmt, index, image);
    }

    @Override
    public boolean isInstallationInProgress(String orderId) {

        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM tecnicos_logs WHERE order_id = ? "
                + "AND current_order = 1 AND accion_tipo = '" + ACTION + "') THEN 1 ELSE 0 END AS in_progress";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("in_progress");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al verificar el estado de la instalación: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public void rejectInstallation(String staffUuid, String orderId) {

        String sql = "DELETE FROM tecnicos_logs WHERE staff_uuid = ? AND order_id = ? "
                + "AND accion_tipo = '" + ACTION + "' AND current_order = 1";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffUuid);
            stmt.setString(2, orderId);

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("No tienes esta instalación asignada.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo rechazar la instalación: " + e.getMessage()
            );
        }
    }

}
