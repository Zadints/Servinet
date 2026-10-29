package org.example.servinet.application.port.out.sql;
import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;

import java.util.List;

public interface ClientRepository {

    List<OrderReportDto> getAllClientsOrderDatabase();

    void setOrderDatabase(Client client, String whatsAppNum);

    List<ClientPlan> getAllPlansDatabase();

    void updateClientDatabase(Client client, String whatsApp);

    void deleteClientDatabase(Client client);
}
