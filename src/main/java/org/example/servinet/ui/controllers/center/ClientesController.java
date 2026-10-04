package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.example.servinet.core.application.usecase.ClientsUseCase;
import org.example.servinet.core.domain.entities.Client;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.core.domain.enums.PlanStatusClient;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.infrastructure.concurrency.AppExecutor;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.ClienteRowController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ClientesController {

    @FXML private VBox clientsList;
    @FXML private ComboBox<String> cmbSearchType;
    @FXML private TextField txtSearchClient;

    @FXML private Label lblRankingDescription;
    @FXML private Label lblTopClientName;
    @FXML private Label lblTopClientScore;
    @FXML private Label lblTopClientDescription;

    @FXML private Label lblTotalClients;
    @FXML private Label lblClientsUpToDate;
    @FXML private Label lblClientsPending;

    private IndexController indexController;
    private boolean showingIrresponsible = false;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    public void initialize() {
        renderClients(ClientsUseCase.getAllClients());
        updateSummary();
        updateRankingCard();

        loadClientsInBackground();
    }

    private void loadClientsInBackground() {

        Task<List<Client>> task = new Task<>() {
            @Override
            protected List<Client> call() {

                return ClientsUseCase.getAllClients();
            }
        };

        task.setOnSucceeded(event -> {
            renderClients(task.getValue());
            updateSummary();
            updateRankingCard();
        });

        task.setOnFailed(event -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "No se pudieron cargar los clientes.");
        });

        AppExecutor.execute(task);
    }

    private void renderClients(List<Client> clients) {

        clientsList.getChildren().clear();

        if (clients == null || clients.isEmpty()) {
            Label empty = new Label("No hay clientes para mostrar.");
            empty.getStyleClass().add("tertiary-text");
            clientsList.getChildren().add(empty);
            return;
        }

        for (Client client : clients) {
            addClientRow(client);
        }
    }

    private void addClientRow(Client client) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/cliente-row.fxml")
            );

            Node row = loader.load();
            ClienteRowController controller = loader.getController();

            controller.setClient(client);
            controller.setOnInfo(this::showClientInfo);
            controller.setOnPayments(this::showClientPayments);
            controller.setOnCopy(this::copyClientProfile);
            controller.setOnEdit(this::editClient);
            controller.setOnDelete(this::deleteClient);

            clientsList.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshClients() {
        loadClientsInBackground();
    }

    private void updateSummary() {
        lblTotalClients.setText(String.valueOf(ClientsUseCase.getTotalClients()));
        lblClientsUpToDate.setText(String.valueOf(ClientsUseCase.getClientsUpToDate()));
        lblClientsPending.setText(String.valueOf(ClientsUseCase.getClientsPending()));
    }

    private void updateRankingCard() {

        if (showingIrresponsible) {
            List<Client> worst = ClientsUseCase.getMostIrresponsibleClients();
            if (worst.isEmpty()) {
                clearTopClientCard();
                return;
            }
            Client peor = worst.get(0);
            lblTopClientName.setText(peor.getFullName());
            lblTopClientScore.setText(peor.getScorePayment() + "%");
            lblTopClientDescription.setText("Cliente con más pagos pendientes.");
            return;
        }

        Client best = ClientsUseCase.getBestClient();

        if (best == null) {
            clearTopClientCard();
            return;
        }

        lblTopClientName.setText(best.getFullName());
        lblTopClientScore.setText(best.getScorePayment() + "%");
        lblTopClientDescription.setText("Pagos realizados puntualmente.");
    }

    private void clearTopClientCard() {
        lblTopClientName.setText("--");
        lblTopClientScore.setText("--");
        lblTopClientDescription.setText("Aún no hay clientes registrados.");
    }

    public void changeRanking() {
        showingIrresponsible = !showingIrresponsible;
        lblRankingDescription.setText(
                showingIrresponsible
                        ? "Clientes con más pagos pendientes."
                        : "Clientes con mejor comportamiento de pago."
        );
        updateRankingCard();
    }

    public void searchClients() {
        try {
            String type = cmbSearchType.getValue();
            String query = txtSearchClient.getText();
            renderClients(ClientsUseCase.searchClients(type, query));
        } catch (RoleNoPermission e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    public void clearSearch() {
        txtSearchClient.clear();
        cmbSearchType.getSelectionModel().clearSelection();
        renderClients(ClientsUseCase.getAllClients());
    }

    public void createClient() {
        if (indexController == null) return;
        indexController.abrirModalClient(FormType.CLIENT_CREATE, this::refreshClients, null);
    }

    private void editClient(Client client) {
        if (indexController == null || client == null) return;
        indexController.abrirModalClient(FormType.CLIENT_EDIT, this::refreshClients, client);
    }

    private void deleteClient(Client client) {
        if (client == null) return;

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que deseas eliminar a " + client.getFullName() + "? Esta acción no se puede deshacer.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setHeaderText("Eliminar cliente");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                ClientsUseCase.deleteClient(client.getUuid());
                refreshClients();
            } catch (RoleNoPermission e) {
                showAlert(Alert.AlertType.WARNING, e.getMessage());
            }
        }
    }

    private void showClientInfo(Client client) {
        if (client == null) return;

        String plan = client.getPlan() != null ? client.getPlan().getName() : "Sin plan asignado";
        String fechaInstalacion = client.getInstallationDate() != null
                ? client.getInstallationDate().toLocalDate().toString()
                : "No registrada";

        String info = "Nombre: " + client.getFullName() + "\n"
                + "DNI: " + (client.getDni() != null ? client.getDni() : "--") + "\n"
                + "Teléfono: " + (client.getPhone() != null ? client.getPhone() : "--") + "\n"
                + "Correo: " + (client.getEmail() != null ? client.getEmail() : "--") + "\n"
                + "Zona: " + (client.getZone() != null ? client.getZone() : "--") + "\n"
                + "Dirección: " + (client.getAddress() != null ? client.getAddress() : "--") + "\n"
                + "Plan: " + plan + "\n"
                + "Fecha de instalación: " + fechaInstalacion + "\n"
                + "Estado: " + client.getClientStatus() + "\n"
                + "Pago: " + (client.getPlanStatus() == PlanStatusClient.AL_DIA ? "Al día" : "Con deuda");

        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, ButtonType.OK);
        alert.setHeaderText("Información del cliente");
        alert.showAndWait();
    }

    private void showClientPayments(Client client) {
        if (client == null) return;

        String info;

        if (client.getPayments() == null || client.getPayments().isEmpty()) {
            info = "Este cliente no tiene pagos registrados todavía.\n\n"
                    + "Estado actual: " + (client.getPlanStatus() == PlanStatusClient.AL_DIA ? "Al día" : "Con deuda");
        } else {
            StringBuilder sb = new StringBuilder();
            client.getPayments().forEach(p -> sb
                    .append(p.getPayDate() != null ? p.getPayDate().toLocalDate() : "--")
                    .append(" • S/. ").append(p.getMont())
                    .append(" • ").append(p.getPaid() ? "Pagado" : "Pendiente")
                    .append("\n"));
            info = sb.toString();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, ButtonType.OK);
        alert.setHeaderText("Pagos de " + client.getFullName());
        alert.showAndWait();
    }

    private void copyClientProfile(Client client) {
        if (client == null) return;

        String profile = client.getFullName() + " - " +
                (client.getDni() != null ? "DNI " + client.getDni() + " - " : "") +
                (client.getPhone() != null ? client.getPhone() + " - " : "") +
                (client.getZone() != null ? "Zona: " + client.getZone() + " - " : "") +
                (client.getAddress() != null ? client.getAddress() : "");

        ClipboardContent content = new ClipboardContent();
        content.putString(profile);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }
}