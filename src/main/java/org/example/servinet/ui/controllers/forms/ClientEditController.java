package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.clients.EditClientUseCase;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.ui.events.MouseMove;

public class ClientEditController {

    public AnchorPane menuClientEdit;
    @FXML private Label lblErrorEditClient;
    @FXML private TextField txtClientFirstNameEdit;
    @FXML private TextField txtClientLastNameEdit;
    @FXML private TextField txtClientDniEdit;
    @FXML private TextField txtClientPhoneEdit;
    @FXML private TextField txtClientEmailEdit;
    @FXML private TextField txtClientAddressEdit;
    @FXML private ComboBox<ClientPlan> cbxClientPlanEdit;
    @FXML private TextField txtClientNumWhatsApp;
    @FXML private Button btnClose;
    private Client client;
    private final EditClientUseCase editClientUseCase;
    private final PlanUseCase planUseCase;
    public ClientEditController(EditClientUseCase editClientUseCase, PlanUseCase planUseCase) {
        this.editClientUseCase = editClientUseCase;
        this.planUseCase = planUseCase;
    }

    public void initialize(){
        MouseMove mouseMove = new MouseMove(menuClientEdit);
        mouseMove.start();
    }

    public void setClient(Client client) {
        this.client = client;

        txtClientFirstNameEdit.setText(client.getFirstName());
        txtClientLastNameEdit.setText(client.getLastName());
        txtClientDniEdit.setText(client.getDni());
        txtClientPhoneEdit.setText(client.getPhone());
        txtClientEmailEdit.setText(client.getEmail());
        txtClientAddressEdit.setText(client.getAddress());
        txtClientNumWhatsApp.setText(client.getPhone());
        ClientPlan plan = planUseCase.getPlan(client.getOrder().getPlanUuid());
        cbxClientPlanEdit.setValue(plan);
    }

    public void saveEditClient(ActionEvent actionEvent) {

        if (client == null) {
            lblErrorEditClient.setVisible(true);
            lblErrorEditClient.setManaged(true);
            lblErrorEditClient.setText("No hay ningún cliente seleccionado para editar.");
            return;
        }

        ClientPlan clientPlan = cbxClientPlanEdit.getValue();

        if (clientPlan == null) {
            lblErrorEditClient.setVisible(true);
            lblErrorEditClient.setManaged(true);
            lblErrorEditClient.setText("No hay ningún plan seleccionado para editar.");
        }

        ClientDto dto = new ClientDto(

                txtClientDniEdit.getText(),
                txtClientFirstNameEdit.getText(),
                txtClientLastNameEdit.getText(),
                txtClientPhoneEdit.getText(),
                txtClientAddressEdit.getText(),
                txtClientEmailEdit.getText(),
                txtClientNumWhatsApp.getText(),
                clientPlan,
                null,
                null,
                null
        );

        try {
            editClientUseCase.editClient(dto);
            closeForm(actionEvent);
        } catch (InvalidValueException | PermissionException e) {
            lblErrorEditClient.setVisible(true);
            lblErrorEditClient.setManaged(true);
            lblErrorEditClient.setText(e.getMessage());
        }

    }

    public void closeForm(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
