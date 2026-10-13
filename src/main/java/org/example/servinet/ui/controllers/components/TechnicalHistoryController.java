package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.core.domain.entities.technicians.Order;
import org.example.servinet.core.domain.entities.technicians.Technical;


public class TechnicalHistoryController {

    @FXML private Label lblOrderId;
    @FXML private Label lblOrderTimeEnd;
    @FXML private Label lblOrderStatus;
    @FXML private Label lblClientName;
    @FXML private Label lblClientPlan;
    @FXML private Label lblClientAddress;
    private Order orderData;

    public void setTechnicalHistory(Order o){
        lblOrderId.setText(o.getOrderId());
        lblOrderTimeEnd.setText(
                String.valueOf(
                        o.getOrderEndInstall() != null
                                ? o.getOrderEndInstall()
                                : "No finalizado"
                )
        );
        lblOrderStatus.setText(o.getOrderStatus());
        lblClientName.setText(o.getClientName());
        lblClientPlan.setText(o.getPlanName());
        lblClientAddress.setText(o.getClientAddress());
    }
    public Order getTechnical() {
        return  orderData;
    }


}
