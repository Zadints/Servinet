package org.example.servinet.ui.controllers.components;

import javafx.scene.control.Label;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.clients.CallClientUseCase;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;

public class SellClientController {
    public Label lblClientName;
    public Label lblDescription;
    public Label lblStatus;

    private Client client;
    private final CallClientUseCase callClientUseCase;
    private final PlanUseCase planUseCase;

    public SellClientController(CallClientUseCase callClientUseCase, PlanUseCase planUseCase) {
        this.callClientUseCase = callClientUseCase;
        this.planUseCase = planUseCase;
    }

    public void setClient(Client client) {
        this.client = client;
        lblClientName.setText(client.getFirstName() + " " + client.getLastName());

        ClientPlan plan = planUseCase.getPlan(client.getOrder().getPlanUuid());

        lblDescription.setText(plan.getName() + " " + plan.getSpeed());
        lblStatus.setText(client.getClientStatus() + "");
    }

    public void copiPhone() {
        try {
            System.out.println("Enviando msg");
            callClientUseCase.sendCall(client);
        }catch (Exception e){
            System.out.println(e.getMessage() + e.getCause());
        }
    }


}
