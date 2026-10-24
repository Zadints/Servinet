package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.application.usecase.AppGeneralUseCase;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.ui.controllers.center.*;
import org.example.servinet.ui.events.MouseMove;

import java.io.IOException;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.PermissionPolicy.Module;

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



    public void initialize() {

        MouseMove newMove = new MouseMove(titleBar);
        newMove.start();

        userImage.setFill(
                new ImagePattern(LoginStaffUseCase.getUserPerfilImg())
        );

        lblUserRol.setText(LoginStaffUseCase.getStringUserRol());
        lblUserName.setText(LoginStaffUseCase.getUserName());
        lblAppName.setText(AppGeneralUseCase.getAppName());

        // Evaluar permisos al iniciar la pantalla.
        aplicarPermisosMenu();
    }


    private void aplicarPermisosMenu() {

        // Capturar los permisos de la sesión.
        PermissionPolicy policy = PermissionPolicy.current();

        // Mostrar u ocultar botones.
        show(
                btnDashboard,
                policy.canOpen(Module.DASHBOARD)
        );

        show(
                btnAdministracion,
                policy.canOpen(Module.ADMINISTRATION)
        );

        show(
                btnAnuncios,
                policy.canOpen(Module.ANNOUNCEMENTS)
        );

        show(
                btnAntenas,
                policy.canOpen(Module.ANTENNAS)
        );

        show(
                btnBackups,
                policy.canOpen(Module.BACKUPS)
        );

        show(
                btnClientes,
                policy.canOpen(Module.CLIENTS)
        );

        show(
                btnVentas,
                policy.canOpen(Module.SALES)
        );

        show(
                btnTecnicos,
                policy.canOpen(Module.TECHNICIANS)
        );

        // Elegir la primera pantalla permitida.
        String[] vistasIniciales = {
                "dashboard.fxml",
                "administration.fxml",
                "anuncios.fxml",
                "antenas.fxml",
                "backups.fxml",
                "cliente.fxml",
                "sell.fxml",
                "tecnicos.fxml"
        };

        for (String vista : vistasIniciales) {

            Module module = moduloDeVista(vista);

            if (policy.canOpen(module)) {

                renderizarFxml(vista);
                return;
            }
        }

        // Si el usuario no puede acceder a ningún módulo.
        brPanel.setCenter(
                new Label(
                        "Tu rol no tiene permisos para acceder " +
                                "a los módulos. Contacta al administrador."
                )
        );
    }


    private Module moduloDeVista(String archivo) {

        if (archivo == null) {
            return null;
        }

        return switch (archivo) {

            case "dashboard.fxml" ->
                    Module.DASHBOARD;

            case "administration.fxml" ->
                    Module.ADMINISTRATION;

            case "anuncios.fxml" ->
                    Module.ANNOUNCEMENTS;

            case "antenas.fxml" ->
                    Module.ANTENNAS;

            case "backups.fxml" ->
                    Module.BACKUPS;

            case "cliente.fxml" ->
                    Module.CLIENTS;

            case "sell.fxml",
                 "sell-form.fxml",
                 "ventas-menu.fxml",
                 "registrar-pago.fxml",
                 "ofertas.fxml",
                 "mis-contratos.fxml",
                 "editar-contrato.fxml",
                 "estadisticas-ventas.fxml",
                 "prospectos.fxml" ->
                    Module.SALES;

            case "tecnicos.fxml" ->
                    Module.TECHNICIANS;

            default -> null;
        };
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

    @FXML protected void onDashboardClick(ActionEvent event) {
        renderizarFxml("dashboard.fxml");
    }
    @FXML protected void onAdministracionClick(ActionEvent event) {
        renderizarFxml("administration.fxml");
    }
    @FXML protected void onAnunciosClick(ActionEvent event) {
        renderizarFxml("anuncios.fxml");
    }
    @FXML protected void onAntenasClick(ActionEvent event) {
        renderizarFxml("antenas.fxml");
    }
    @FXML protected void onClientesClick(ActionEvent event) {
        renderizarFxml("cliente.fxml");
    }
    @FXML protected void onBackupsClick(ActionEvent event) {
        renderizarFxml("backups.fxml");
    }
    @FXML protected void onSellClick(ActionEvent event) {
        renderizarFxml("sell.fxml");
    }
    @FXML protected void onTecnicosClick(ActionEvent event){renderizarFxml("tecnicos.fxml");}

    @FXML protected void onLogoutClick(){
        LoginStaffUseCase.closeActualSessionStaff();

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/org/example/servinet/login.fxml")
        );
        Stage stagePrincipal = new Stage();

        stagePrincipal.initStyle(StageStyle.TRANSPARENT);


        try {
            Scene scene = new Scene(fxmlLoader.load());

            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/index.css").toExternalForm(),
                    getClass().getResource("/styles/center-styles.css").toExternalForm(),
                    getClass().getResource("/styles/exception.css").toExternalForm()
            );

            scene.setFill(Color.TRANSPARENT);

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            double width = screen.getWidth() * 0.98;
            double height = screen.getHeight() * 0.98;

            stagePrincipal.setWidth(width);
            stagePrincipal.setHeight(height);

            stagePrincipal.setX(
                    screen.getMinX() +
                            (screen.getWidth() - width) / 2
            );

            stagePrincipal.setY(
                    screen.getMinY() +
                            (screen.getHeight() - height) / 2
            );

            stagePrincipal.setMinWidth(400);
            stagePrincipal.setMinHeight(500);

            stagePrincipal.setTitle("Servinet");
            stagePrincipal.setScene(scene);

            Stage stageLogin = (Stage) btnLogoutSession.getScene().getWindow();

            stagePrincipal.show();
            stageLogin.close();


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar abrir la pantalla principal.");
        }
    }



    //----------------------------------------
    // Opciones del tab o llamado también header
    //-------------------------------------------
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