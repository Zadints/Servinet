package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.dto.PaidPaymentDto;
import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.exception.PaidException;

import java.time.LocalDateTime;
import java.util.List;

public class PaidPaymentUseCase {
    private final LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository;
    private final InMemoryPaysRepository inMemoryPayRepository;

    public PaidPaymentUseCase(LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository, InMemoryPaysRepository inMemoryPayRepository) {
        this.sqlPaymentAuthRepository = sqlPaymentAuthRepository;
        this.inMemoryPayRepository = inMemoryPayRepository;
    }

    public void paidPayment(Payment payment, PaidPaymentDto paid){

        if (paid.image() == null){
            throw  new PaidException("Debes ingresar una imagen para registrar el pago.");
        }

        payment.setDatePaidUser(LocalDateTime.now());
        payment.setPaid(true);
        payment.setImage(paid.image());
        payment.setPayMethod(paid.payMethod());

    }
}
