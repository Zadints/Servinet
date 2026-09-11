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
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.center.AdministrationController;
import org.example.servinet.ui.controllers.center.AntenasController;
import org.example.servinet.core.domain.utils.MouseMove;

import java.io.IOException;

public class IndexController {

    @FXML
    private BorderPane brPanel;
    @FXML
    private HBox titleBar;
    @FXML
    private StackPane modalOverlay;
    @FXML
    private Circle userImage;
    @FXML
    private Label lblUserRol;
    @FXML
    private Label lblUserName;
    @FXML
    public void initialize() {
        MouseMove newMove = new MouseMove();
        newMove.ControlHBox(titleBar);
        userImage.setFill(new ImagePattern(SessionUseCase.getUserPerfilImg()));
        lblUserRol.setText(SessionUseCase.getUserRol().toString());
        lblUserName.setText(SessionUseCase.getUserName());
        renderizarFxml("dashboard.fxml");
    }

    @FXML
    public void onDashboardClick(ActionEvent event) {
        renderizarFxml("dashboard.fxml");
    }

    @FXML
    public void onAdministracionClick(ActionEvent event) {
        renderizarFxml("administration.fxml");
    }

    @FXML
    public void onAnunciosClick(ActionEvent event) {
        renderizarFxml("anuncios.fxml");
    }

    @FXML
    public void onAntenasClick(ActionEvent event) {
        renderizarFxml("antenas.fxml");
    }

    @FXML
    public void onClientesClick(ActionEvent event) {
        renderizarFxml("cliente.fxml");
    }

    @FXML
    public void onBackupsClick(ActionEvent event) {
        renderizarFxml("backups.fxml");
    }
    @FXML
    public void onSellClick(ActionEvent event) {
        renderizarFxml("sell.fxml");
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


    @FXML
    public void onCloseClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void onMinimizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private Button btnMaximize;
    @FXML
    private VBox root;
    private boolean maximized = true;
    @FXML
    protected void onMaximizeClick() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();

        if (maximized) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            root.getStyleClass().add("maximized");
            maximized = false;
        } else {
            stage.setWidth(1300);
            stage.setHeight(700);
            stage.centerOnScreen();
            root.getStyleClass().remove("maximized");
            maximized = true;
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

            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + archivo);
        }
    }
}