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
import javafx.scene.image.Image;
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
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.usecase.AppGeneralUseCase;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.ui.controllers.center.AdministrationController;
import org.example.servinet.ui.controllers.center.AntenasController;
import org.example.servinet.core.domain.utils.MouseMove;

import java.io.IOException;

public class IndexController {
    @FXML private Button btnDashboard;
    @FXML private Button btnAdministracion;
    @FXML private Button btnAnuncios;
    @FXML private Button btnAntenas;
    @FXML private Button btnBackups;
    @FXML private Button btnClientes;
    @FXML private Button btnVentas;
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

    public void initialize() {
        MouseMove newMove = new MouseMove();
        newMove.ControlHBox(titleBar);

        userImage.setFill(new ImagePattern(SessionUseCase.getUserPerfilImg()));
        lblUserRol.setText(SessionUseCase.getStringUserRol());
        lblUserName.setText(SessionUseCase.getUserName());
        applySidebarPermissions();
        openFirstAllowedSection();

        Thread tr = new Thread(() -> {
            RolesUseCase.loadRoles();
            AppGeneralUseCase.loadAppConfig();
            Platform.runLater(() -> {
                lblAppName.setText(AppGeneralUseCase.getAppName());
            });
        });
        tr.setDaemon(true);
        tr.start();

    }
    // Opciones del sidebar o también llamado left en fxml

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
    @FXML protected void onLogoutClick(){
        SessionUseCase.closeSessionUser();

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



    // Opciones del tab o llamado también header
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


    // Métodos auxiliares para el funcionamiento de ui
    private void renderizarFxml(String archivo)
    {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/center/" + archivo ));
            Node vista = loader.load();

            Object controller = loader.getController();

           /* if (controller instanceof AntenasController antenasController) {
                antenasController.setIndexController(this);
            }*/
            if (controller instanceof AdministrationController administrationController) {
                administrationController.setIndexController(this);
            }
            if (controller instanceof AntenasController antenasController) {
                antenasController.setIndexController(this);
            }

            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + archivo);
        }
    }

    public void abrirModal(FormType type){
        abrirModal(type, null, null);
    }

    private boolean has(Permission p) {
        return PermissionValidation.hasPermission(p);
    }

    private void showSection(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }


    private void applySidebarPermissions() {
        showSection(btnDashboard, has(Permission.DASH_VIEW));
        showSection(btnAdministracion, has(Permission.AD_VIEW));
        showSection(btnAnuncios, has(Permission.ANOUN_VIEW));
        showSection(btnAntenas, has(Permission.ANT_VIEW_ANTENNAS) || has(Permission.ANT_MANAGER_ALL));
        showSection(btnBackups, has(Permission.BACKUPS_MANAGER_ALL));
        showSection(btnClientes, has(Permission.CLIENTS_MANAGER_ALL)
                || has(Permission.CLIENT_SEARCH) || has(Permission.CLIENT_INFO));
        showSection(btnVentas, has(Permission.SELL));
    }


    private void openFirstAllowedSection() {
        if (btnDashboard.isVisible()) renderizarFxml("dashboard.fxml");
        else if (btnAdministracion.isVisible()) renderizarFxml("administration.fxml");
        else if (btnAnuncios.isVisible()) renderizarFxml("anuncios.fxml");
        else if (btnAntenas.isVisible()) renderizarFxml("antenas.fxml");
        else if (btnClientes.isVisible()) renderizarFxml("cliente.fxml");
        else if (btnVentas.isVisible()) renderizarFxml("sell.fxml");
        else if (btnBackups.isVisible()) renderizarFxml("backups.fxml");
        else brPanel.setCenter(new Label("Tu rol no tiene secciones asignadas. Habla con el administrador."));
    }

    public void abrirModal(FormType type, Runnable onSaved, User userToEdit){

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/form.fxml")
            );
            Parent form = loader.load();
            FormController controller = loader.getController();
            controller.setOnSaved(onSaved);
            controller.setUserToEdit(userToEdit);
            controller.setParent(modalOverlay, type);

            modalOverlay.getChildren().clear();
            modalOverlay.getChildren().add(form);

            modalOverlay.setVisible(true);
        }catch(IOException e){
            System.out.println(e.getCause());
        }
    }
}