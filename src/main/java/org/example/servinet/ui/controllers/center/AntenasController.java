package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.servinet.application.usecase.antenna.DeleteAntennaUseCase;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.application.usecase.antenna.MaintenanceAntennaUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.ui.animations.LoadingAnimation;
import org.example.servinet.ui.animations.SlideUpAnimation;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AntennaCarruselController;

import java.io.IOException;
import java.util.*;

import javafx.scene.layout.VBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.controllers.forms.AntennaMaintenanceController;
import org.example.servinet.ui.controllers.forms.AntennasEditController;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.navigator.Navigator;

public class AntenasController {
    @FXML public Label lblForMaintenance;
    @FXML private Label lblActiveAntennas;
    @FXML private Label lblMaintenanceAntennas;
    @FXML private Label lblInactiveAntennas;
    @FXML private Label lblConnectedClients;
    @FXML private Label lblSelectedAntenna;
    @FXML private ImageView imgSelectedAntenna;
    @FXML private Label lblSelectedStatus;
    @FXML private Label lblSelectedClients;
    @FXML private Label lblSelectedLocation;
    @FXML private IndexController indexController;
    @FXML private HBox antennaList;
    @FXML private VBox sectionResumenAntenas;
    @FXML private VBox sectionCarruselAntenas;
    @FXML private VBox sectionDetalleAntena;
    @FXML private Button btnCreateAntenna;
    @FXML private Button btnStartMaintenance;
    @FXML private Button btnFinishMaintenance;
    @FXML private Button btnEditAntenna;
    @FXML private Button btnDeleteAntenna;
    @FXML private StackPane loadingOverlay;
    @FXML private Circle loadingCircle;
    @FXML private Label lblLoading;

    private boolean puedeVerCarrusel;
    private Antenna currentViewAntenna;
    private Task<AntennasData> loadTask;
    private final Map<String, Integer> connectionsCache = new HashMap<>();

    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetAntennaUseCase getAntennaUseCase;
    private final LoadingAnimation loadingAnimation;
    private final SlideUpAnimation slideUpAnimation;
    private final DeleteAntennaUseCase deleteAntennaUseCase;
    private final MaintenanceAntennaUseCase maintenanceAntennaUseCase;
    private final GetClientUseCase getClientUseCase;

