package org.example.servinet.ui.controllers.center;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.servinet.application.usecase.clients.DeleteClientUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.ui.animations.LoadingAnimation;
import org.example.servinet.ui.controllers.components.ClienteRowController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.layout.HBox;

import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.controllers.forms.ClientEditController;
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

    @FXML private StackPane loadingOverlay;
    @FXML private Circle loadingCircle;
    @FXML private Label lblLoading;

    private boolean puedeVerInfo;
    private boolean puedeVerPagos;
    private boolean puedeBuscar;
    private boolean puedeEditar;
    private boolean puedeEliminar;
    private boolean puedeVerListaInicial;
    private boolean puedeRanking;
    private final PauseTransition searchDelay =
            new PauseTransition(Duration.millis(350));

    private final AtomicLong searchVersion = new AtomicLong();
    private PauseTransition renderPause;
    private boolean showingIrresponsible = false;
    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetClientUseCase getClientUseCase;
    private final LoadingAnimation loadingAnimation;
    private final DeleteClientUseCase deleteUseCase;

    public ClientesController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService, GetClientUseCase getClientUseCase, LoadingAnimation loadingAnimation, DeleteClientUseCase deleteUseCase) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
        this.getClientUseCase = getClientUseCase;
        this.loadingAnimation = loadingAnimation;
        this.deleteUseCase = deleteUseCase;
    }


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

        puedeRanking = puedeVerInfo && puedeVerPagos;

        mostrar(sectionPaymentSummary, puedeVerPagos);
        mostrar(rankingHeader, puedeRanking);
        mostrar(rankingClientCard, puedeRanking);

        mostrar(searchControls, puedeBuscar);

        mostrar(
                clientsSection,
                puedeVerListaInicial || puedeBuscar
        );

        loadClientsInBackground();

        searchDelay.setOnFinished(event -> {

            searchClients();
        });

        txtSearchClient.textProperty().addListener(
                (obs, oldValue, newValue) -> searchDelay.playFromStart()
        );
    }


    private void mostrar(Node nodo, boolean permitido) {

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }


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

        if (!puedeVerListaInicial) return;

        loadingAnimation.onAnimation(
                loadingOverlay,
                loadingCircle,
                lblLoading
        );

        Task<List<Client>> task = new Task<>() {
            @Override
            protected List<Client> call() {
                return getClientUseCase.getAll();
            }
        };

        task.setOnSucceeded(event -> {
            loadingAnimation.finishAnimation();

            List<Client> clients = task.getValue();

            clientsList.getChildren().clear();

            if (clients == null || clients.isEmpty()) {
                Label mensaje = new Label("No hay clientes registrados.");
                mensaje.getStyleClass().add("tertiary-text");
                clientsList.getChildren().add(mensaje);
                return;
            }

            renderClients(clients);

            if (puedeBuscar) {
                Label mensaje = new Label(
                        "Escribe un dato para buscar clientes."
                );
                mensaje.getStyleClass().add("tertiary-text");
                clientsList.getChildren().add(mensaje);
            }
        });

        task.setOnFailed(event -> {
            loadingAnimation.finishAnimation();

            Throwable error = task.getException();

            System.err.println(
                    "Error cargando clientes: " +
                            (error != null ? error.getMessage() : "Error desconocido")
            );
        });

        appExecutor.execute(task);
    }


    private void renderClients(List<Client> clients) {

        // Cancelar un renderizado anterior, si existe.
        if (renderPause != null) {
            renderPause.stop();
        }

        clientsList.getChildren().clear();

        if (clients == null || clients.isEmpty()) {
            Label empty = new Label("No hay clientes para mostrar.");
            empty.getStyleClass().add("tertiary-text");
            clientsList.getChildren().add(empty);
            return;
        }

        final int[] index = {0};
        final int batchSize = 10;

        renderPause = new PauseTransition(Duration.millis(16));

        renderPause.setOnFinished(event -> {

            int limit = Math.min(index[0] + batchSize, clients.size());

            while (index[0] < limit) {
                addClientRow(clients.get(index[0]));
                index[0]++;
            }

            if (index[0] < clients.size()) {
                renderPause.playFromStart();
            } else {
                renderPause = null;
            }
        });

        renderPause.play();
    }

    private void addClientRow(Client client) {
        try {

            ClienteRowController controller = navigator.loadComponent(
                    "/org/example/servinet/components/cliente-row.fxml",
                    clientsList.getChildren()::add
            );

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

        } catch (IOException e) {
            System.out.println("Error al cargar la fila del cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshClients() {
        loadClientsInBackground();
    }


    /*public void refreshNowClients() {


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
                ClientService.loadClientsAndOrders();
                return ClientService.getAllClients();
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
    }*/

    private void updateSummary() {



        lblTotalClients.setText(String.valueOf(getClientUseCase.getTotalClients()));
        lblClientsUpToDate.setText(String.valueOf(getClientUseCase.getClientsUpToDate()));
        lblClientsPending.setText(String.valueOf(getClientUseCase.getClientsPending()));
    }

    private void updateRankingCard() {

        if (showingIrresponsible) {
            List<Client> worst = getClientUseCase.getMostIrresponsibleClients();
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

        Client best = getClientUseCase.getBestClient();

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
        long currentVersion = searchVersion.incrementAndGet();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    List<Client> clients =
                            getClientUseCase.searchClients(type, query);

                    Platform.runLater(() -> {
                        if (currentVersion != searchVersion.get()) return;

                        clientsList.getChildren().clear();

                        for (Client client : clients) {
                            addClientRow(client);
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (currentVersion != searchVersion.get()) return;

                        System.err.println(
                                "Error buscando clientes: " + e.getMessage()
                        );
                    });
                }

                return null;
            }
        };

        appExecutor.execute(task);
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


    private void editClient(Client client) {
        if (!exigirPermiso(Permission.CLIENTS_BYPASS)) return;

        try {
            navigator.openWindow(
                    "/org/example/servinet/forms/client-edit.fxml",
                    (Stage) btnChangeRanking.getScene().getWindow(),
                    0.50,
                    0.75,
                    controller -> {
                        ClientEditController editController = (ClientEditController) controller;
                        editController.setClient(client);
                    }
            );
            renderClients(getClientUseCase.getAll());
        }catch (IOException e){
            System.out.println(e.getMessage() +e.getCause());
        }
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
                deleteUseCase.deleteClient(client.getDni());
                refreshClients();
            } catch (Exception e) {
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
                    .append(p.getDatePaidUser() != null ? p.getDatePaidUser().toLocalDate() : "--")
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