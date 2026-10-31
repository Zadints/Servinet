package org.example.servinet.infrastructure.database.sqlserver;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.PayMethods;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlPaymentsRepository implements LongDataRepository<Payment, List<Payment>, List<Payment>> {

    private final DatabaseFactory databaseFactory;

    public SqlPaymentsRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    /**
     * Obtiene los 10 ùltimos pagos realizados por el usuario, de ahi va aumentando de 10 en 10
     **/

    @Override
    public List<Payment> getPage(String uuid, int offset, int pageSize) {
        String sql = "{call dbo.sp_GetPaymentsByClientPage(?, ?, ?)}";

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio.");
        }

        List<Payment> payments = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, uuid);
                stmt.setInt(2, offset);
                stmt.setInt(3, pageSize);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        payments.add(mapPayment(rs));
                    }
                }
            }

            return payments;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los pagos del cliente", e
            );
        }
    }

    /**
     * crea un pago pendiente del usuario
     **/
    @Override
    public void add(Payment entity) {
        String sql = "{call dbo.sp_CreatePendingPayment(?, ?, ?, ?, ?)}";

        if (entity == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, entity.getClientDni());
                stmt.setBigDecimal(2, entity.getAmount());
                stmt.setString(3, entity.getDescription());
                stmt.setString(4, entity.getReference());
                stmt.setString(5, entity.getId());

                stmt.execute();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo crear el pago pendiente", e
            );
        }
    }

    /**
     * Obtiene todos los pagos de un ciente a si sea 100 (ideal para el módulo de mostrar todo)
     **/
    @Override
    public List<Payment> get(String id) {
        String sql = "{call dbo.sp_GetPaymentsByClient(?)}";

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio.");
        }

        List<Payment> payments = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        payments.add(mapPayment(rs));
                    }
                }
            }

            return payments;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener todos los pagos del cliente", e
            );
        }
    }
    /**
     * Elimina el pago realizado solo si el usuario no existe proque el pago realizado
     * no depende de Cliente (sirve para sacar ganancias)
     **/

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del pago es obligatorio.");
        }

        String sql = "{call dbo.sp_DeletePayment(?)}";

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, id);
                stmt.execute();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el pago", e
            );
        }
    }
    /**
     * Actualiza cuando un usuario ya pagó . la entidad
     * Payment trae el pago actualizado con el mism oID seria
     * buscar y realizar el pago.
     **/
    @Override
    public void update(Payment entity) {
        String sql = "{call dbo.sp_CompletePayment(?, ?, ?)}";

        if (entity == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }


        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, entity.getId());

                if (entity.getPayMethod() != null) {
                    stmt.setString(2, entity.getPayMethod().name());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }

                if (entity.getImage() != null) {
                    stmt.setBytes(3, entity.getImage());
                } else {
                    stmt.setNull(3, Types.VARBINARY);
                }

                stmt.execute();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo completar el pago", e
            );
        }
    }


    private Payment mapPayment(ResultSet rs) throws SQLException {
        Timestamp timestampPaid = rs.getTimestamp("payment_date_paid");
        Timestamp timestampCreated = rs.getTimestamp("payment_created_at");

        LocalDateTime datePaid = timestampPaid != null
                ? timestampPaid.toLocalDateTime()
                : null;

        LocalDateTime dateCreateAt = timestampCreated != null
                ? timestampCreated.toLocalDateTime()
                : null;

        String method = rs.getString("payment_method");
        byte[] image = rs.getBytes("payment_image");

        return new Payment(
                rs.getString("payment_id"),
                rs.getString("client_dni"),
                rs.getBigDecimal("payment_amount"),
                datePaid,
                dateCreateAt,
                method != null ? PayMethods.valueOf(method) : null,
                rs.getBoolean("payment_ispaid"),
                image,
                rs.getString("payment_description"),
                rs.getString("payment_reference")
        );
    }
}
