package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.UiPermission;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.ui.ImageConverter;
import org.example.servinet.ui.controllers.center.*;
import org.example.servinet.ui.events.MouseMove;

import java.io.IOException;

import org.example.servinet.ui.navigator.Navigator;

public class IndexController {

    @FXML private BorderPane brPanel;
    @FXML private HBox titleBar;
    @FXML private StackPane modalOverlay;
    @FXML private Circle userImage;
    @FXML private Label lblUserRol;
    @FXML private Label lblUserName;
    @FXML private Label lblAppName;
    @FXML private Button btnLogoutSession;
    @FXML private Button btnMaximize;
    @FXML private VBox root;
    private boolean maximized = false;

    @FXML private Button btnAdministracion;
    @FXML private Button btnAnuncios;
    @FXML private Button btnAntenas;
    @FXML private Button btnBackups;
    @FXML private Button btnClientes;
    @FXML private Button btnVentas;
    @FXML private Button btnTecnicos;
    @FXML private Button btnDashboard;


    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final GetStaffUseCase staffUseCase;
    private final AuthorizationSecurity authorizationService;

    public IndexController(AppExecutor appExecutor, Navigator navigator, GetStaffUseCase staffUseCase, AuthorizationSecurity authorizationService) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.staffUseCase = staffUseCase;

