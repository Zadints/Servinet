package org.example.servinet.ui.controllers.components;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.TechnicalUseCase;
import org.example.servinet.application.usecase.clients.CallClientUseCase;
import org.example.servinet.application.usecase.payments.GetPaymentUseCase;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;

import java.util.List;

public class SellClientController {
    public Label lblClientName;
    public Label lblDescription;
    public Label lblStatus;
    public Button btnInfo;

    private ClientPlan plan;
    private Client client;
    private final CallClientUseCase callClientUseCase;
    private final PlanUseCase planUseCase;
    private final TechnicalUseCase technicalUseCase;

    public SellClientController(CallClientUseCase callClientUseCase, PlanUseCase planUseCase, TechnicalUseCase technicalUseCase) {
        this.callClientUseCase = callClientUseCase;
        this.planUseCase = planUseCase;
        this.technicalUseCase = technicalUseCase;
    }

    public void setClient(Client client) {
        this.client = client;
        lblClientName.setText(client.getFirstName() + " " + client.getLastName());

        plan = planUseCase.getPlan(client.getOrder().getPlanUuid());

        lblDescription.setText( "PLAN " + plan.getName() + " • Internet " + plan.getSpeed() + "Mbps");
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


    public void infoGive() {

        List<OrderContainer> orders = technicalUseCase.getTechnicianLogsByClient(client.getDni());
        String password = "No se completó la instalación";
        for (OrderContainer o : orders){
            if (o.orderReport().getRouterPassword() != null &&
                    !o.orderReport().getRouterPassword().isEmpty()){
                 password = o.orderReport().getRouterPassword();
            }
        }


        String info = "Nombre: " + client.getFullName() + "\n"
                + "DNI: " + (client.getDni() != null ? client.getDni() : "--") + "\n"
                + "Correo: " + (client.getEmail() != null ? client.getEmail() : "--") + "\n"
                + "Dirección: " + (client.getAddress() != null ? client.getAddress() : "--") + "\n"
                + "Plan: " + plan + "\n"
                + "Fecha de creación: " + client.getClientRegister() + "\n"
                + "Estado: " + client.getClientStatus() + "\n"
                + "Contraseña router: " + password + "\n"
                + "Pago: " + (client.getClientStatus() == ClientStatus.ACTIVO ? "Al día" : "Con deuda");

        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, ButtonType.OK);
        alert.setHeaderText("Información del cliente");
        alert.showAndWait();
    }


}
