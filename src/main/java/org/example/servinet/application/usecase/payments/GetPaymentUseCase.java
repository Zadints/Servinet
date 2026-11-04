package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.client.Payment;

import java.math.BigDecimal;
import java.util.List;

public class GetPaymentUseCase {
    private final PaymentsService paymentsService;

    public GetPaymentUseCase(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    public List<Payment> getAll(String dni) {
        return paymentsService.getAll(dni);
    }
    public BigDecimal getAllCount() {
        return paymentsService.getAllCount();
    }
    public List<Payment> getAll() {
        return paymentsService.getAll();
    }
}
