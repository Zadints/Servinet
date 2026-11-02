package org.example.servinet.ui.controllers.forms;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.application.usecase.api.ConsultDniUseCase;
import org.example.servinet.application.usecase.clients.CreateClientUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;

public class VentasFormController {

    public ComboBox<Antenna> cmbAntenna;
    public Button btnCreateClient;
    @FXML private  TextField txtWhatsapp;
    @FXML private ComboBox<ClientPlan> cmbPlan;

    @FXML private  Button btnClose;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDni;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtEmail;
    @FXML private AnchorPane mainSell;

    @FXML private Label lblSummaryClient;
    @FXML private Label lblSummaryPlan;
    @FXML private Label lblSummaryAmount;

    private final PlanUseCase planUseCase;
    private final GetAntennaUseCase getAntennaUseCase;
    private final Navigator navigator;
    private final ConsultDniUseCase consultDniUseCase;
    private final AppExecutor appExecutor;
    private final CreateClientUseCase createClientUseCase;
    private ClientPlan planOptional;


    public VentasFormController(PlanUseCase planUseCase, GetAntennaUseCase getAntennaUseCase, Navigator navigator, ConsultDniUseCase consultDniUseCase, AppExecutor appExecutor, CreateClientUseCase createClientUseCase) {
        this.planUseCase = planUseCase;
        this.getAntennaUseCase = getAntennaUseCase;
        this.navigator = navigator;
        this.consultDniUseCase = consultDniUseCase;
        this.appExecutor = appExecutor;
        this.createClientUseCase = createClientUseCase;
    }

    public void setPlanOptional(ClientPlan planOptional) {

        this.planOptional = planOptional;

        if (cmbPlan != null && planOptional != null) {
            cmbPlan.getSelectionModel().select(planOptional);
            updateResum();
        }
    }

    @FXML
    public void initialize() {

        MouseMove mouseMove = new MouseMove(mainSell);
        mouseMove.start();

        cargarDatosVentana();

        txtNombre.textProperty().addListener((obs, oldValue, newValue) -> updateResum());
        txtApellido.textProperty().addListener((obs, oldValue, newValue) -> updateResum());
        txtDni.textProperty().addListener((obs, oldValue, newValue) -> updateResum());

        cmbPlan.valueProperty().addListener(
                (obs, oldValue, newValue) -> UpdatePlans()
        );

        txtDni.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.length() == 8) {

                Task<DniDataDto> task = new Task<>() {
                    @Override
                    protected DniDataDto call() throws Exception {
                        return consultDniUseCase.execute(newValue);
                    }
                };

                task.setOnSucceeded(event -> {

                    DniDataDto dto = task.getValue();

                    if (dto == null) return;

                    txtNombre.setText(dto.getNombres());
                    txtApellido.setText(
                            dto.getApellidoMaterno() + " " + dto.getApellidoPaterno()
                    );
                });

                appExecutor.execute(task);
            }
        });

        updateResum();
        UpdatePlans();
    }

    private void updateResum() {

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String dni = txtDni.getText().trim();

        String cliente = (nombre + " " + apellido).trim();

        if (cliente.isEmpty()) {
            cliente = "Sin datos del cliente";
        }

        if (!dni.isEmpty()) {
            cliente += " · DNI: " + dni;
        }

        lblSummaryClient.setText(cliente);

    }

    private void cargarDatosVentana() {
        try {
            cmbPlan.getItems().setAll(planUseCase.getAll());
            cmbAntenna.getItems().setAll(getAntennaUseCase.getAll());

            if (planOptional != null){
                cmbPlan.getSelectionModel().select(planOptional);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
        }



    }

    private void UpdatePlans() {

        ClientPlan plan = cmbPlan.getValue();

        if (plan == null) {
            lblSummaryPlan.setText("Sin plan seleccionado");
            lblSummaryAmount.setText("S/ 0.00");
            return;
        }

        lblSummaryPlan.setText(plan.getName());

        lblSummaryAmount.setText(
                "S/ " + String.format("%.2f", plan.getPrice())
        );
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
                Stage stage = (Stage) btnClose.getScene().getWindow();
                stage.close();
            }
        });
    }

    @FXML
    public void registerSale() {
        String nombre   = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String dni      = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String whatsapp = txtWhatsapp.getText().trim();
        String email    = txtEmail.getText().trim();
        String direccion    = txtDireccion.getText().trim();

        if (cmbAntenna.getValue() == null) {
            try {
                navigator.showError(
                        "Debes seleccionar un rol",
                        "Error al registrar usuario",
                        (Stage) btnCreateClient.getScene().getWindow());
            }catch (IOException e){
                System.out.println("Debes seleccionar un rol" + e.getMessage());
            }

            return;
        }

        if (cmbPlan.getValue() == null) {
            try {
                navigator.showError(
                        "Debes seleccionar un rol",
                        "Error al registrar usuario",
                        (Stage) btnCreateClient.getScene().getWindow());
            }catch (IOException e){
                System.out.println("Debes seleccionar un rol" + e.getMessage());
            }
            return;
        }
        ClientPlan planSeleccionado = cmbPlan.getValue();
        Antenna antenna = cmbAntenna.getValue();

        try {

            ClientDto dto = new ClientDto(
                dni,
                nombre,
                apellido,
                telefono,
                direccion,
                email,
                whatsapp,
                planSeleccionado,
                antenna.getUuid()
            );

            createClientUseCase.addClientOrder(dto);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un fallo al procesar la venta: " + e.getMessage());
        }
        closeForm();
    }



    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msj) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}