    public AntenasController(
            AppExecutor appExecutor,
            Navigator navigator,
            AuthorizationSecurity authorizationService,
            GetAntennaUseCase getAntennaUseCase,
            LoadingAnimation loadingAnimation,
            SlideUpAnimation slideUpAnimation,
            DeleteAntennaUseCase deleteAntennaUseCase,
            MaintenanceAntennaUseCase maintenanceAntennaUseCase,
            GetClientUseCase getClientUseCase
    ) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
        this.getAntennaUseCase = getAntennaUseCase;
        this.loadingAnimation = loadingAnimation;
        this.slideUpAnimation = slideUpAnimation;
        this.deleteAntennaUseCase = deleteAntennaUseCase;
        this.maintenanceAntennaUseCase = maintenanceAntennaUseCase;
        this.getClientUseCase = getClientUseCase;
    }

    @FXML
    private void initialize() {
        boolean puedeVerInformacion = authorizationService.hasPermission(Permission.ANT_VIEW_INFO);
        boolean puedeCrear = authorizationService.hasPermission(Permission.ANT_CREATE);
        boolean puedeEditar = authorizationService.hasPermission(Permission.ANT_EDIT);
        boolean puedeEliminar = authorizationService.hasPermission(Permission.ANT_DELETE);
        boolean puedeIniciarMantenimiento = authorizationService.hasPermission(Permission.ANT_START_MAINTE);
        boolean puedeFinalizarMantenimiento = authorizationService.hasPermission(Permission.ANT_END_MAINT);
        boolean puedeCambiarEstado = authorizationService.hasPermission(
                Permission.ANT_GO_ACTIVE, Permission.ANT_GO_DESACTIVE);

        boolean tieneAccionesSobreAntena = puedeEditar || puedeEliminar
                || puedeIniciarMantenimiento || puedeFinalizarMantenimiento || puedeCambiarEstado;

        puedeVerCarrusel = authorizationService.hasPermission(
                Permission.ANT_VIEW_ANTENNAS, Permission.ANT_VIEW_INFO)
                || tieneAccionesSobreAntena;

        mostrar(btnCreateAntenna, puedeCrear);
        mostrar(sectionResumenAntenas, puedeVerInformacion);
        mostrar(sectionCarruselAntenas, puedeVerCarrusel);
        mostrar(sectionDetalleAntena, puedeVerInformacion || tieneAccionesSobreAntena);
        mostrar(btnStartMaintenance, puedeIniciarMantenimiento);
        mostrar(btnFinishMaintenance, puedeFinalizarMantenimiento);
        mostrar(btnEditAntenna, puedeEditar);
        mostrar(btnDeleteAntenna, puedeEliminar);

        if (puedeVerCarrusel) loadAntennas();
    }

    private void mostrar(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private boolean exigirPermiso(Permission permiso) {
        if (authorizationService.hasPermission(permiso)) return true;
        mostrarError("Acceso denegado", "Tu rol no permite realizar esta acción.");
        return false;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }

    private record AntennasData(List<Antenna> antennas, Map<String, Integer> connections, int totalConnected) {}

    private void loadAntennas() {
        if (!puedeVerCarrusel) return;
        if (loadTask != null && loadTask.isRunning()) return;

        loadingAnimation.onAnimation(loadingOverlay, loadingCircle, lblLoading);
        Task<AntennasData> task = new Task<>() {
            @Override
            protected AntennasData call() {
                List<Antenna> antennas = getAntennaUseCase.getAll();
                if (antennas == null) antennas = List.of();

                Map<String, Integer> counts = new HashMap<>();
                for (Antenna antenna : antennas) {
                    if (isCancelled()) return null;
                    if (antenna == null || antenna.getUuid() == null) continue;
                    counts.put(antenna.getUuid(),
                            getClientUseCase.getConnectedClientsAntenna(antenna.getUuid()));
                }

                if (isCancelled()) return null;
                int total = getClientUseCase.getAllClientConectedAntenna();
                return new AntennasData(new ArrayList<>(antennas), counts, total);
            }
        };
        loadTask = task;

        task.setOnSucceeded(event -> {
            loadingAnimation.finishAnimation();
            AntennasData data = task.getValue();
            if (data == null) return;
            connectionsCache.clear();
            connectionsCache.putAll(data.connections());
            lblConnectedClients.setText(String.valueOf(data.totalConnected()));
            antennaList.getChildren().clear();
            currentViewAntenna = null;
            clearDetails();

            int active = 0, maintenance = 0, inactive = 0;
            for (Antenna antenna : data.antennas()) {
                if (antenna == null) continue;
                if (antenna.getStatus() == StatusAntenna.ACTIVE) active++;
                else if (antenna.getStatus() == StatusAntenna.MAINTENANCE) maintenance++;
                else if (antenna.getStatus() == StatusAntenna.OFFLINE) inactive++;
                renderingAntennas(antenna);
            }
            lblActiveAntennas.setText(String.valueOf(active));
            lblMaintenanceAntennas.setText(String.valueOf(maintenance));
            lblInactiveAntennas.setText(String.valueOf(inactive));
        });
        task.setOnFailed(event -> {
            loadingAnimation.finishAnimation();
            mostrarError("Error al cargar antenas", errorMessage(task.getException()));
        });
        appExecutor.execute(task);
    }

    private String errorMessage(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null
                ? "Ocurrió un error inesperado." : throwable.getMessage();
    }

    private void clearDetails() {
        lblSelectedAntenna.setText("-");
        lblSelectedStatus.setText("-");
        lblSelectedClients.setText("-");
        lblSelectedLocation.setText("-");
        lblForMaintenance.setText("-");
        imgSelectedAntenna.setImage(null);
    }

    private void renderingAntennas(Antenna antenna) {
        try {
            AntennaCarruselController con = navigator.loadComponent(
                    "/org/example/servinet/components/antena-carrusel.fxml",
                    antennaList.getChildren()::add);
            Image image = ImageConverter.toImage(antenna.getImage());
            con.setAntenna(antenna);
            con.setImgAntenna1(image);
            con.setLblAntennaLocate("[" + antenna.getLatitude() + " , " + antenna.getLongitude() + "]");
            con.setLblAntennaName(antenna.getName());
            con.setLblAntennaStatus(String.valueOf(antenna.getStatus()));
            con.setLblClientsAntenna(String.valueOf(connectionsCache.getOrDefault(antenna.getUuid(), 0)));
            con.setOnAntennaSelected(this::selectAntenna);
        } catch (IOException e) {
            mostrarError("Error al mostrar antena", errorMessage(e));
        }
    }

    private void selectAntenna(Antenna antenna) {
        currentViewAntenna = antenna;
        lblSelectedAntenna.setText(antenna.getName());
        imgSelectedAntenna.setImage(ImageConverter.toImage(antenna.getImage()));
        lblSelectedStatus.setText(String.valueOf(antenna.getStatus()));
        lblSelectedLocation.setText(antenna.getLatitude() + " , " + antenna.getLongitude());
        lblForMaintenance.setText(String.valueOf(antenna.isForMaintenance()));
        lblSelectedClients.setText(String.valueOf(connectionsCache.getOrDefault(antenna.getUuid(), 0)));

        boolean inMaintenance = antenna.getStatus() == StatusAntenna.MAINTENANCE;
        mostrar(btnFinishMaintenance, inMaintenance && authorizationService.hasPermission(Permission.ANT_END_MAINT));
        mostrar(btnStartMaintenance, !inMaintenance && authorizationService.hasPermission(Permission.ANT_START_MAINTE));
    }

    @FXML
    protected void deleteAntenna() {
        if (!exigirPermiso(Permission.ANT_DELETE)) return;
        if (currentViewAntenna == null) {
            mostrarError("Eliminar antena", "Selecciona una antena.");
            return;
        }
        Antenna target = currentViewAntenna;
        loadingAnimation.onAnimation(loadingOverlay, loadingCircle, lblLoading);
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                deleteAntennaUseCase.deleteAntenna(target);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            loadingAnimation.finishAnimation();
            loadAntennas();
        });
        task.setOnFailed(e -> {
            loadingAnimation.finishAnimation();
            mostrarError("Error al eliminar", errorMessage(task.getException()));
        });
        appExecutor.execute(task);
    }

    @FXML
    protected void editAntenna() {
        if (!exigirPermiso(Permission.ANT_EDIT)) return;
        if (currentViewAntenna == null) {
            mostrarError("Editar antena", "Selecciona una antena.");
            return;
        }
        try {
            navigator.<AntennasEditController>openWindow(
                    "/org/example/servinet/forms/antennas-edit.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(), 0.30, 0.50,
                    controller -> controller.setAntenna(currentViewAntenna));
            loadAntennas();
        } catch (Exception e) {
            mostrarError("Error al editar", errorMessage(e));
        }
    }

    @FXML
    protected void finishMaintenance() {
        if (!exigirPermiso(Permission.ANT_END_MAINT)) return;
        if (currentViewAntenna == null) {
            mostrarError("Mantenimiento", "Selecciona una antena.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finalizar mantenimiento");
        alert.setHeaderText("¿Estás seguro de finalizar el mantenimiento?");
        alert.setContentText("Esta acción marcará el mantenimiento como finalizado.");
        ButtonType confirm = new ButtonType("Finalizar");
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirm, cancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != confirm) return;

        Antenna target = currentViewAntenna;
        loadingAnimation.onAnimation(loadingOverlay, loadingCircle, lblLoading);
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                maintenanceAntennaUseCase.off(target);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            loadingAnimation.finishAnimation();
            loadAntennas();
        });
        task.setOnFailed(e -> {
            loadingAnimation.finishAnimation();
            mostrarError("Error al finalizar mantenimiento", errorMessage(task.getException()));
        });
        appExecutor.execute(task);
    }

    @FXML
    protected void startMaintenance() {
        if (!exigirPermiso(Permission.ANT_START_MAINTE)) return;
        if (currentViewAntenna == null) {
            mostrarError("Mantenimiento", "Selecciona una antena.");
            return;
        }
        try {
            navigator.<AntennaMaintenanceController>openWindow(
                    "/org/example/servinet/forms/antenna-manteinance.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(), 0.49, 0.36,
                    controller -> controller.setAntenna(currentViewAntenna));
            loadAntennas();
        } catch (Exception e) {
            mostrarError("Error al iniciar mantenimiento", errorMessage(e));
        }
    }

    @FXML
    protected void createAntenna() {
        if (!exigirPermiso(Permission.ANT_CREATE)) return;
        try {
            navigator.awaitOpenWindow(
                    "/org/example/servinet/forms/antenna-create.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(), 0.70, 0.70);
            loadAntennas();
        } catch (Exception e) {
            mostrarError("Error al crear antena", errorMessage(e));
        }
    }

    @FXML protected void previousAntenna() { }
    @FXML protected void nextAntenna() { }
}
