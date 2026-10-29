package org.example.servinet.application.port.out;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.IsPaid;

import java.time.Month;
import java.util.List;

public interface InMemoryPaysRepository {

    PaymentsScore getPayScore(String dni);
    void addPayScore(PaymentsScore entity);

    void addNewPayment(Payment newPaid);
    void addPaidPayment(Payment newPaid);
    List<Payment> getPendingPays(String dni);
    List<Payment> getPaidPays(String dni);
    List<Payment> getAllPays(String dni);


    IsPaid isExistPayMont(String dni);
}
