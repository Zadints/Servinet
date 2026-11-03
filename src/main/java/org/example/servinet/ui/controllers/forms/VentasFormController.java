package org.example.servinet.ui.controllers.forms;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.api.LocationPicker;
import org.example.servinet.ui.dto.GeoLocationDto;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VentasFormController{


    /**
     *   Dynamic text that can be changed according
     *   to user preference, including for the addition
     *   of advertisements.
     **/
    @FXML private Label sellSlogan;
    private final String[] slogans = {
            "Registra un cliente y gana puntos para tu comisión.",
            "Registra un nuevo cliente y configura el contrato de servicio de internet."
    };

    @FXML private ComboBox<Antenna> cmbAntenna;
    @FXML private Button btnCreateClient;
    @FXML private  TextField txtWhatsapp;
    @FXML private ComboBox<ClientPlan> cmbPlan;

    @FXML private Button btnSelectMap;

    @FXML private  Button btnClose;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDni;
    @FXML private TextField txtDireccion;
    @FXML private Label txtCoordinates;
    @FXML private TextField txtEmail;
    @FXML private AnchorPane mainSell;

    @FXML private Label lblSummaryClient;
    @FXML private Label lblSummaryPlan;
    @FXML private Label lblSummaryAmount;
    @FXML private Label lblSummaryDni;

    private final PlanUseCase planUseCase;
    private final GetAntennaUseCase getAntennaUseCase;
    private final Navigator navigator;
    private final ConsultDniUseCase consultDniUseCase;
    private final AppExecutor appExecutor;
    private final CreateClientUseCase createClientUseCase;
    private ClientPlan planOptional;
    private final LocationPicker locationPicker;
    private GeoLocationDto selectedLocation;

    public VentasFormController(PlanUseCase planUseCase, GetAntennaUseCase getAntennaUseCase, Navigator navigator, ConsultDniUseCase consultDniUseCase, AppExecutor appExecutor, CreateClientUseCase createClientUseCase, LocationPicker locationPicker) {
        this.planUseCase = planUseCase;
        this.getAntennaUseCase = getAntennaUseCase;
        this.navigator = navigator;
        this.consultDniUseCase = consultDniUseCase;
        this.appExecutor = appExecutor;
        this.createClientUseCase = createClientUseCase;
        this.locationPicker = locationPicker;
    }

    public void setPlanOptional(ClientPlan planOptional) {

        this.planOptional = planOptional;

        if (cmbPlan != null && planOptional != null) {
            cmbPlan.getSelectionModel().select(planOptional);
            eventStringPlan();
        }
    }

    @FXML
    public void initialize() {

        MouseMove mouseMove = new MouseMove(mainSell);
        mouseMove.start();

        loadAllDataWindows();
        registerDniEventClient();
        registerPlanEventClient();
        registerPhoneEvent();

    }

    /**
     * methods that call external APIs via an event
     **/
    private void registerDniEventClient(){
        txtDni.textProperty().addListener(
                (observable, oldValue, newValue) -> {

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

                    if (dto.getDni().isEmpty()) {
                        txtNombre.setText("");
                        txtApellido.setText("");
                        lblSummaryClient.setText("Sin datos del cliente");
                        lblSummaryDni.setText("Sin datos del cliente");
                        return;
                    }

                    txtNombre.setText(dto.getNombres());
                    txtApellido.setText(dto.getApellidos());
                    lblSummaryClient.setText(dto.getFullName());
                    lblSummaryDni.setText(dto.getDni());

                });

                appExecutor.execute(task);
            }
        });
    }
    private void registerPlanEventClient(){
        cmbPlan.valueProperty().addListener(
                (obs, oldValue, newValue) -> eventStringPlan()
        );
    }
    private void eventStringPlan(){
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
    private void registerPhoneEvent() {
        txtTelefono.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue.length() == 9
                            && txtWhatsapp.getText().isBlank()) {

                        txtWhatsapp.setText(newValue);
                    }
                });
    }
    private void loadAllDataWindows() {
        CompletableFuture<List<ClientPlan>> plansFuture =
                CompletableFuture.supplyAsync(
                        planUseCase::getAll,
                        appExecutor
                );

        CompletableFuture<List<Antenna>> antennasFuture =
                CompletableFuture.supplyAsync(
                        getAntennaUseCase::getAll,
                        appExecutor
                );
        CompletableFuture.allOf(plansFuture, antennasFuture)
                .thenRun(() -> {

                    List<ClientPlan> plans = plansFuture.join();
                    List<Antenna> antennas = antennasFuture.join();

                    Platform.runLater(() -> {
                        cmbPlan.getItems().setAll(plans);
                        cmbAntenna.getItems().setAll(antennas);
                    });

                });
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
        String nombre   = txtNombre.getText();
        String apellido = txtApellido.getText();
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

        ClientDto dto = new ClientDto(
                dni,
                nombre,
                apellido,
                telefono,
                direccion,
                email,
                whatsapp,
                planSeleccionado,
                antenna.getUuid(),
                selectedLocation.latitude(),
                selectedLocation.longitude()
        );



        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call(){
                createClientUseCase.addClientOrder(dto);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            closeForm();
        });

        task.setOnFailed(event -> {

            Throwable error = task.getException();

            error.printStackTrace();
            System.out.println(error.getMessage() + error.getCause());

            showError(error.getMessage(),
                    "No se pudo crear el usuario",
                    (Stage) btnCreateClient.getScene().getWindow());
        });

        appExecutor.execute(task);

    }

    private void showError(String msg, String title , Stage stage){
        try {
            navigator.showError(
                    msg,
                    title,
                    stage
            );
        }catch (IOException e){
            System.out.println(e.getMessage() + e.getCause());
        }
    }



    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msj) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }



    @FXML
    private void openLocationMap() {

        selectedLocation = locationPicker.show(
                btnSelectMap.getScene().getWindow()
        );

        if (selectedLocation == null) {
            System.out.println("No se seleccionó ninguna ubicación.");
            return;
        }

        txtCoordinates.setText(
                "Latitud: " + selectedLocation.latitude() +
                        " Longitud: " + selectedLocation.longitude()
        );


    }
}