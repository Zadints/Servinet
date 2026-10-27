package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.example.servinet.application.usecase.ClientsUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.ClienteRowController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.scene.layout.HBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.navigator.Navigator;

public class ClientesController {

    @FXML private VBox clientsList;
    @FXML private ComboBox<String> cmbSearchType;
    @FXML private TextField txtSearchClient;
    @FXML private Button btnChangeRanking;
    @FXML private Label lblRankingDescription;
    @FXML private Label lblTopClientName;
    @FXML private Label lblTopClientScore;
    @FXML private Label lblTopClientDescription;
    @FXML private Label lblTotalClients;
    @FXML private Label lblClientsUpToDate;
    @FXML private Label lblClientsPending;

    @FXML private VBox sectionPaymentSummary;
    @FXML private VBox rankingClientCard;
    @FXML private VBox clientsSection;

    @FXML private HBox rankingHeader;
    @FXML private HBox searchControls;

    @FXML private Button btnRefreshClients;

    private boolean puedeVerInfo;
    private boolean puedeVerPagos;
    private boolean puedeBuscar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeVerListaInicial;
    private boolean puedeRanking;

    private IndexController indexController;
    private boolean showingIrresponsible = false;

    public ClientesController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;





    public void initialize() {



        puedeVerInfo = authorizationService.hasPermission(
                Permission.CLIENT_INFO
        );

        puedeVerPagos = authorizationService.hasPermission(
                Permission.CLIENT_PAY_VIEW
        );

        puedeBuscar = authorizationService.hasPermission(
                Permission.CLIENT_SEARCH
        );

        // El proyecto actual exige CLIENTS_BYPASS
        // para editar registros de clientes.
        puedeEditar = authorizationService.hasPermission(
                Permission.CLIENTS_BYPASS
        );

        puedeEliminar = authorizationService.hasPermission(
                Permission.CLIENT_DELETE
        );

        // Mostrar lista cuando existe una acción
        // que necesita identificar clientes.
        puedeVerListaInicial =
                puedeVerInfo
                        || puedeVerPagos
                        || puedeEditar
                        || puedeEliminar;

        // Ranking: requiere información y pagos.
        puedeRanking = puedeVerInfo && puedeVerPagos;

        // Aplicar permisos al diseño.
        mostrar(sectionPaymentSummary, puedeVerPagos);
        mostrar(rankingHeader, puedeRanking);
        mostrar(rankingClientCard, puedeRanking);

        mostrar(searchControls, puedeBuscar);

        mostrar(
                clientsSection,
                puedeVerListaInicial || puedeBuscar
        );

        mostrar(
                btnRefreshClients,
                puedeVerListaInicial || puedeBuscar
        );

        // Cargar únicamente las secciones autorizadas.
        loadClientsInBackground();
    }


    private void mostrar(Node nodo, boolean permitido) {

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }

    // Verificar permiso antes de ejecutar acciones.
    private boolean exigirPermiso(Permission permiso) {

        if (authorizationService.hasPermission(permiso)) {
            return true;
        }

        showAlert(
                Alert.AlertType.WARNING,
                "No tienes permiso para realizar esta acción."
        );

        return false;
    }



    private void loadClientsInBackground() {

        mostrarListaInicial();

        if (puedeVerPagos) {
            updateSummary();
        }

        if (puedeRanking) {
            updateRankingCard();
        }
    }

    private void mostrarListaInicial() {

        if (puedeVerListaInicial) {

            renderClients(ClientsUseCase.getAllClients());
            return;
        }

        clientsList.getChildren().clear();

        // Un usuario con solo CLIENT_SEARCH
        // debe introducir un criterio de búsqueda.
        if (puedeBuscar) {

            Label mensaje = new Label(
                    "Escribe un dato para buscar clientes."
            );

            mensaje.getStyleClass().add("tertiary-text");

            clientsList.getChildren().add(mensaje);
        }
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

            controller.configurarPermisos(
                    puedeVerInfo,
                    puedeVerPagos,
                    puedeEditar,
                    puedeEliminar
            );

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


    public void refreshNowClients() {


        if (!authorizationService.hasPermission(
                Permission.CLIENT_INFO,
                Permission.CLIENT_PAY_VIEW,
                Permission.CLIENT_SEARCH,
                Permission.CLIENT_DELETE,
                Permission.CLIENTS_BYPASS)) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para consultar clientes."
            );

            return;
        }

        Task<List<Client>> task = new Task<>() {
            @Override
            protected List<Client> call() {
                ClientsUseCase.loadClientsAndOrders();
                return ClientsUseCase.getAllClients();
            }
        };

