package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.IsPaid;
import org.example.servinet.domain.exception.PaidException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InMemoryPayRepository implements InMemoryPaysRepository{

    private Map<String,PaymentsScore> paymentsScore;


    @Override
    public PaymentsScore getPayScore(String dni) {
        return paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), dni))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addPayScore(PaymentsScore entity) {
        paymentsScore.put(entity.getDni(), entity);
    }

    @Override
    public void addNewPayment(Payment newPaid) {
        PaymentsScore pay = paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), newPaid.getClientDni()))
                .findFirst()
                .orElse(null);

        assert pay != null;
        pay.addNewPayment(newPaid);
    }

    @Override
    public void addPaidPayment(Payment newPaid) {


        PaymentsScore pay = paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), newPaid.getClientDni()))
                .findFirst()
                .orElse(null);

        assert pay != null;
        boolean added = pay.addPaidPayment(newPaid);

        if (!added) throw new PaidException("El pago ingresado no existe en la lista para pagar");
    }

    @Override
    public List<Payment> getPendingPays(String dni) {
        PaymentsScore pending =  paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), dni))
                .findFirst()
                .orElse(null);

        assert pending != null;
        return pending.getPendingPayments();
    }

    @Override
    public List<Payment> getPaidPays(String dni) {
        PaymentsScore pending =  paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), dni))
                .findFirst()
                .orElse(null);

        assert pending != null;
        return pending.getPaidPayments();
    }

    @Override
    public List<Payment> getAllPays(String dni) {
        PaymentsScore pending =  paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), dni))
                .findFirst()
                .orElse(null);

        assert pending != null;
        return pending.getAllPayments();
    }
    @Override
    public IsPaid isExistPayMont(String dni) {

        PaymentsScore paymentScore = paymentsScore.values().stream()
                .filter(payment -> Objects.equals(payment.getDni(), dni))
                .findFirst()
                .orElse(null);

        if (paymentScore == null) {
            return IsPaid.NO_LOAD;
        }

        List<Payment> pays = paymentScore.getAllPayments();

        if (pays == null || pays.isEmpty()) {
            return IsPaid.NO_LOAD;
        }

        LocalDateTime lastPayment = pays.stream()
                .map(Payment::getCreateAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new PaidException(
                        "No se encontraron pagos con fechas de creación válidas"
                ));

        LocalDateTime limit = LocalDateTime.now().minusDays(30);

        // FALSE: puede crear un nuevo pago mensual.
        // TRUE: todavía no puede crearlo.
        return !lastPayment.isAfter(limit)
                ? IsPaid.FALSE
                : IsPaid.TRUE;
    }
}
