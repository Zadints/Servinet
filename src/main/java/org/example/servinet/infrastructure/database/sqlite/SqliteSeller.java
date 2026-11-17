package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.dto.AttentionLog;
import org.example.servinet.application.port.out.sql.SqlSellerRepository;
import org.example.servinet.domain.entities.AttentionStatistics;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteSeller implements SqlSellerRepository {

    private final DatabaseFactory databaseFactory;

    public SqliteSeller(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public List<AttentionStatistics> getAttentionStatistics() {

        String sql = "SELECT staff_uuid, "
                + "COUNT(*) AS total_atenciones, "
                + "ROUND(AVG(CAST(valoracion AS REAL)), 2) AS promedio_valoracion, "
                + "SUM(CASE WHEN atencion_tipo = 'VENTA' THEN 1 ELSE 0 END) AS total_ventas, "
                + "SUM(CASE WHEN atencion_tipo = 'INFORME' THEN 1 ELSE 0 END) AS total_informes, "
                + "SUM(CASE WHEN atencion_tipo = 'SOPORTE' THEN 1 ELSE 0 END) AS total_soportes, "
                + "SUM(CASE WHEN atencion_tipo = 'SUSPENCION' THEN 1 ELSE 0 END) AS total_suspensiones, "
                + "SUM(CASE WHEN atencion_tipo = 'COBRO' THEN 1 ELSE 0 END) AS total_cobros, "
                + "SUM(CASE WHEN atencion_tipo = 'ATENCION_CLIENTE' THEN 1 ELSE 0 END) AS total_atenciones_cliente "
                + "FROM atencion_logs GROUP BY staff_uuid ORDER BY total_atenciones DESC";

        List<AttentionStatistics> statistics = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                statistics.add(new AttentionStatistics(
                        rs.getString("staff_uuid"),
                        rs.getInt("total_atenciones"),
                        SqliteUtil.getDecimal(rs, "promedio_valoracion"),
                        rs.getInt("total_ventas"),
                        rs.getInt("total_informes"),
                        rs.getInt("total_soportes"),
                        rs.getInt("total_suspensiones"),
                        rs.getInt("total_cobros"),
                        rs.getInt("total_atenciones_cliente")
                ));
            }

            return statistics;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener estadísticas de atención", e
            );
        }
    }

    @Override
    public void registerAttentionLog(AttentionLog log) {

        String sql = "INSERT INTO atencion_logs (atencion_uuid, staff_uuid, client_dni, client_name, "
                + "client_last_name, atencion_tipo, valoracion, description_tipo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, String.valueOf(log.staffUuid()));
            SqliteUtil.setNullableString(stmt, 3, log.clientDni());
            stmt.setString(4, log.clientName());
            stmt.setString(5, log.clientLastName());
            stmt.setString(6, log.atencionTipo().name());
            stmt.setInt(7, log.valoracion());
            SqliteUtil.setNullableString(stmt, 8, log.descriptionTipo());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al registrar la atención", e
            );
        }
    }

}
