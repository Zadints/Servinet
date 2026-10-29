package org.example.servinet.application.service;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.IsPaid;
import org.example.servinet.domain.exception.PaidException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentsService {
    private final LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository;
    private final InMemoryPaysRepository inMemoryPayRepository;
    private boolean isOneRecursive;

    public PaymentsService(LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository, InMemoryPaysRepository inMemoryPayRepository) {
        this.sqlPaymentAuthRepository = sqlPaymentAuthRepository;
        this.inMemoryPayRepository = inMemoryPayRepository;
        isOneRecursive = false;
    }

    public PaymentsScore getAll(String dni) {

        PaymentsScore payments = inMemoryPayRepository.getPayScore(dni);
        if (payments!=null) {
            return payments;
        }

        List<Payment> pays = sqlPaymentAuthRepository.getPage(dni, 0, 10);

        if (pays == null){
            return null;
        }

        inMemoryPayRepository.addPayScore(loadPaidUser(pays,dni));

        return inMemoryPayRepository.getPayScore(dni);
    }

    public static PaymentsScore loadPaidUser(List<Payment> payments, String dni) {

        List<Payment> paidPayments = payments.stream()
                .filter(Payment::isPaid)
                .toList();

        List<Payment> pendingPayments = payments.stream()
                .filter(payment -> !payment.isPaid())
                .toList();

        return new PaymentsScore(dni ,pendingPayments, paidPayments);
    }

    public void createNewPay(Client client){

        IsPaid isPaid = inMemoryPayRepository.isExistPayMont(client.getDni());

        switch (isPaid){
            case IsPaid.FALSE -> {
                createPay(client);
            }
            case IsPaid.TRUE -> {
                throw new PaidException("No peudes crear un pago doble en el mismo mes.");
            }
            case IsPaid.NO_LOAD -> {

                if (!isOneRecursive) return;

                List<Payment> pays = sqlPaymentAuthRepository.getPage(client.getDni(), 0, 10);

                if (pays == null){
                    createPay(client);
                }
                isOneRecursive = true;
                createNewPay(client);
            }
        }
    }

    private void createPay(Client client){
        String mesActual = LocalDate.now().getMonth().toString();

        var pay = new Payment(
                UUID.randomUUID().toString(),
                client.getDni(),
                BigDecimal.valueOf(client.getPlan().getPrice()),
                null,
                LocalDateTime.now(),
                null,
                null,
                null,
                "Recibo de pago del mes de " +  mesActual,
                "Pago de servicio"
        );

        sqlPaymentAuthRepository.add(pay);
        inMemoryPayRepository.addPaidPayment(pay);
    }
}
