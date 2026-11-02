package org.example.servinet.ui.controllers.components;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import org.example.servinet.application.usecase.clients.CallClientUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;

import java.util.function.Consumer;

public class SellClientController {
    public Label lblClientName;
    public Label lblDescription;
    public Label lblStatus;

    private Client client;
    private final CallClientUseCase callClientUseCase;

    public SellClientController(CallClientUseCase callClientUseCase) {
        this.callClientUseCase = callClientUseCase;
    }

    public void setClient(Client client) {
        this.client = client;
        lblClientName.setText(client.getFirstName() + " " + client.getLastName());
        lblDescription.setText(client.getPlan().toString());
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
