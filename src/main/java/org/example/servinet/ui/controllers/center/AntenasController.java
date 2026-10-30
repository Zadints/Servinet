package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.ui.animations.LoadingAnimation;
import org.example.servinet.ui.animations.SlideUpAnimation;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AntennaCarruselController;

import java.io.IOException;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.navigator.Navigator;

public class AntenasController {

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


    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetAntennaUseCase getAntennaUseCase;
    private final LoadingAnimation loadingAnimation;
    private final SlideUpAnimation slideUpAnimation;

    public AntenasController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService, GetAntennaUseCase getAntennaUseCase, LoadingAnimation loadingAnimation, SlideUpAnimation slideUpAnimation) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
        this.getAntennaUseCase = getAntennaUseCase;
        this.loadingAnimation = loadingAnimation;
        this.slideUpAnimation = slideUpAnimation;
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
    private void avisarPendiente(String funcion) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("Función pendiente");
        alerta.setHeaderText(funcion);
        alerta.setContentText(
                "Esta operación todavía no está implementada " +
                        "en el proyecto original de ServiNet."
        );

        alerta.showAndWait();
    }

    @FXML public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML protected void deleteAntenna() {

        if (!exigirPermiso(Permission.ANT_DELETE)) {
            return;
        }

        avisarPendiente("Eliminar antena");
    }
    @FXML protected void editAntenna() {

        if (!exigirPermiso(Permission.ANT_EDIT)) {
            return;
        }

        if (indexController != null) {
            indexController.abrirModal(
                    FormType.ANTENNA_EDIT
            );
        }
    }
    @FXML
    protected void finishMaintenance() {

        if (!exigirPermiso(Permission.ANT_END_MAINT)) {
            return;
        }

        avisarPendiente("Finalizar mantenimiento");
    }
    @FXML
    protected void startMaintenance() {

        if (!exigirPermiso(Permission.ANT_START_MAINTE)) {
            return;
        }

        avisarPendiente("Iniciar mantenimiento");
    }
    @FXML
    protected void createAntenna() {

        if (!exigirPermiso(Permission.ANT_CREATE)) {
            return;
        }

        if (indexController != null) {
            indexController.abrirModal(
                    FormType.ANTENNA_CREATE
            );
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
            protected List<Antenna> call() throws Exception {

                return getAntennaUseCase.getAll();
            }
        };

        task.setOnSucceeded( event -> {

            List<Antenna> antennas = task.getValue();

            if (antennas == null) return;

            loadingAnimation.finishAnimation();
            for (Antenna a:antennas  ){
                Image image = ImageConverter.toImage(a.getImage());
                renderingAntennas(a, image);
            }

        });

        appExecutor.execute(task);

    }

    private void renderingAntennas(Antenna a, Image image) {
        try {
            AntennaCarruselController con = navigator.loadComponent(
                    "/org/example/servinet/components/antena-carrusel.fxml",
                    antennaList.getChildren()::add
            );

            con.setImgAntenna1(image);
            con.setLblAntennaLocate("[" + a.getLatitude() +" , "+ a.getLatitude() + "]");
            con.setLblAntennaName(a.getName());
            con.setLblAntennaStatus(a.getStatus().toString());
            con.setLblClientsAntenna("44");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
