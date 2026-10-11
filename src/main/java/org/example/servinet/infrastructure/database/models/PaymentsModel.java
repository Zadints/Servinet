package org.example.servinet.infrastructure.database.models;
import org.example.servinet.core.domain.entities.Payment;
import org.example.servinet.core.domain.enums.PayMethods;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentsModel {

    public static void createPendingPaymentDatabase(Payment payment) {

        String sql = """
        EXEC dbo.sp_CreatePendingPayment
            @client_dni = ?,
            @payment_amount = ?,
            @payment_description = ?,
            @payment_reference = ?,
            @payment_id = ?
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos",
                    null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, payment.getClientDni());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getDescription());
            stmt.setString(4, payment.getReference());
            stmt.setString(5, payment.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo crear el pago pendiente",
                    e
            );
        }
    }
    public static Payment completePaymentDatabase(
            String paymentId,
            String paymentMethod,
            byte[] paymentImage) {

        String sql = """
        EXEC dbo.sp_CompletePayment
            @payment_id = ?,
            @payment_method = ?,
            @payment_image = ?;
        """;

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos",
                    null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentId);
            stmt.setString(2, paymentMethod);
            stmt.setBytes(3, paymentImage);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Timestamp timestamp = rs.getTimestamp("payment_date_paid");

                    LocalDateTime datePaid = timestamp != null
                            ? timestamp.toLocalDateTime()
                            : null;

                    Payment payment = new Payment(
                            rs.getString("payment_id"),
                            rs.getString("client_dni"),
                            rs.getBigDecimal("payment_amount"),
                            datePaid,
                            PayMethods.valueOf(rs.getString("payment_method")),
                            rs.getBoolean("payment_ispaid"),
                            ImageConverter.toImage(rs.getBytes("payment_image")),
                            rs.getString("payment_description"),
                            rs.getString("payment_reference")
                    );

                    return payment;
                }

                throw new DatabaseException(
                        "No se recibió el pago actualizado",
                        null
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo completar el pago",
                    e
            );
        }
    }

    public static List<Payment> getPaymentsByClientDatabase(String dni) {

        String sql = """
        EXEC dbo.sp_GetPaymentsByClient
            @client_dni = ?;
        """;

        List<Payment> payments = new ArrayList<>();

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No hay conexión disponible con la base de datos",
                    null
            );
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Timestamp timestamp = rs.getTimestamp("payment_date_paid");

                    LocalDateTime datePaid = timestamp != null
                            ? timestamp.toLocalDateTime()
                            : null;


                    Payment payment = new Payment(
                            rs.getString("payment_id"),
                            rs.getString("client_dni"),
                            rs.getBigDecimal("payment_amount"),
                            datePaid,
                            PayMethods.valueOf(rs.getString("payment_method")),
                            rs.getBoolean("payment_ispaid"),
                            ImageConverter.toImage(rs.getBytes("payment_image")),
                            rs.getString("payment_description"),
                            rs.getString("payment_reference")

                    );

                    payments.add(payment);
                }
            }

            return payments;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los pagos del cliente",
                    e
            );
        }
    }
}
