package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.AttentionLog;
import org.example.servinet.application.port.out.sql.Sellerrepository;
import org.example.servinet.domain.entities.AttentionStatistics;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlSellerRepository implements Sellerrepository {

    private final DatabaseFactory databaseFactory;

    public SqlSellerRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }


    @Override
    public List<AttentionStatistics> getAttentionStatistics() {

        String sql = "{call dbo.sp_GetAttentionStatistics()}";

        List<AttentionStatistics> statistics = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                statistics.add(new AttentionStatistics(
                        rs.getString("staff_uuid"),
                        rs.getInt("total_atenciones"),
                        rs.getBigDecimal("promedio_valoracion"),
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

        String sql = "{call dbo.sp_RegisterAttentionLog(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setObject(1, log.staffUuid());

            stmt.setString(2, log.clientDni());
            stmt.setString(3, log.clientName());
            stmt.setString(4, log.clientLastName());
            stmt.setString(5, log.atencionTipo().name());
            stmt.setInt(6, log.valoracion());
            stmt.setString(7, log.descriptionTipo());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al registrar la atención", e
            );
        }
    }

}
