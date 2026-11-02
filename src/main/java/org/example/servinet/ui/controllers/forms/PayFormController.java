
package org.example.servinet.ui.controllers.forms;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.example.servinet.application.dto.PaidPaymentDto;
import org.example.servinet.application.dto.PaymentsScore;
import org.example.servinet.application.usecase.payments.GetPaymentUseCase;
import org.example.servinet.application.usecase.payments.PaidPaymentUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.domain.enums.PayMethods;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.events.MouseMove;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PayFormController {

    public AnchorPane maincontent;
    public Button btnClose;

    @FXML private ComboBox<Payment> cmbPendingPayment;
    @FXML private ComboBox<Client> cmbClient;
    @FXML private ComboBox<String> cmbSearchType;
    @FXML private TextField txtSearchClient;
    @FXML private ComboBox<PayMethods> cmbMetodoPago;

    @FXML private Label lblCoincidence;
    @FXML private Label lblNombreCliente;
    @FXML private Label lblApellidoCliente;
    @FXML private Label lblTelefonoCliente;
    @FXML private Label lblAntenaConectada;
    @FXML private Label lblMesPago;
    @FXML private Label lblMontoPago;
    @FXML private Label lblFechaPago;

    @FXML private Button btnClearSearch;
    @FXML private ImageView imgComprobante;
    @FXML private Button btnSubirComprobante;
    @FXML private Label lblNombreComprobante;

    private List<Client> allClients = List.of();

    private final PaidPaymentUseCase paidPaymentUseCase;
    private final GetClientUseCase getClientUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final AppExecutor appExecutor;

    public PayFormController(
            PaidPaymentUseCase paidPaymentUseCase,
            GetClientUseCase getClientUseCase,
            GetPaymentUseCase getPaymentUseCase,
            AppExecutor appExecutor
    ) {
        this.paidPaymentUseCase = paidPaymentUseCase;
        this.getClientUseCase = getClientUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.appExecutor = appExecutor;
    }

    @FXML
    public void initialize() {

        MouseMove mouseMove = new MouseMove(maincontent);
        mouseMove.start();

        configurarComboBox();

        cmbMetodoPago.getItems().setAll(PayMethods.values());

        txtSearchClient.textProperty().addListener(
                (observable, oldValue, newValue) -> filtrarClientes()
        );

        cmbSearchType.setOnAction(event -> filtrarClientes());

        cmbClient.setOnAction(event ->
                mostrarDatosCliente(cmbClient.getValue())
        );

        cmbPendingPayment.setOnAction(event ->
                mostrarDatosPago(cmbPendingPayment.getValue())
        );

        cargarClientes();
    }

    private void cargarClientes() {

        Task<List<Client>> task = new Task<>() {
            @Override
            protected List<Client> call() throws Exception {
                return getClientUseCase.getAll();
            }
        };

        task.setOnSucceeded(event -> {

            List<Client> clients = task.getValue();

            allClients = clients != null
                    ? clients
                    : List.of();

            filtrarClientes();
        });

        task.setOnFailed(event -> {

            task.getException().printStackTrace();

            mostrarAlerta(
                    "Error",
                    "No se pudieron cargar los clientes."
            );
        });

        appExecutor.execute(task);
    }

    private void filtrarClientes() {

        String query = txtSearchClient.getText() == null
                ? ""
                : txtSearchClient.getText().trim().toLowerCase();

        String criterio = cmbSearchType.getValue();

        List<Client> filteredClients = allClients.stream()
                .filter(client -> coincideCliente(client, criterio, query))
                .toList();

        cmbClient.getItems().setAll(filteredClients);

        lblCoincidence.setText(
                filteredClients.size() + " Coincidencias"
        );

        if (!filteredClients.contains(cmbClient.getValue())) {

            cmbClient.getSelectionModel().clearSelection();

            mostrarDatosCliente(null);
        }
    }

    private boolean coincideCliente(
            Client client,
            String criterio,
            String query
    ) {

        if (query.isEmpty()) {
            return true;
        }

        if (criterio == null || client == null) {
            return false;
        }

        String valor = switch (criterio) {

            case "Nombre" -> client.getFirstName();

            case "Apellido" -> client.getLastName();

            case "Telefono" -> client.getPhone();

            case "Dni" -> client.getDni();

            case "Direccion" -> client.getAddress();

            case "Whatsapp" -> client.getWhatsapp();

            case "Plan" -> client.getPlan() != null
                    ? client.getPlan().getName()
                    : "";

            default -> "";
        };

        return valor != null &&
                valor.toLowerCase().contains(query);
    }

    // =========================================================
    // CONFIGURACIÓN DE COMBOBOX
    // =========================================================

    private void configurarComboBox() {

        cmbClient.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {

                super.updateItem(client, empty);

                setText(empty || client == null
                        ? null
                        : client.getFirstName() + " " + client.getLastName());
            }
        });

        cmbClient.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {

                super.updateItem(client, empty);

                setText(empty || client == null
                        ? "Selecciona un cliente"
                        : client.getFirstName() + " " + client.getLastName());
            }
        });

        cmbPendingPayment.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Payment payment, boolean empty) {

                super.updateItem(payment, empty);

                Client client = cmbClient.getValue();

                setText(empty || payment == null
                        ? null
                        : client != null && client.getPlan() != null
                        ? client.getPlan().getName() + "  S/. " + payment.getAmount()
                        : "Plan no disponible");
            }
        });

        cmbPendingPayment.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Payment payment, boolean empty) {

                super.updateItem(payment, empty);

                Client client = cmbClient.getValue();

                setText(empty || payment == null
                        ? "Selecciona un pago pendiente"
                        : client != null && client.getPlan() != null
                        ? client.getPlan().getName() + "  S/. " + payment.getAmount()
                        : "Plan no disponible");
            }
        });
    }

    // =========================================================
    // INFORMACIÓN DEL CLIENTE
    // =========================================================

    private void mostrarDatosCliente(Client client) {

        if (client == null) {

            limpiarDatosCliente();

            cmbPendingPayment.getItems().clear();

            limpiarDatosPago();

            return;
        }

        lblNombreCliente.setText(client.getFirstName());
        lblApellidoCliente.setText(client.getLastName());
        lblTelefonoCliente.setText(client.getPhone());
        lblAntenaConectada.setText("No disponible");

        cargarPagosPendientes(client);
    }

    // =========================================================
    // PAGOS PENDIENTES
    // =========================================================

    private void cargarPagosPendientes(Client client) {

        cmbPendingPayment.getItems().clear();

        limpiarDatosPago();

        String dni = client.getDni();

        Task<PaymentsScore> task = new Task<>() {
            @Override
            protected PaymentsScore call() throws Exception {
                return getPaymentUseCase.getAll(dni);
            }
        };

        task.setOnSucceeded(event -> {

            // Evitar mostrar resultados de otro cliente
            Client selectedClient = cmbClient.getValue();

            if (selectedClient == null ||
                    !selectedClient.getDni().equals(dni)) {
                return;
            }

            PaymentsScore payments = task.getValue();

            List<Payment> pendingPayments = payments != null
                    ? payments.getPendingPayments()
                    : null;

            if (pendingPayments == null || pendingPayments.isEmpty()) {

                mostrarAlerta(
                        "Información",
                        "Este cliente no tiene pagos pendientes."
                );

                return;
            }

            cmbPendingPayment.getItems().setAll(pendingPayments);

            cmbPendingPayment.getSelectionModel().selectFirst();
        });

        task.setOnFailed(event -> {

            task.getException().printStackTrace();

            mostrarAlerta(
                    "Error",
                    "No se pudieron consultar los pagos del cliente."
            );
        });

        appExecutor.execute(task);
    }

    private void mostrarDatosPago(Payment payment) {

        if (payment == null) {
            limpiarDatosPago();
            return;
        }

        lblMesPago.setText(payment.getDescription());

        lblMontoPago.setText(
                String.format("S/ %.2f", payment.getAmount())
        );

        lblFechaPago.setText(
                payment.getCreateAt().toString()
        );
    }

    // =========================================================
    // LIMPIEZA
    // =========================================================

    private void limpiarDatosCliente() {

        lblNombreCliente.setText("—");
        lblApellidoCliente.setText("—");
        lblTelefonoCliente.setText("—");
        lblAntenaConectada.setText("—");
    }

    private void limpiarDatosPago() {

        lblMesPago.setText("—");
        lblMontoPago.setText("—");
        lblFechaPago.setText("—");
    }

    // =========================================================
    // COMPROBANTE
    // =========================================================

    @FXML
    public void seleccionarComprobante() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar Comprobante de Pago");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos de Imagen",
                        "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {

            lblNombreComprobante.setText(
                    "Comprobante: " + file.getName()
            );

            imgComprobante.setImage(
                    new Image(file.toURI().toString())
            );
        }
    }

    // =========================================================
    // BÚSQUEDA
    // =========================================================

    @FXML
    public void clearSearch() {

        txtSearchClient.clear();

        cmbSearchType.getSelectionModel().selectFirst();

        cmbClient.getSelectionModel().clearSelection();

        cmbPendingPayment.getItems().clear();

        mostrarDatosCliente(null);

        filtrarClientes();
    }

    // =========================================================
    // REGISTRAR PAGO
    // =========================================================

    @FXML
    public void registrarPago() {

        Client client = cmbClient.getValue();

        if (client == null) {

            mostrarAlerta(
                    "Atención",
                    "Selecciona un cliente."
            );

            return;
        }

        Payment selectedPayment = cmbPendingPayment.getValue();

        if (selectedPayment == null) {

            mostrarAlerta(
                    "Atención",
                    "Selecciona el pago que deseas cubrir."
            );

            return;
        }

        PayMethods payMethod = cmbMetodoPago.getValue();

        if (payMethod == null) {

            mostrarAlerta(
                    "Atención",
                    "Selecciona un método de pago."
            );

            return;
        }

        Image comprobante = imgComprobante.getImage();

        if (comprobante == null) {

            mostrarAlerta(
                    "Atención",
                    "Selecciona una imagen como comprobante de pago."
            );

            return;
        }

        // Capturamos los datos antes de ejecutar la tarea
        String dni = client.getDni();

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {

                byte[] imageBytes = ImageConverter.toBytes(comprobante);

                PaidPaymentDto paidDto = new PaidPaymentDto(
                        imageBytes,
                        payMethod
                );

                paidPaymentUseCase.paidPayment(
                        selectedPayment,
                        paidDto
                );

                return null;
            }
        };

        task.setOnSucceeded(event -> {

            mostrarAlerta(
                    "Éxito",
                    "El pago se registró correctamente."
            );

            // Actualizar los pagos pendientes del cliente
            Client selectedClient = cmbClient.getValue();

            if (selectedClient != null &&
                    selectedClient.getDni().equals(dni)) {

                cargarPagosPendientes(selectedClient);
            }

            // Limpiar comprobante
            imgComprobante.setImage(null);

            lblNombreComprobante.setText("Sin comprobante");
        });

        task.setOnFailed(event -> {

            task.getException().printStackTrace();

            mostrarAlerta(
                    "Error",
                    "No se pudo registrar el pago."
            );
        });

        appExecutor.execute(task);
    }

    // =========================================================
    // ALERTAS
    // =========================================================

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert.AlertType type = titulo.equalsIgnoreCase("Error")
                ? Alert.AlertType.ERROR
                : titulo.equalsIgnoreCase("Atención")
                ? Alert.AlertType.WARNING
                : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    // =========================================================
    // CERRAR FORMULARIO
    // =========================================================

    @FXML
    public void closeForm() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirmar salida");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que quieres salir?");

        alert.getButtonTypes().setAll(
                new ButtonType("Sí, salir", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        alert.showAndWait().ifPresent(response -> {

            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {

                Stage stage = (Stage) btnClose.getScene().getWindow();

                stage.close();
            }
        });
    }
}
