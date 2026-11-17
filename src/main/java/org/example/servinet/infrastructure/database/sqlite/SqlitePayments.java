package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlLongDataRepository;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.domain.enums.PayMethods;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlitePayments implements SqlLongDataRepository<Payment, List<Payment>, List<Payment>> {

    private static final String PAYMENT_COLUMNS =
            "payment_id, client_dni, payment_amount, payment_date_paid, payment_created_at, "
                    + "payment_method, payment_ispaid, payment_image, payment_description, payment_reference";

    private final DatabaseFactory databaseFactory;

    public SqlitePayments(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public List<Payment> getPage(String uuid, int offset, int pageSize) {
        String sql = "SELECT payment_id, client_dni, payment_amount, payment_created_at, payment_date_paid, "
                + "payment_method, payment_ispaid, NULL AS payment_image, payment_description, payment_reference "
                + "FROM payments WHERE client_dni = ? "
                + "ORDER BY payment_created_at DESC, payment_id DESC LIMIT ? OFFSET ?";

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio.");
        }

        if (offset < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Los parámetros de paginación no son válidos.");
        }

        List<Payment> payments = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid);
                stmt.setInt(2, pageSize);
                stmt.setInt(3, offset);

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

    @Override
    public void add(Payment entity) {
        String sql = "INSERT INTO payments (payment_id, client_dni, payment_amount, payment_description, "
                + "payment_reference, payment_ispaid, payment_created_at) VALUES (?, ?, ?, ?, ?, 0, ?)";

        if (entity == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.getId());
                stmt.setString(2, entity.getClientDni());
                SqliteUtil.setDecimal(stmt, 3, entity.getAmount());
                SqliteUtil.setNullableString(stmt, 4, entity.getDescription());
                SqliteUtil.setNullableString(stmt, 5, entity.getReference());
                stmt.setString(6, SqliteUtil.now());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo crear el pago pendiente", e
            );
        }
    }

    @Override
    public List<Payment> get(String dni) {
        String sql = "SELECT " + PAYMENT_COLUMNS + " FROM payments WHERE client_dni = ? "
                + "ORDER BY payment_created_at DESC";

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio.");
        }

        List<Payment> payments = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, dni);

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

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del pago es obligatorio.");
        }

        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);

                if (stmt.executeUpdate() == 0) {
                    throw new DatabaseException("El pago no existe.");
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el pago", e
            );
        }
    }

    @Override
    public void update(Payment entity) {
        String sql = "UPDATE payments SET payment_date_paid = ?, payment_method = ?, "
                + "payment_image = ?, payment_ispaid = 1 WHERE payment_id = ?";

        if (entity == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }

        try (Connection conn = databaseFactory.getConnection()) {
            if (conn == null) {
                throw new DatabaseException(
                        "No hay conexión disponible con la base de datos", null
                );
            }

            SqliteUtil.runInTransaction(conn, c -> {

                if (!SqliteUtil.exists(c, "SELECT 1 FROM payments WHERE payment_id = ?", entity.getId())) {
                    throw new DatabaseException("El pago no existe.");
                }

                if (SqliteUtil.exists(
                        c,
                        "SELECT 1 FROM payments WHERE payment_id = ? AND payment_ispaid = 1",
                        entity.getId())) {
                    throw new DatabaseException("Este pago ya fue registrado como pagado.");
                }

                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    stmt.setString(1, SqliteUtil.now());

                    if (entity.getPayMethod() != null) {
                        stmt.setString(2, entity.getPayMethod().name());
                    } else {
                        SqliteUtil.setNullableString(stmt, 2, null);
                    }

                    SqliteUtil.setNullableBytes(stmt, 3, entity.getImage());
                    stmt.setString(4, entity.getId());

                    stmt.executeUpdate();
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo completar el pago", e
            );
        }
    }

    @Override
    public BigDecimal getAllMoney() {

        String sql = "SELECT COALESCE(SUM(payment_amount), 0) AS total_money FROM payments";

        try (
                Connection conn = databaseFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

            if (rs.next()) {
                return SqliteUtil.getDecimal(rs, "total_money");
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {

            throw new DatabaseException(
                    "Error al obtener el dinero total de los pagos",
                    e
            );
        }
    }

    @Override
    public List<Payment> getAll() {

        String sql = "SELECT " + PAYMENT_COLUMNS + " FROM payments ORDER BY payment_created_at DESC";

        List<Payment> payments = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapPayment(rs));
            }

            return payments;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener todos los pagos",
                    e
            );
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        LocalDateTime datePaid = SqliteUtil.getDateTime(rs, "payment_date_paid");
        LocalDateTime dateCreateAt = SqliteUtil.getDateTime(rs, "payment_created_at");

        String method = rs.getString("payment_method");
        byte[] image = rs.getBytes("payment_image");

        return new Payment(
                rs.getString("payment_id"),
                rs.getString("client_dni"),
                SqliteUtil.getDecimal(rs, "payment_amount"),
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
