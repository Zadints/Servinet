package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.entities.client.OrderReport;


public class TechnicalHistoryController {

    @FXML private Label lblOrderId;
    @FXML private Label lblOrderTimeEnd;
    @FXML private Label lblOrderStatus;
    @FXML private Label lblClientName;
    @FXML private Label lblClientPlan;
    @FXML private Label lblOrderCreate;
    private OrderContainer orderData;

    public void setTechnicalHistory(OrderContainer orderContainer, ClientPlan plan){

        OrderReport report = orderContainer.orderReport();
        Order order = orderContainer.order();

        lblOrderId.setText(order.getOrderId());
        lblOrderTimeEnd.setText(
                String.valueOf(
                        report.getReportEnded() != null
                                ? report.getReportEnded()
                                : "No finalizado"
                )
        );
        lblOrderStatus.setText(order.getOrderStatus().toString());
        lblClientName.setText(report.getReportClientDni());
        lblClientPlan.setText(plan.toString());
        lblOrderCreate.setText(order.getOrderCreated().toString());
    }
    public OrderContainer getTechnical() {
        return  orderData;
    }


}
