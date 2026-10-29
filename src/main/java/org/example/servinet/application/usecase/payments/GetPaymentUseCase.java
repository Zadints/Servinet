package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.Payment;

import java.util.List;

public class GetPaymentUseCase {
    private final PaymentsService paymentsService;

    public GetPaymentUseCase(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    public PaymentsScore getAll(String dni) {
        return paymentsService.getAll(dni);
    }
}
