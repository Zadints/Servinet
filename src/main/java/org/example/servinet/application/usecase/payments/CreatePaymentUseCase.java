package org.example.servinet.application.usecase.payments;

import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.client.Client;

public class CreatePaymentUseCase {

    private final PaymentsService paymentsService;

    public CreatePaymentUseCase(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    /*
    public void createNewPay(Client client){
       paymentsService.savePaymentIfNoExist(client);
    }

*/
}
