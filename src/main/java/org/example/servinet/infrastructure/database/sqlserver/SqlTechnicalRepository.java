package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.port.out.sql.TechnicalRepository;
import org.example.servinet.domain.entities.InstallationReport;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.entities.client.OrderReport;
import org.example.servinet.domain.entities.technicians.TechnicalLogs;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.TechnicalStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqlTechnicalRepository implements TechnicalRepository {

    private final DatabaseFactory databaseFactory;

    public SqlTechnicalRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }


    @Override
    public List<OrderContainer> getTechnicianLogs(String staffUuid) {

        String sql = "{call dbo.sp_GetTechnicianLogs(?)}";
        List<OrderContainer> orders = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setObject(1, staffUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Timestamp orderCreated = rs.getTimestamp("order_created");
                    Timestamp reportCreate = rs.getTimestamp("report_create");
                    Timestamp reportEnded = rs.getTimestamp("report_ended");

                    String status = rs.getString("report_status");

                    OrderStatus reportStatus = status != null
                            ? OrderStatus.valueOf(status)
                            : OrderStatus.PENDING;

                    orders.add(
                            new OrderContainer(
                                new OrderReport(
                                        rs.getString("report_client_dni"),
                                        rs.getString("report_observation"),
                                        rs.getBytes("img_router"),
                                        rs.getBytes("img_conexion"),
                                        rs.getBytes("img_mini_antenna"),
                                        rs.getString("router_password"),
                                        reportCreate != null ? reportCreate.toLocalDateTime() : null,
                                        reportStatus,
                                        reportEnded != null ? reportEnded.toLocalDateTime() : null
                                ),
                                new Order(
                                        rs.getString("order_id"),
                                        rs.getString("order_antenna_connect"),
                                        rs.getString("order_plan_connect"),
                                        orderCreated != null ? orderCreated.toLocalDateTime() : null,
                                        OrderStatus.valueOf(rs.getString("order_status"))
                                )
                    ));
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
        String sql = "{call dbo.sp_GetTechnicianStatistics()}";

        List<TechnicalStatic> statistics = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
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

        String sql = "{CALL dbo.sp_GetTechnicianLogsByClient(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, clientDni);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp orderCreated = rs.getTimestamp("order_created");
                    Timestamp reportCreate = rs.getTimestamp("report_create");
                    Timestamp reportEnded = rs.getTimestamp("report_ended");
                    String status = rs.getString("report_status");
                    OrderStatus reportStatus = status != null
                            ? OrderStatus.valueOf(status)
                            : OrderStatus.PENDING;

                    OrderContainer log = new OrderContainer(
                            new OrderReport(
                                    rs.getString("report_client_dni"),
                                    rs.getString("report_observation"),
                                    rs.getBytes("img_router"),
                                    rs.getBytes("img_conexion"),
                                    rs.getBytes("img_mini_antenna"),
                                    rs.getString("router_password"),
                                    reportCreate != null ? reportCreate.toLocalDateTime() : null,
                                    reportStatus,
                                    reportEnded != null ? reportEnded.toLocalDateTime() : null
                            ),
                            new Order(
                                    rs.getString("order_id"),
                                    rs.getString("order_antenna_connect"),
                                    rs.getString("order_plan_connect"),
                                    orderCreated != null ? orderCreated.toLocalDateTime() : null,
                                    OrderStatus.valueOf(rs.getString("order_status"))
                            ));

                    logs.add(log);
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

        String sql = "{call dbo.sp_AcceptInstallation(?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, staffUuid);
            stmt.setString(2, orderId);
            stmt.setString(3, clientDni);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo aceptar la instalación: " + e.getMessage()
            );
        }
    }

    @Override
    public Optional<TechnicalLogs> getActiveInstallation( String staffUuid) {

        String sql = "{call dbo.sp_GetActiveInstallation(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

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

        String sql = "{call dbo.sp_FinishInstallation(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, staffUuid);
            stmt.setString(2, report.getOrderId());
            stmt.setString(3, report.getReportClientDni());
            stmt.setString(4, report.getReportObservation());

            setImage(stmt, 5, report.getImgRouter());
            setImage(stmt, 6, report.getImgConexion());
            setImage(stmt, 7, report.getImgMiniAntenna());

            stmt.setString(8, report.getRouterPassword());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo finalizar la instalación: " + e.getMessage()
            );
        }
    }

    private void setImage(
            CallableStatement stmt,
            int index,
            byte[] image
    ) throws SQLException {

        if (image == null) {
            stmt.setNull(index, java.sql.Types.VARBINARY);
            return;
        }

        stmt.setBytes(index, image);
    }


    @Override
    public boolean isInstallationInProgress(String orderId) {

        String sql = "{call dbo.sp_IsInstallationInProgress(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

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

        String sql = "{call dbo.sp_RejectInstallation(?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, staffUuid);
            stmt.setString(2, orderId);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo rechazar la instalación: " + e.getMessage()
            );
        }
    }

}
