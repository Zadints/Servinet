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
import org.example.servinet.core.application.usecase.AppGeneralUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.center.AdministrationController;
import org.example.servinet.ui.controllers.center.AntenasController;
import org.example.servinet.core.domain.utils.MouseMove;

import java.io.IOException;

public class IndexController {

    @FXML private BorderPane brPanel;
    @FXML private HBox titleBar;
    @FXML private StackPane modalOverlay;
    @FXML private Circle userImage;
    @FXML private Label lblUserRol;
    @FXML private Label lblUserName;
    @FXML private Label lblAppName;
    @FXML private Button btnLogoutSession;

    public void initialize() {
        MouseMove newMove = new MouseMove();
        newMove.ControlHBox(titleBar);
        userImage.setFill(new ImagePattern(SessionUseCase.getUserPerfilImg()));
        lblUserRol.setText(SessionUseCase.getStringUserRol());
        lblUserName.setText(SessionUseCase.getUserName());
        renderizarFxml("dashboard.fxml");
        lblAppName.setText(AppGeneralUseCase.loadAppConfig());
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





    @FXML
    public void onCloseClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void onMinimizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML private Button btnMaximize;
    @FXML private VBox root;
    private boolean maximized = false;

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

            // Restaurar al 98% de la pantalla
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


    /*-----------------------------------
        Método no cambiar es para renderizar Fxml de forma optimizada sin mucho código
        de ruta.
     -------------------------------------*/
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

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/form.fxml")
            );
            System.out.println("FORM CARGADOoo");
            Parent form = loader.load();
            System.out.println("FORM CARGADO: " + form);
            FormController controller = loader.getController();
            controller.setParent(modalOverlay, type);

            modalOverlay.getChildren().clear();
            modalOverlay.getChildren().add(form);

            modalOverlay.setVisible(true);
        }catch(IOException e){
            System.out.println(e.getCause());
        }
    }
}