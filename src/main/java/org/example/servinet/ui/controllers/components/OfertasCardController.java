package org.example.servinet.ui.controllers.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.ClientPlan;

import java.util.function.Consumer;

public class OfertasCardController {
    @FXML private Button btnOfertUse;
    @FXML private Label lblPrice;
    @FXML private Label lblSlogan;
    @FXML private Label lblDescription;
    @FXML private Label lblName;
    private Consumer<ClientPlan> onPlanSelected;
    private ClientPlan plan;

    @FXML
    public void setPlanOfert(ClientPlan plan){

        this.plan = plan;
        lblPrice.setText(" S/. " + plan.getPrice());
        lblName.setText(plan.getName());
        lblSlogan.setText("");
        lblDescription.setText(plan.getSpeed() + "Mbps - Instalación incluida");
    }

    @FXML private void initialize() {
        btnOfertUse.setOnMouseClicked(event -> {
            if (onPlanSelected != null && plan != null) {
                onPlanSelected.accept(plan);
            }
        });
    }

    public void selectClientPlan(Consumer<ClientPlan> callback) {
        this.onPlanSelected = callback;
    }

}
