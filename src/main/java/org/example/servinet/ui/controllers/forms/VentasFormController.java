package org.example.servinet.ui.controllers.forms;

import javafx.application.Platform;
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
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.infrastructure.api.LocationPicker;
import org.example.servinet.ui.dto.GeoLocationDto;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
    private List<Antenna> availableAntennas = new ArrayList<>();

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

        configureAntennaComboBox();

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
                        availableAntennas = antennas;
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
            return;
        }

        txtCoordinates.setText(
                "Latitud: " + selectedLocation.latitude() +
                        " Longitud: " + selectedLocation.longitude()
        );

        Antenna nearest = findNearestAntenna();

        if (nearest != null) {
            cmbAntenna.setValue(nearest);
        }

        cmbAntenna.setCellFactory(cmbAntenna.getCellFactory());
    }

    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        final int EARTH_RADIUS = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private Antenna findNearestAntenna() {

        if (selectedLocation == null || availableAntennas.isEmpty()) {
            return null;
        }

        return availableAntennas.stream()
                .min(Comparator.comparingDouble(antenna ->
                        calculateDistance(
                                selectedLocation.latitude(),
                                selectedLocation.longitude(),
                                antenna.getLatitude(),
                                antenna.getLongitude()
                        )
                ))
                .orElse(null);
    }

    private void configureAntennaComboBox() {

        cmbAntenna.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Antenna antenna, boolean empty) {
                super.updateItem(antenna, empty);

                if (empty || antenna == null) {
                    setText(null);
                    return;
                }

                if (selectedLocation == null) {
                    setText(antenna.getName());
                    return;
                }

                double distance = calculateDistance(
                        selectedLocation.latitude(),
                        selectedLocation.longitude(),
                        antenna.getLatitude(),
                        antenna.getLongitude()
                );

                Antenna nearest = findNearestAntenna();

                String recommendation = antenna.equals(nearest)
                        ? " ⭐ Recomendada"
                        : "";

                setText(String.format(
                        "%s - %.2f km%s",
                        antenna.getName(),
                        distance,
                        recommendation
                ));
            }
        });

        cmbAntenna.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Antenna antenna, boolean empty) {
                super.updateItem(antenna, empty);

                if (empty || antenna == null) {
                    setText(null);
                    return;
                }

                setText(antenna.getName());
            }
        });
    }
}