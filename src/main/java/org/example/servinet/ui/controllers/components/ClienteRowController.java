package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.core.domain.entities.Client;
import org.example.servinet.core.domain.enums.PlanStatusClient;

import java.util.function.Consumer;

public class ClienteRowController {

    @FXML private Label lblClientName;
    @FXML private Label lblClientPhonePlan;
    @FXML private Label lblClientZoneAddress;
    @FXML private Label lblClientStatus;
    @FXML private Label lblClientPaymentStatus;

    private Client client;

    private Consumer<Client> onInfo;
    private Consumer<Client> onPayments;
    private Consumer<Client> onCopy;
    private Consumer<Client> onEdit;
    private Consumer<Client> onDelete;

    public void setClient(Client client) {
        this.client = client;

        lblClientName.setText(client.getFullName());

        String planName = client.getPlan() != null ? client.getPlan().getName() : "Sin plan";
        String phone = client.getPhone() != null ? client.getPhone() : "Sin teléfono";
        lblClientPhonePlan.setText(phone + " • " + planName);

        String zone = client.getZone() != null ? client.getZone() : "--";
        String address = client.getAddress() != null ? client.getAddress() : "--";
        lblClientZoneAddress.setText("Zona: " + zone + " • " + address);

        lblClientStatus.setText(client.getClientStatus().toString().toUpperCase());

        boolean alDia = client.getPlanStatus() == PlanStatusClient.AL_DIA;
        lblClientPaymentStatus.setText(alDia ? "AL DÍA" : "CON DEUDA");
        lblClientPaymentStatus.getStyleClass().removeAll("payment-paid", "payment-debt");
        lblClientPaymentStatus.getStyleClass().add(alDia ? "payment-paid" : "payment-debt");
    }

    public void setOnInfo(Consumer<Client> onInfo) { this.onInfo = onInfo; }
    public void setOnPayments(Consumer<Client> onPayments) { this.onPayments = onPayments; }
    public void setOnCopy(Consumer<Client> onCopy) { this.onCopy = onCopy; }
    public void setOnEdit(Consumer<Client> onEdit) { this.onEdit = onEdit; }
    public void setOnDelete(Consumer<Client> onDelete) { this.onDelete = onDelete; }

    @FXML private void onInfoClick() { if (onInfo != null) onInfo.accept(client); }
    @FXML private void onPaymentsClick() { if (onPayments != null) onPayments.accept(client); }
    @FXML private void onCopyClick() { if (onCopy != null) onCopy.accept(client); }
    @FXML private void onEditClick() { if (onEdit != null) onEdit.accept(client); }
    @FXML private void onDeleteClick() { if (onDelete != null) onDelete.accept(client); }
}