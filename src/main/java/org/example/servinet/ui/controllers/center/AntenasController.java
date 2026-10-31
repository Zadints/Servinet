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
import java.util.List;
import java.util.Optional;

import javafx.scene.layout.VBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.controllers.forms.AntennaMaintenanceController;
import org.example.servinet.ui.controllers.forms.AntennasEditController;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.navigator.Navigator;

public class AntenasController {

    public Label lblForMaintenance;
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
    /**
     *  Atributos de animacion fxml
     **/
    @FXML private StackPane loadingOverlay;
    @FXML private Circle loadingCircle;
    @FXML private Label lblLoading;

    private boolean puedeVerCarrusel;
    private Antenna currentViewAntenna;

    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetAntennaUseCase getAntennaUseCase;
    private final LoadingAnimation loadingAnimation;
    private final SlideUpAnimation slideUpAnimation;
    private final DeleteAntennaUseCase deleteAntennaUseCase;
    private final MaintenanceAntennaUseCase maintenanceAntennaUseCase;
    private final GetClientUseCase getClientUseCase;
    public AntenasController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService, GetAntennaUseCase getAntennaUseCase, LoadingAnimation loadingAnimation, SlideUpAnimation slideUpAnimation, DeleteAntennaUseCase deleteAntennaUseCase, MaintenanceAntennaUseCase maintenanceAntennaUseCase, GetClientUseCase getClientUseCase) {
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

    @FXML private void initialize() {


        boolean puedeVerInformacion = authorizationService.hasPermission(
                Permission.ANT_VIEW_INFO
        );

        boolean puedeCrear = authorizationService.hasPermission(
                Permission.ANT_CREATE
        );

        boolean puedeEditar = authorizationService.hasPermission(
                Permission.ANT_EDIT
        );

        boolean puedeEliminar = authorizationService.hasPermission(
                Permission.ANT_DELETE
        );

        boolean puedeIniciarMantenimiento = authorizationService.hasPermission(
                Permission.ANT_START_MAINTE
        );

        boolean puedeFinalizarMantenimiento = authorizationService.hasPermission(
                Permission.ANT_END_MAINT
        );

        boolean puedeCambiarEstado = authorizationService.hasPermission(
                Permission.ANT_GO_ACTIVE,
                Permission.ANT_GO_DESACTIVE
        );

        boolean tieneAccionesSobreAntena =
                puedeEditar
                        || puedeEliminar
                        || puedeIniciarMantenimiento
                        || puedeFinalizarMantenimiento
                        || puedeCambiarEstado;


        puedeVerCarrusel = authorizationService.hasPermission(
                Permission.ANT_VIEW_ANTENNAS,
                Permission.ANT_VIEW_INFO
        ) || tieneAccionesSobreAntena;

        mostrar(btnCreateAntenna, puedeCrear);

        mostrar(
                sectionResumenAntenas,
                puedeVerInformacion
        );

        mostrar(
                sectionCarruselAntenas,
                puedeVerCarrusel
        );

        mostrar(
                sectionDetalleAntena,
                puedeVerInformacion || tieneAccionesSobreAntena
        );

        mostrar(
                btnStartMaintenance,
                puedeIniciarMantenimiento
        );

        mostrar(
                btnFinishMaintenance,
                puedeFinalizarMantenimiento
        );

        mostrar(btnEditAntenna, puedeEditar);
        mostrar(btnDeleteAntenna, puedeEliminar);

        if (puedeVerCarrusel) {
            loadAntennas();
        }
    }


    private void mostrar(Node componente, boolean permitido) {

        if (componente == null) {
            return;
        }

        componente.setVisible(permitido);
        componente.setManaged(permitido);
    }

    private boolean exigirPermiso(Permission permiso) {

        if (authorizationService.hasPermission(permiso)) {
            return true;
        }

        Alert alerta = new Alert(Alert.AlertType.WARNING);

        alerta.setTitle("Acceso denegado");
        alerta.setHeaderText("Permiso insuficiente");
        alerta.setContentText(
                "Tu rol no permite realizar esta acción."
        );

        alerta.showAndWait();

        return false;
    }

    @FXML protected void deleteAntenna() {

        if (!exigirPermiso(Permission.ANT_DELETE)) {
            return;
        }

        if (currentViewAntenna == null){
            try {
                navigator.showError(
                        "Debes seleccionar una antena para pdoer eliminar",
                        "Error al editar usuario",
                        (Stage) btnDeleteAntenna.getScene().getWindow());


            }catch (IOException j){
                System.out.println(j.getMessage());
                System.out.println( j.getCause());
            }
        }

        loadingAnimation.onAnimation(loadingOverlay,loadingCircle,lblLoading);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                deleteAntennaUseCase.deleteAntenna(currentViewAntenna);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            loadingAnimation.finishAnimation();
            loadAntennas();
        });
        appExecutor.execute(task);
    }
    @FXML protected void editAntenna() {

        if (!exigirPermiso(Permission.ANT_EDIT)) {
            return;
        }

        try {
            navigator.<AntennasEditController>openWindow(
                    "/org/example/servinet/forms/antennas-edit.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(),
                    0.30,
                    0.50,
                    controller -> controller.setAntenna(currentViewAntenna)
            );

            loadAntennas();

        } catch (Exception e) {
            System.out.println(e.getMessage() + e.getCause());
        }

    }
    @FXML
    protected void finishMaintenance() {



        if (!exigirPermiso(Permission.ANT_END_MAINT)) {
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

        if (result.isPresent() && result.get() == confirm) {
            try {
                maintenanceAntennaUseCase.off(currentViewAntenna);
            }catch (Exception e){
                System.out.println(e.getMessage()+ e.getCause());
            }
        }


    }
    @FXML
    protected void startMaintenance() {

        if (!exigirPermiso(Permission.ANT_START_MAINTE)) {
            return;
        }
        try {
            navigator.<AntennaMaintenanceController>openWindow(
                    "/org/example/servinet/forms/antenna-manteinance.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(),
                    0.49,
                    0.36,
                    controller -> controller.setAntenna(currentViewAntenna)
            );

            loadAntennas();

        } catch (Exception e) {
            System.out.println(e.getMessage() + e.getCause());
        }

    }
    @FXML
    protected void createAntenna() {

        if (!exigirPermiso(Permission.ANT_CREATE)) {
            return;
        }

        try {
            navigator.awaitOpenWindow(
                    "/org/example/servinet/forms/antenna-create.fxml",
                    (Stage) btnCreateAntenna.getScene().getWindow(),
                    0.70,
                    0.70
            );

           loadAntennas();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML protected void previousAntenna(){

    }
    @FXML protected void nextAntenna(){

    }

    private void loadAntennas(){

        loadingAnimation.onAnimation(loadingOverlay,loadingCircle,lblLoading);
        Task<List<Antenna>> task = new Task<List<Antenna>>() {
            @Override
            protected List<Antenna> call(){
                return getAntennaUseCase.getAll();
            }
        };

        task.setOnSucceeded( event -> {
            loadingAnimation.finishAnimation();
            List<Antenna> antennas = task.getValue();

            int antennasActive = 0;
            int antennasMaintenance = 0;
            int antennasInactive = 0;
            //int antennasConnect= 0; obtener esto de getclients

            if (antennas == null) return;
            antennaList.getChildren().clear();
            for (Antenna a:antennas  ){

                if (a.getStatus().equals(StatusAntenna.ACTIVE)) antennasActive++;
                if (a.getStatus().equals(StatusAntenna.OFFLINE)) antennasInactive++;
                if (a.getStatus().equals(StatusAntenna.MAINTENANCE)) antennasMaintenance++;

                Image image = ImageConverter.toImage(a.getImage());
                renderingAntennas(a, image);
            }
            lblActiveAntennas.setText(antennasActive + "");
            lblInactiveAntennas.setText(antennasInactive + "");
            lblMaintenanceAntennas.setText(antennasMaintenance + "");
            lblConnectedClients.setText(getClientUseCase.getAllClientConectedAntenna() + "");

        });

        appExecutor.execute(task);

    }

    private void renderingAntennas(Antenna a, Image image) {
        try {

            AntennaCarruselController con = navigator.loadComponent(
                    "/org/example/servinet/components/antena-carrusel.fxml",
                    antennaList.getChildren()::add
            );
            con.setAntenna(a);
            con.setImgAntenna1(image);
            con.setLblAntennaLocate("[" + a.getLatitude() +" , "+ a.getLongitude() + "]");
            con.setLblAntennaName(a.getName());
            con.setLblAntennaStatus(a.getStatus().toString());
            con.setLblClientsAntenna("44");

            con.setOnAntennaSelected(selectedAntenna -> {

                currentViewAntenna =  selectedAntenna;
                lblSelectedAntenna.setText(selectedAntenna.getName());
                imgSelectedAntenna.setImage(ImageConverter.toImage(selectedAntenna.getImage()));
                lblSelectedStatus.setText(selectedAntenna.getStatus().toString());
                lblSelectedClients.setText(getClientUseCase.getConnectedClientsAntenna(selectedAntenna.getUuid()) + "");
                lblSelectedLocation.setText(selectedAntenna.getLatitude() + " , " + selectedAntenna.getLongitude());
                lblForMaintenance.setText(selectedAntenna.isForMaintenance() + "");
                if (selectedAntenna.getStatus().equals(StatusAntenna.MAINTENANCE)) {
                    btnFinishMaintenance.setVisible(true);
                    btnStartMaintenance.setVisible(false);
                    return;
                }

                btnFinishMaintenance.setVisible(false);
                btnStartMaintenance.setVisible(true);
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }



}
