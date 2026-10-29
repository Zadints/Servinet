package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.payments.CreatePaymentUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.entities.Payment;
import org.example.servinet.ui.controllers.IndexController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class VentasFormController {

    private IndexController indexController;

    // --- Controles FXML ---
    @FXML private ComboBox<ClientPlan> cmbPlan; // Corregido a ClientPlan
    @FXML private ComboBox<String> cmbMetodoPago;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDni;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtEmail;

    @FXML private TextArea txtObservacion;
    @FXML private Label lblNombreComprobante;
    @FXML private ImageView imgComprobante;

    @FXML private Label lblResumenCliente;
    @FXML private Label lblResumenPlan;
    @FXML private Label lblResumenMonto;

    private File selectedComprobanteFile;
    private final PlanUseCase planUseCase;
    private final CreatePaymentUseCase createPaymentUseCase;
    public VentasFormController(PlanUseCase planUseCase, CreatePaymentUseCase createPaymentUseCase) {
        this.planUseCase = planUseCase;
        this.createPaymentUseCase = createPaymentUseCase;
    }

    // ¡IMPRESCINDIBLE LA ANOTACIÓN @FXML!
    @FXML
    public void initialize() {
        System.out.println(">>> INICIALIZANDO VENTASFORMCONTROLLER <<<");
        cargarDatosVentana();
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
        // Respaldo directo por si el FXMLLoader no invocó initialize()
        cargarDatosVentana();
    }

    private void cargarDatosVentana() {
        // 1. Cargar planes desde el pool hacia el ComboBox
        // PRUEBA SIMPLE: Mostrar un mensaje cuando selecciones un plan
        if (cmbPlan != null) {
            cmbPlan.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Plan Seleccionado",
                            "Elegiste: " + newVal.getName() + " - S/ " + newVal.getPrice()
                    );
                }
            });
        }
        if (cmbPlan != null) {
            cmbPlan.getItems().setAll(planUseCase.getPool());

            // Forzar celdas limpias y evitar el apilamiento de estilos CSS
            cmbPlan.setCellFactory(param -> new ListCell<ClientPlan>() {
                @Override
                protected void updateItem(ClientPlan item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Muestra una sola línea ordenada: Nombre, velocidad y precio
                        setText(item.getName() + " - " + item.getSpeed() + " Mbps (S/ " + String.format("%.2f", item.getPrice()) + ")");
                    }
                }
            });

            cmbPlan.setButtonCell(new ListCell<ClientPlan>() {
                @Override
                protected void updateItem(ClientPlan item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Seleccionar plan");
                    } else {
                        setText(item.getName() + " - " + item.getSpeed() + " Mbps (S/ " + String.format("%.2f", item.getPrice()) + ")");
                    }
                }
            });
        }

        // 2. Poblar Métodos de Pago (si está vacío)
        if (cmbMetodoPago != null && cmbMetodoPago.getItems().isEmpty()) {
            cmbMetodoPago.getItems().addAll("EFECTIVO", "YAPE", "PLIN", "TRANSFERENCIA");
        }

        // 3. Restricción DNI (solo 8 números)
        if (txtDni != null) {
            txtDni.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (!newValue.matches("\\d*") || newValue.length() > 8) {
                        txtDni.setText(oldValue == null ? "" : oldValue);
                    }
                }
            });
        }

        // 4. Listener para nombre en el panel lateral
        if (txtNombre != null) {
            txtNombre.textProperty().addListener((obs, oldVal, newVal) -> actualizarResumen());
        }

        // 5. Listener para actualizar el resumen cuando cambie el plan seleccionado
        if (cmbPlan != null) {
            cmbPlan.valueProperty().addListener((obs, oldVal, newVal) -> actualizarResumen());
        }
    }

    private void actualizarResumen() {
        if (lblResumenCliente != null && txtNombre != null) {
            lblResumenCliente.setText(txtNombre.getText().trim().isEmpty() ? "Sin seleccionar" : txtNombre.getText().trim());
        }

        if (cmbPlan != null && cmbPlan.getValue() != null) {
            ClientPlan planSel = cmbPlan.getValue();

            if (lblResumenPlan != null) {
                lblResumenPlan.setText(planSel.getName());
            }

            if (lblResumenMonto != null) {
                Double precio = planSel.getPrice() != null ? planSel.getPrice() : 0.0;
                lblResumenMonto.setText(String.format("S/ %.2f", precio));
            }
        }
    }

    @FXML
    public void seleccionarComprobante() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante de Pago");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            this.selectedComprobanteFile = file;

            if (lblNombreComprobante != null) {
                lblNombreComprobante.setText("Comprobante: " + file.getName());
            }

            if (imgComprobante != null) {
                imgComprobante.setImage(new Image(file.toURI().toString()));
            }
        }
    }

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
                if (indexController != null) {
                    indexController.renderizarFxml("sell.fxml");
                }
            }
        });
    }

    @FXML
    public void cancelForm() {
        if (indexController != null) {
            indexController.renderizarFxml("sell.fxml");
        }
    }

    @FXML
    public void registerSale() {
        System.out.println(">>> REGISTRANDO VENTA <<<");

        String nombre = (txtNombre != null && txtNombre.getText() != null) ? txtNombre.getText().trim() : "";
        String apellido = (txtApellido != null && txtApellido.getText() != null) ? txtApellido.getText().trim() : "";
        String dni = (txtDni != null && txtDni.getText() != null) ? txtDni.getText().trim() : "";
        String telefono = (txtTelefono != null && txtTelefono.getText() != null) ? txtTelefono.getText().trim() : "";
        String email = (txtEmail != null && txtEmail.getText() != null) ? txtEmail.getText().trim() : "";

        // Validaciones
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Ingresa el nombre del cliente.");
            txtNombre.requestFocus();
            return;
        }

        if (apellido.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Ingresa el apellido del cliente.");
            txtApellido.requestFocus();
            return;
        }

        if (dni.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Ingresa el DNI del cliente.");
            txtDni.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Ingresa el número de teléfono.");
            txtTelefono.requestFocus();
            return;
        }

        if (cmbPlan == null || cmbPlan.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Debes seleccionar un plan de internet.");
            cmbPlan.requestFocus();
            return;
        }

        if (cmbMetodoPago == null || cmbMetodoPago.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Debes seleccionar un método de pago.");
            cmbMetodoPago.requestFocus();
            return;
        }

        if (selectedComprobanteFile == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Comprobante Faltante", "Debes adjuntar la imagen del comprobante.");
            return;
        }

        if (!dni.matches("\\d{8}")) {
            mostrarAlerta(Alert.AlertType.WARNING, "DNI Inválido", "El DNI debe tener exactamente 8 dígitos.");
            txtDni.requestFocus();
            return;
        }

        if (!telefono.matches("\\d{7,9}")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Teléfono Inválido", "El teléfono debe tener entre 7 y 9 dígitos.");
            txtTelefono.requestFocus();
            return;
        }

        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo Inválido", "El formato de correo es incorrecto.");
            txtEmail.requestFocus();
            return;
        }

        try {
            byte[] imageBytes = convertirArchivoABytes(selectedComprobanteFile);
            ClientPlan planSeleccionado = cmbPlan.getValue();
            BigDecimal monto = BigDecimal.valueOf(planSeleccionado.getPrice() != null ? planSeleccionado.getPrice() : 0.0);
            String metodo = cmbMetodoPago.getValue();

            /*Payment paymentRegistrado = createPaymentUseCase.createNewPay(
                    //pasar aqui el clietne seleccionado
            );

            if (paymentRegistrado != null) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El contrato y el pago fueron registrados.");
                if (indexController != null) {
                    indexController.renderizarFxml("sell.fxml");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el pago en la base de datos.");
            }
*/
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un fallo al procesar la venta: " + e.getMessage());
        }
    }

    private byte[] convertirArchivoABytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return buffer;
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msj) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}