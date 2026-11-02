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

        System.out.println("========== REGISTRO DE PAGO ==========");
        System.out.println("ID del pago: " + payment.getId());
        System.out.println("Descripción: " + payment.getDescription());
        System.out.println("Monto: S/ " + payment.getAmount());
        System.out.println("Método de pago: " + paid.payMethod());
        System.out.println("Imagen recibida: " + paid.image().length + " bytes");
        System.out.println("Estado anterior: " + payment.isPaid());


        payment.setDatePaidUser(LocalDateTime.now());
        payment.setPaid(true);
        payment.setImage(paid.image());
        payment.setPayMethod(paid.payMethod());

        sqlPaymentAuthRepository.update(payment);

        System.out.println("Estado nuevo: " + payment.isPaid());
        System.out.println("Fecha de pago: " + payment.getDatePaidUser());
        System.out.println("========== PAGO REGISTRADO ==========");

    }
}