        task.setOnSucceeded(event -> {
            loadClientsInBackground();
        });

        task.setOnFailed(event -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "No se pudieron cargar los clientes.");
        });

        appExecutor.execute(task);
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
        if (!exigirPermiso(Permission.CLIENT_INFO)
                || !exigirPermiso(Permission.CLIENT_PAY_VIEW)) {
            return;
        }
        showingIrresponsible = !showingIrresponsible;
        lblRankingDescription.setText(
                showingIrresponsible
                        ? "Clientes con más pagos pendientes."
                        : "Clientes con mejor comportamiento de pago."
        );
        btnChangeRanking.setText(
                showingIrresponsible
                ? "Ver responsables"
                : "Ver irresponsables"
        );
        updateRankingCard();
    }


    public void searchClients() {

        if (!exigirPermiso(Permission.CLIENT_SEARCH)) {
            return;
        }

        String type = cmbSearchType.getValue();
        String query = txtSearchClient.getText();

        // Evitar mostrar todos los clientes mediante
        // una búsqueda vacía si no tiene permiso
        // de consulta general.
        if ((query == null || query.isBlank())
                && !puedeVerListaInicial) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Debes ingresar un dato para buscar."
            );

            return;
        }

        try {

            List<Client> encontrados =
                    ClientsUseCase.searchClients(type, query);

            renderClients(encontrados);

        } catch (PermissionException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    e.getMessage()
            );
        }
    }


    public void clearSearch() {

        if (!exigirPermiso(Permission.CLIENT_SEARCH)) {
            return;
        }

        txtSearchClient.clear();

        cmbSearchType.getSelectionModel()
                .clearSelection();

        mostrarListaInicial();
    }

    public void createClient() {
        if (!exigirPermiso(Permission.CLIENTS_BYPASS)) return;
        if (indexController == null) return;
        indexController.abrirModalClient(FormType.CLIENT_CREATE, this::refreshClients, null);
    }

    private void editClient(Client client) {
        if (!exigirPermiso(Permission.CLIENTS_BYPASS)) return;
        if (indexController == null || client == null) return;
        indexController.abrirModalClient(FormType.CLIENT_EDIT, this::refreshClients, client);
    }

    private void deleteClient(Client client) {
        if (!exigirPermiso(Permission.CLIENT_DELETE)) return;
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
                ClientsUseCase.deleteClient(client.getDni());
                refreshClients();
            } catch (PermissionException e) {
                showAlert(Alert.AlertType.WARNING, e.getMessage());
            }
        }
    }

    private void showClientInfo(Client client) {
        if (!exigirPermiso(Permission.CLIENT_INFO)) return;
        if (client == null) return;

        String plan = client.getPlan() != null ? client.getPlan().getName() : "Sin plan asignado";
        String fechaInstalacion = client.getInstallationDate() != null
                ? client.getInstallationDate().toLocalDate().toString()
                : "No registrada";

        String info = "Nombre: " + client.getFullName() + "\n"
                + "DNI: " + (client.getDni() != null ? client.getDni() : "--") + "\n"
                + "Teléfono: " + (client.getPhone() != null ? client.getPhone() : "--") + "\n"
                + "Correo: " + (client.getEmail() != null ? client.getEmail() : "--") + "\n"
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
        if (!exigirPermiso(Permission.CLIENT_PAY_VIEW)) return;
        if (client == null) return;

        String info;

        if (client.getAllPayments() == null || client.getAllPayments().isEmpty()) {
            info = "Este cliente no tiene pagos registrados todavía.\n\n"
                    + "Estado actual: " + (client.getPlanStatus() == PlanStatusClient.AL_DIA ? "Al día" : "Con deuda");
        } else {
            StringBuilder sb = new StringBuilder();
            client.getAllPayments().forEach(p -> sb
                    .append(p.getPayDate() != null ? p.getPayDate().toLocalDate() : "--")
                    .append(" • S/. ").append(p.getAmount())
                    .append(" • ").append(p.isPaid() ? "Pagado" : "Pendiente")
                    .append("\n"));
            info = sb.toString();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, ButtonType.OK);
        alert.setHeaderText("Pagos de " + client.getFullName());
        alert.showAndWait();
    }

    private void copyClientProfile(Client client) {
        if (!exigirPermiso(Permission.CLIENT_INFO)) return;
        if (client == null) return;

        String profile = client.getFullName() + " - " +
                (client.getDni() != null ? "DNI " + client.getDni() + " - " : "") +
                (client.getPhone() != null ? client.getPhone() + " - " : "") +
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