        this.authorizationService = authorizationService;
    }

    public void initialize() {

        MouseMove newMove = new MouseMove(titleBar);
        newMove.start();

        //permissionPolicy();
        loadCurrentStaffAccount();
    }

    private void loadCurrentStaffAccount(){
        Staff staff  = staffUseCase.getCurrentStaff();

        userImage.setFill(
                new ImagePattern(ImageConverter.toImage(staff.getPerfilImg()))
        );

        lblUserRol.setText(staffUseCase.getUserRol().getName());
        lblUserName.setText(staffUseCase.getCurrentStaff().getDisplay());
        lblAppName.setText("Servinet");
    }


    private void permissionPolicy() {
        UiPermission uiPermission = authorizationService.getUiPermission();

        //show(btnDashboard,uiPermission.isViewDashboard());
        show(btnAdministracion,uiPermission.isViewAdministration());
        show(btnAnuncios,uiPermission.isViewAnnounces());
        show(btnAntenas,uiPermission.isViewAntennas());
        show(btnBackups,uiPermission.isViewBackups());
        show(btnClientes,uiPermission.isViewClients());
        show(btnVentas, uiPermission.isViewSales());
        show(btnTecnicos, uiPermission.isViewTechnical());
        navigate("dashboard.fxml");
    }

    public void abrirModalAnnounce(Runnable onSaved, Announcement announcementToEdit) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/form.fxml")
            );

            Parent form = loader.load();

            FormController controller = loader.getController();

            controller.setAnnouncementToEdit(announcementToEdit);
            controller.setOnSaved(onSaved);
            controller.setParent(modalOverlay, FormType.ANNOUNCE_CREATE);

            modalOverlay.getChildren().clear();
            modalOverlay.getChildren().add(form);
            modalOverlay.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar el formulario de anuncios");
        }
    }
    private void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    @FXML protected void onDashboardClick(ActionEvent event){
        navigate("dashboard.fxml");
    }
    @FXML protected void onAdministracionClick(ActionEvent event) {
        navigate("administration.fxml");
    }
    @FXML protected void onAnunciosClick(ActionEvent event) {
        navigate("anuncios.fxml");
    }
    @FXML protected void onAntenasClick(ActionEvent event) {
        navigate("antenas.fxml");
    }
    @FXML protected void onClientesClick(ActionEvent event) {
        navigate("cliente.fxml");
    }
    @FXML protected void onBackupsClick(ActionEvent event) {
        navigate("backups.fxml");
    }
    @FXML protected void onSellClick(ActionEvent event) {
        navigate("sell.fxml");
    }
    @FXML protected void onTecnicosClick(ActionEvent event){
        navigate("tecnicos.fxml");
    }


    private void navigate(String file){
        try {
            navigator.navigateToCenter(brPanel, file);
        }catch (IOException e){
            System.out.println("error al cargar: " + e.getMessage());
        }

    }


    @FXML protected void onLogoutClick(){
       try {
           navigator.backPage();
       }catch (IOException e){
           System.out.println(e.getCause().getMessage());
       }
    }

    @FXML protected void onCloseClick(ActionEvent event) {
        Platform.exit();
    }
    @FXML protected void onMinimizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        stage.setIconified(true);
    }
    @FXML protected void onMaximizeClick() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();

        if (!maximized) {

            // Maximizar al 100% de la pantalla
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());

            root.getStyleClass().add("maximized");

            maximized = true;

        } else {

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            double width = screen.getWidth() * 0.98;
            double height = screen.getHeight() * 0.98;

            stage.setWidth(width);
            stage.setHeight(height);

            stage.setX(
                    screen.getMinX()
                            + (screen.getWidth() - width) / 2
            );

            stage.setY(
                    screen.getMinY()
                            + (screen.getHeight() - height) / 2
            );

            root.getStyleClass().remove("maximized");

            maximized = false;
        }
    }



    public void renderizarFxml(String archivo)
    {
        /*
        // 1. Identificar el módulo.
        Module module = moduloDeVista(archivo);

        // 2. Evaluar los permisos del usuario actual.
        PermissionPolicy policy = PermissionPolicy.current();

        // 3. Bloquear vistas no registradas o no autorizadas.
        if (module == null || !policy.canOpen(module)) {

            System.out.println(
                    "Acceso denegado a la vista: " + archivo
            );

            return;
        }
*/
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/center/" + archivo ));
            Node vista = loader.load();

            Object controller = loader.getController();


            if (controller instanceof AdministrationController administrationController) {
                administrationController.setIndexController(this);
            }
            if (controller instanceof AntenasController antenasController) {
                antenasController.setIndexController(this);
            }
            if (controller instanceof ClientesController clientesController) {
                clientesController.setIndexController(this);
            }

            if (controller instanceof VentasMenuController ventasMenuController) {
                ventasMenuController.setIndexController(this);
            }
            if (controller instanceof AnunciosController anunciosController) {
                anunciosController.setIndexController(this);
            }
            if (controller instanceof VentasFormController ventasFormController) {
                ventasFormController.setIndexController(this);
            }

            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + archivo);
        }
    }

    public void abrirModal(FormType type) {

        abrirModal(
                type,
                null,
                null
        );
    }


    public void abrirModal(FormType type, Runnable onSaved, Staff userToEdit) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/servinet/form.fxml"
                    )
            );


            Parent form = loader.load();


            FormController controller =
                    loader.getController();


            /*
             * IMPORTANTE:
             *
             * El usuario debe asignarse ANTES de setParent(),
             * porque setParent() rellena los campos del formulario
             * cuando el tipo es USER_EDIT.
             */
            controller.setUserToEdit(userToEdit);

            controller.setOnSaved(onSaved);

            controller.setParent(
                    modalOverlay,
                    type
            );


            modalOverlay
                    .getChildren()
                    .clear();

            modalOverlay
                    .getChildren()
                    .add(form);

            modalOverlay.setVisible(true);


        } catch (IOException e) {

            e.printStackTrace();

            System.out.println(
                    "Error al cargar el formulario: "
                            + type
            );
        }
    }


    public void abrirModalClient(
            FormType type,
            Runnable onSaved,
            Client clientToEdit
    ) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/servinet/form.fxml"
                    )
            );

            Parent form = loader.load();

            FormController controller =
                    loader.getController();

            /*
             * IMPORTANTE:
             *
             * El cliente debe asignarse ANTES de setParent(),
             * porque setParent() rellena los campos del formulario
             * cuando el tipo es CLIENT_EDIT.
             */
            controller.setClientToEdit(clientToEdit);

            controller.setOnSaved(onSaved);

            controller.setParent(
                    modalOverlay,
                    type
            );

            modalOverlay
                    .getChildren()
                    .clear();

            modalOverlay
                    .getChildren()
                    .add(form);

            modalOverlay.setVisible(true);

        } catch (IOException e) {

            e.printStackTrace();

            System.out.println(
                    "Error al cargar el formulario: "
                            + type
            );
        }
    }
}