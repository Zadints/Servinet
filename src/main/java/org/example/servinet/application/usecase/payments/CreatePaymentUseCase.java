package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.port.out.InMemoryPaysRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LongDataRepository;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.IsPaid;
import org.example.servinet.domain.exception.PaidException;
import org.example.servinet.infrastructure.database.sqlserver.SqlPaymentsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreatePaymentUseCase {

    private final PaymentsService paymentsService;

    public CreatePaymentUseCase(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }
    
    public void createNewPay(Client client){
       paymentsService.createNewPay(client);
    }


}
