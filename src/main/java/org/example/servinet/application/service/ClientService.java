package org.example.servinet.application.service;

import javafx.concurrent.Task;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.domain.enums.ClientStatus;

import java.time.LocalDateTime;
import java.util.*;

public class ClientService {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AppExecutor appExecutor;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;

    public ClientService(InMemoryClientRepository inMemoryClientRepository, AppExecutor appExecutor, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.appExecutor = appExecutor;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
    }

    public boolean isClientExist(String dni){

        List<Client> cachedClients = inMemoryClientRepository.getAllClients();

        if (cachedClients == null || cachedClients.isEmpty()) {
            cachedClients = getAll();
        }

        if (cachedClients == null || cachedClients.isEmpty()) return false;

        return inMemoryClientRepository.getClient(dni) != null;
    }

    //retorna cliente entero

    public List<Client> getAll() {

        List<Client> cachedClients = inMemoryClientRepository.getAllClients();

        if (cachedClients != null && !cachedClients.isEmpty()) {
            return cachedClients;
        }

        List<Client> clients = sqlClientAuthRepository.getClientsWithOrders();

        if (clients == null){
            return new ArrayList<>();
        }

        for (Client c : clients) {

            List<Payment> payments = paymentsService.getAll(c.getDni());

            if (payments != null){
                for (Payment p : payments){
                    if (!p.isPaid()) {

                        LocalDateTime createdAt = p.getCreateAt();
                        LocalDateTime datePaidUser = p.getDatePaidUser();

                        if (createdAt != null && datePaidUser != null) {

                            boolean exceededThreeDays =
                                    datePaidUser.isAfter(createdAt.plusDays(3));

                            if (exceededThreeDays) {
                                c.setClientStatus(ClientStatus.SUSPENDIDO);
                                c.addPendingPayment(p);
                                Task<Void> task = new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        sqlClientAuthRepository.updateClientDatabase(new Client(
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                ClientStatus.SUSPENDIDO,
                                                null,
                                                null,
                                                null,
                                                null
                                        ));
                                        return null;
                                    }
                                };
                                appExecutor.execute(task);
                                continue;
                            }
                        }
                    }

                    c.addPaidPayment(p);
                }
            }

            inMemoryClientRepository.addClient(c);
        }

        return inMemoryClientRepository.getAllClients();
    }



}