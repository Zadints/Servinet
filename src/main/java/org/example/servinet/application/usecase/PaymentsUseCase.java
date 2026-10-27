package org.example.servinet.application.usecase;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.infrastructure.database.sqlserver.PaymentsModel;

import java.math.BigDecimal;
import java.util.List;

public class PaymentsUseCase {

    public static PaymentsScore loadPaidsUser(String dni) {

        List<Payment> payments = PaymentsModel.getPaymentsByClientDatabase(dni);

        List<Payment> paidPayments = payments.stream()
                .filter(Payment::isPaid)
                .toList();

        List<Payment> pendingPayments = payments.stream()
                .filter(payment -> !payment.isPaid())
                .toList();

        Payment altualPaid = payments.isEmpty()
                ? null
                : payments.getFirst();

        return new PaymentsScore(pendingPayments, paidPayments,altualPaid );
    }

    public static Payment generateNewPaidsUser(String dni, BigDecimal mount, String mes) {

        Payment newPayment = new Payment(
            new GenerateIdUseCase(new UuidGenerator()).toString(),
            dni,
            mount,
            null,
            null,
            null,
            null,
            "Recibo de pago del mes de " + mes,
            "Pago de servicio"
        );

        PaymentsModel.createPendingPaymentDatabase(newPayment);
        return newPayment;
    }

}
