package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.PlanStatusClient;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ClienteRowController {

    @FXML private Label lblClientName;
    @FXML private Label lblClientPhonePlan;
    @FXML private Label lblClientZoneAddress;
    @FXML private Label lblClientStatus;
    @FXML private Label lblClientPaymentStatus;

    @FXML private Button btnClientInfo;
    @FXML private Button btnClientPayments;
    @FXML private Button btnClientCopy;
    @FXML private Button btnClientEdit;
    @FXML private Button btnClientDelete;

    @FXML private VBox clientStatusBox;
    @FXML private VBox clientPaymentBox;

    private boolean puedeVerInfo;
    private boolean puedeVerPagos;

    private Client client;

    private Consumer<Client> onInfo;
    private Consumer<Client> onPayments;
    private Consumer<Client> onCopy;
    private Consumer<Client> onEdit;
    private Consumer<Client> onDelete;


    public void configurarPermisos(
            boolean info,
            boolean pagos,
            boolean editar,
            boolean eliminar
    ) {

        this.puedeVerInfo = info;
        this.puedeVerPagos = pagos;

        // Botones de la fila
        mostrar(btnClientInfo, info);
        mostrar(btnClientPayments, pagos);
        mostrar(btnClientCopy, info);
        mostrar(btnClientEdit, editar);
        mostrar(btnClientDelete, eliminar);

        // Información adicional
        mostrar(clientStatusBox, info);
        mostrar(clientPaymentBox, pagos);

        mostrar(lblClientPhonePlan, info);
        mostrar(lblClientZoneAddress, info);
    }

    private void mostrar(Node nodo, boolean permitido) {

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }



    public void setClient(Client client) {

        this.client = client;

        // Nombre necesario para identificar el registro
        lblClientName.setText(client.getFullName());

        // Datos del perfil
        if (puedeVerInfo) {

            String planName = client.getPlan() != null
                    ? client.getPlan().getName()
                    : "Sin plan";

            String phone = client.getPhone() != null
                    ? client.getPhone()
                    : "Sin teléfono";

            lblClientPhonePlan.setText(
                    phone + " • " + planName
            );

            String address = client.getAddress() != null
                    ? client.getAddress()
                    : "--";

            lblClientZoneAddress.setText(
                    "• Dirección: " + address
            );

            lblClientStatus.setText(
                    String.valueOf(client.getClientStatus())
                            .toUpperCase()
            );

        } else {

            lblClientPhonePlan.setText("");
            lblClientZoneAddress.setText("");
            lblClientStatus.setText("");
        }

        // Información de pagos
        if (puedeVerPagos) {

            boolean alDia =
                    client.getPlanStatus() == PlanStatusClient.AL_DIA;

            lblClientPaymentStatus.setText(
                    alDia ? "AL DÍA" : "CON DEUDA"
            );

            lblClientPaymentStatus.getStyleClass()
                    .removeAll("payment-paid", "payment-debt");

            lblClientPaymentStatus.getStyleClass().add(
                    alDia ? "payment-paid" : "payment-debt"
            );

        } else {

            lblClientPaymentStatus.setText("");
        }
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