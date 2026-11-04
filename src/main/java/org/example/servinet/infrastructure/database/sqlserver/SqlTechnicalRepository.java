package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.port.out.sql.TechnicalRepository;
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
                                        OrderStatus.valueOf(rs.getString("report_status")),
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
}
