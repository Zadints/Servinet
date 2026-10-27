package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AntennaCarruselController;

import java.io.IOException;

import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
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

    @FXML private ScrollPane antennaCarousel;

    private boolean puedeVerCarrusel;

    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;

    public AntenasController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
    }

    @FXML
    public void initialize() {


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

        // 3. Cargar tarjetas solo si tiene acceso.
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


    // Evita ejecutar una acción sin autorización.
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


    // Aviso para funciones todavía no implementadas.
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
    @FXML
    protected void deleteAntenna() {

        if (!exigirPermiso(Permission.ANT_DELETE)) {
            return;
        }

        avisarPendiente("Eliminar antena");
    }
    @FXML
    protected void editAntenna() {

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

        //obtener valores del USecase;

        for (int i = 0; i < 5; i++){
            renderingAntennas();
        }
    }

    private void renderingAntennas(){
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/antena-carrusel.fxml")
            );



            Node row = loader.load();
            AntennaCarruselController con = loader.getController();
            //con.configurarPermisos(politicaAntenas);

            con.setImgAntenna1(null);
            con.setLblAntennaLocate("a");
            con.setLblAntennaName("a");
            con.setLblAntennaStatus("a");
            con.setLblClientsAntenna("a");

            antennaList.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
