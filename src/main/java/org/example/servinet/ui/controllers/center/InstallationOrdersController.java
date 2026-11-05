package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.TechnicalUseCase;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.InstallationReport;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class InstallationOrdersController {

    private final GetClientUseCase getClientUseCase;
    private final GetAntennaUseCase getAntennaUseCase;
    private final PlanUseCase planUseCase;
    private final AppExecutor appExecutor;
    private final TechnicalUseCase technicalUseCase;

    private Client selectedClient;
    private Client activeInstallation; // Solo protección local hasta conectar persistencia geteee.
    private File routerImage, connectionImage, miniAntennaImage;
    private int loadVersion;
    private int detailsVersion;
    private boolean busy;
    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    @FXML private HBox ordersCarousel;
    @FXML private VBox emptyOrdersContainer, ordersCarouselSection, installationReportSection;
    @FXML private Label lblAvailableOrders, lblAcceptedOrders, lblLastUpdate;
    @FXML private Label lblSelectedOrderSubtitle, lblSelectedOrderStatus;
    @FXML private Label lblOrderClientName, lblOrderClientDni, lblOrderClientPhone;
    @FXML private Label lblOrderCreated, lblOrderPlan, lblOrderAntenna;
    @FXML private Label lblOrderAddress, lblOrderObservations, lblOrderActionFeedback;
    @FXML private Label lblRouterImageFile, lblConnectionImageFile, lblMiniAntennaImageFile, lblReportFeedback;
    @FXML private Button btnAcceptOrder, btnRejectOrder, btnBackToOrders, btnSaveReport, btnRefreshOrders;
    @FXML private TextField txtReportClientDni;
    @FXML private PasswordField txtRouterPassword;
    @FXML private TextArea txtReportObservation;

    public InstallationOrdersController(GetClientUseCase getClientUseCase,
                                        GetAntennaUseCase getAntennaUseCase,
                                        PlanUseCase planUseCase,
                                        AppExecutor appExecutor, TechnicalUseCase technicalUseCase) {
        this.getClientUseCase = Objects.requireNonNull(getClientUseCase);
        this.getAntennaUseCase = Objects.requireNonNull(getAntennaUseCase);
        this.planUseCase = Objects.requireNonNull(planUseCase);
        this.appExecutor = Objects.requireNonNull(appExecutor);
        this.technicalUseCase = technicalUseCase;
    }

    @FXML
    private void initialize() {
        clearSelectedOrder();
        showForm(false);
        refreshOrders();
    }

    @FXML
    public void refreshOrders() {
        if (activeInstallation != null) {
            showForm(true);
            return;
        }
        final int version = ++loadVersion;
        ++detailsVersion;
        clearSelectedOrder();
        ordersCarousel.getChildren().clear();
        emptyOrdersContainer.setVisible(false);
        emptyOrdersContainer.setManaged(false);
        btnRefreshOrders.setDisable(true);
        lblAvailableOrders.setText("Cargando...");

        Task<List<Client>> task = new Task<>() {
            @Override protected List<Client> call() {
                return getClientUseCase.getAll().stream()
                        .filter(Objects::nonNull)
                        .filter(c -> c.getClientStatus() == ClientStatus.EN_INSTALACION)
                        .filter(c -> c.getOrder() != null)
                        .toList();
            }
        };
        task.setOnSucceeded(event -> {
            if (version != loadVersion) return;
            List<Client> clients = task.getValue();
            // Solo en el hilo JavaFX se crean nodos y se actualiza la interfaz.
            clients.forEach(this::createOrderCard);
            lblAvailableOrders.setText(String.valueOf(clients.size()));
            lblAcceptedOrders.setText("0"); // Reemplazar por estadística real del técnico.
            lblLastUpdate.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            emptyOrdersContainer.setVisible(clients.isEmpty());
            emptyOrdersContainer.setManaged(clients.isEmpty());
            btnRefreshOrders.setDisable(false);
        });
        task.setOnFailed(event -> {
            if (version != loadVersion) return;
            btnRefreshOrders.setDisable(false);
            lblAvailableOrders.setText("-");
            lblOrderActionFeedback.setText("No se pudieron cargar los pedidos: " + message(task.getException()));
        });
        appExecutor.execute(task);
    }

    private void createOrderCard(Client client) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setMinWidth(245);
        card.setPrefWidth(245);
        card.setMaxWidth(245);
        card.getStyleClass().add("admin-option");
        card.setCursor(Cursor.HAND);
        Label name = label(client.getFirstName() + " " + client.getLastName(), "primary-text");
        Label dni = label("DNI: " + client.getDni(), "secondary-text");
        Label phone = label(value(client.getPhone(), "Sin teléfono"), "secondary-text");
        Label status = label(client.getClientStatus().getValue(), "tertiary-text");
        Label orderId = label("Pedido: " + client.getOrder().getOrderId(), "secondary-text");
        card.getChildren().addAll(name, dni, phone, status, orderId);
        card.setOnMouseClicked(event -> selectClient(client, card));
        ordersCarousel.getChildren().add(card);
    }

    private Label label(String text, String css) {
        Label label = new Label(text);
        label.getStyleClass().add(css);
        label.setWrapText(true);
        return label;
    }

    private void selectClient(Client client, VBox card) {
        if (activeInstallation != null || busy) return;
        selectedClient = client;
        ordersCarousel.getChildren().forEach(node -> node.setStyle(null));
        card.setStyle("-fx-border-color: #00A6FF; -fx-border-width: 1.5; -fx-border-radius: 8;");
        renderBasicDetails(client);
        lblOrderPlan.setText("Cargando...");
        lblOrderAntenna.setText("Cargando...");
        lblOrderActionFeedback.setText("");
        btnAcceptOrder.setDisable(true);
        btnRejectOrder.setDisable(true);
        final int version = ++detailsVersion;
        final Order order = client.getOrder();
        if (order == null) {
            lblOrderActionFeedback.setText("El cliente no tiene pedido.");
            return;
        }
        Task<OrderDetails> task = new Task<>() {
            @Override protected OrderDetails call() {
                String antennaName = "Sin antena asignada";
                String planName = "Sin plan asignado";
                if (order.getAntennaUuid() != null) {
                    Antenna antenna = getAntennaUseCase.getAntenna(order.getAntennaUuid());
                    antennaName = antenna == null ? "Antena no encontrada" : antenna.getName();
                }
                if (order.getPlanUuid() != null) {
                    ClientPlan plan = planUseCase.getPlan(order.getPlanUuid());
                    planName = plan == null ? "Plan no encontrado" : plan.getName();
                }
                return new OrderDetails(antennaName, planName);
            }
        };
        task.setOnSucceeded(event -> {
            if (version != detailsVersion || selectedClient != client) return;
            lblOrderAntenna.setText(task.getValue().antenna());
            lblOrderPlan.setText(task.getValue().plan());
            btnAcceptOrder.setDisable(activeInstallation != null);
            btnRejectOrder.setDisable(activeInstallation != null);
        });
        task.setOnFailed(event -> {
            if (version != detailsVersion || selectedClient != client) return;
            lblOrderAntenna.setText("No disponible");
            lblOrderPlan.setText("No disponible");
            lblOrderActionFeedback.setText("No se pudieron cargar los detalles: " + message(task.getException()));
            btnAcceptOrder.setDisable(false); // Permite continuar si la consulta informativa falla.
            btnRejectOrder.setDisable(false);
        });
        appExecutor.execute(task);
    }

    private record OrderDetails(String antenna, String plan) {}

    private void renderBasicDetails(Client client) {
        String name = value(client.getFirstName(), "") + " " + value(client.getLastName(), "");
        lblSelectedOrderSubtitle.setText(name.trim());
        lblSelectedOrderStatus.setText(client.getClientStatus().getValue());
        lblOrderClientName.setText(name.trim());
        lblOrderClientDni.setText(client.getDni());
        lblOrderClientPhone.setText(value(client.getPhone(), "No registrado"));
        lblOrderAddress.setText(value(client.getAddress(), "No registrada"));
        Order order = client.getOrder();
        lblOrderCreated.setText(order != null && order.getOrderCreated() != null
                ? order.getOrderCreated().format(DATE_FORMAT) : "Sin fecha");
        lblOrderObservations.setText("Sin observaciones registradas");
    }

    @FXML
    public void acceptdInstalled() {
        if (busy || selectedClient == null || selectedClient.getOrder() == null) return;
        if (activeInstallation != null) {
            lblOrderActionFeedback.setText("Finaliza tu instalación actual antes de aceptar otra.");
            return;
        }

        try {
            technicalUseCase.acceptInstallation(
                    selectedClient.getOrder().getOrderId(),
                    selectedClient.getDni()
            );

            activeInstallation = selectedClient;
            ++detailsVersion;
            lblAcceptedOrders.setText("1");
            prepareReportForm(activeInstallation);
            showForm(true);
        }catch (Exception e){
            lblReportFeedback.setText(e.getMessage());
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());

        }


    }

    private void prepareReportForm(Client client) {
        txtReportClientDni.setText(client.getDni());
        txtRouterPassword.clear();
        txtReportObservation.clear();
        routerImage = connectionImage = miniAntennaImage = null;
        lblRouterImageFile.setText("Ningún archivo seleccionado");
        lblConnectionImageFile.setText("Ningún archivo seleccionado");
        lblMiniAntennaImageFile.setText("Ningún archivo seleccionado");
        lblReportFeedback.setText("");
    }

    private void showForm(boolean show) {
        ordersCarouselSection.setVisible(!show);
        ordersCarouselSection.setManaged(!show);
        installationReportSection.setVisible(show);
        installationReportSection.setManaged(show);

        btnBackToOrders.setVisible(!show && selectedClient != null);
        btnBackToOrders.setManaged(!show && selectedClient != null);
        btnAcceptOrder.setDisable(show || selectedClient == null);
        btnRejectOrder.setDisable(show || selectedClient == null);
    }

    @FXML
    public void backToOrders() {
        if (activeInstallation != null) {
            lblReportFeedback.setText("Tienes una instalación en curso. Debes finalizarla antes de aceptar otra.");
            return;
        }
        ++detailsVersion;
        clearSelectedOrder();
        showForm(false);
    }

    @FXML
    private void rejectSelectedOrder() {

        if (activeInstallation == null) {
            return;
        }

        String orderId = activeInstallation.getOrder().getOrderId();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                technicalUseCase.rejectInstallation(orderId);
                return null;
            }
        };

        btnRejectOrder.setDisable(true);

        task.setOnSucceeded(event -> {
            activeInstallation = null;
            selectedClient = null;

            backToOrders();
            refreshOrders();
        });

        task.setOnFailed(event -> {
            btnRejectOrder.setDisable(false);

            lblOrderActionFeedback.setText(
                    task.getException().getMessage()
            );
        });

        appExecutor.execute(task);
    }

    @FXML public void selectRouterImage() {
        File file = chooseImage();
        if (file != null) { routerImage = file; lblRouterImageFile.setText(file.getName()); }
    }
    @FXML public void selectConnectionImage() {
        File file = chooseImage();
        if (file != null) { connectionImage = file; lblConnectionImageFile.setText(file.getName()); }
    }
    @FXML public void selectMiniAntennaImage() {
        File file = chooseImage();
        if (file != null) { miniAntennaImage = file; lblMiniAntennaImageFile.setText(file.getName()); }
    }

    private File chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar evidencia fotográfica");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes JPG, PNG o WEBP", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File file = chooser.showOpenDialog(btnSaveReport.getScene().getWindow());
        if (file != null && (!file.isFile() || file.length() > MAX_IMAGE_BYTES)) {
            lblReportFeedback.setText("La imagen debe existir y pesar como máximo 8 MB.");
            return null;
        }
        return file;
    }

    @FXML
    public void saveInstallationReport() {
        if (busy || activeInstallation == null || activeInstallation.getOrder() == null) return;
        String password = txtRouterPassword.getText();
        final String orderId = activeInstallation.getOrder().getOrderId();
        final String dni = activeInstallation.getDni();
        final String observation = txtReportObservation.getText().trim();
        final File router = routerImage, connection = connectionImage, antenna = miniAntennaImage;
        busy = true;
        btnSaveReport.setDisable(true);
        lblReportFeedback.setText("Preparando reporte...");

        Task<InstallationReport> task = new Task<>() {
            @Override protected InstallationReport call() throws Exception {
                return new InstallationReport(orderId, dni, observation,
                        readImage(router), readImage(connection), readImage(antenna), password);
            }
        };
        task.setOnSucceeded(event -> {
            busy = false;
            btnSaveReport.setDisable(false);
            InstallationReport report = task.getValue();

            try {
                technicalUseCase.finishInstallation(report);
            }catch (InvalidValueException | DatabaseException e){
                lblReportFeedback.setText(e.getMessage());
                return;
            }


            lblReportFeedback.setText("Reporte preparado para pedido " + report.getOrderId()
                    + ". Falta conectar el guardado en la base de datos.");
        });
        task.setOnFailed(event -> {
            busy = false;
            btnSaveReport.setDisable(false);
            lblReportFeedback.setText("Error preparando reporte: " + message(task.getException()));
        });
        appExecutor.execute(task);
    }

    private static byte[] readImage(File file) throws Exception {
        if (file == null) return null;
        if (!file.isFile() || file.length() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Imagen inválida o mayor de 8 MB: " + file.getName());
        }
        return Files.readAllBytes(file.toPath());
    }

    private void clearSelectedOrder() {
        selectedClient = null;
        lblSelectedOrderSubtitle.setText("Selecciona una solicitud para ver sus detalles");
        lblSelectedOrderStatus.setText("-");
        lblOrderClientName.setText("-");
        lblOrderClientDni.setText("-");
        lblOrderClientPhone.setText("-");
        lblOrderCreated.setText("-");
        lblOrderPlan.setText("-");
        lblOrderAntenna.setText("-");
        lblOrderAddress.setText("-");
        lblOrderObservations.setText("-");
        lblOrderActionFeedback.setText("");
        btnAcceptOrder.setDisable(true);
        btnRejectOrder.setDisable(true);
        btnBackToOrders.setVisible(false);
        btnBackToOrders.setManaged(false);
    }

    private static String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
    private static String message(Throwable throwable) {
        return throwable == null ? "Error desconocido" : value(throwable.getMessage(), throwable.getClass().getSimpleName());
    }
}

