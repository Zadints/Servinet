package org.example.servinet.application.service;

import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.domain.exception.PaidException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public class PaymentsService {
    private final LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository;

    public PaymentsService(LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPaymentAuthRepository) {
        this.sqlPaymentAuthRepository = sqlPaymentAuthRepository;
    }


    public BigDecimal getAllCount(){
       return sqlPaymentAuthRepository.getAllMoney();
    }

    public List<Payment> getAll(String dni) {
        return sqlPaymentAuthRepository.get(dni);
    }

    public List<Payment> getAll() {
        return sqlPaymentAuthRepository.getAll();
    }


    /**
     * @param payment trae el pago ya construido para registrar, en caso
     *               está el pago registrado en sql retorna error .
     *               el pagoInmmemory n oaplcia proque se guarda dentro
     *                del clietnne la info
     **/
    public void savePaymentIfNoExist(Payment payment, Client client){

        if (payment.getCreateAt() == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria");
        }

        List<Payment> allPayments = client.getAllPayments();

        if (!allPayments.isEmpty()){
            boolean existPaid = allPayments.stream()
                    .filter(pay -> pay.getCreateAt() != null)
                    .anyMatch(pay ->
                            YearMonth.from(pay.getCreateAt())
                                    .equals(YearMonth.from(payment.getCreateAt()))
                    );

            if (!existPaid) {
                throw new PaidException("Error al crear un nuevo pago porque ya existe");
            }
        }

        client.addPaidPayment(payment);
        sqlPaymentAuthRepository.add(payment);
    }


}
