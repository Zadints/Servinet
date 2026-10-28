package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.domain.entities.Payment;

import java.util.List;

public class GetPaymentUseCase {
    private final LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository;
    private final InMemoryPaysRepository inMemoryPayRepository;
    public GetPaymentUseCase(LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository, InMemoryPaysRepository inMemoryPayRepository) {
        this.sqlPaymentAuthRepository = sqlPaymentAuthRepository;
        this.inMemoryPayRepository = inMemoryPayRepository;
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
}
