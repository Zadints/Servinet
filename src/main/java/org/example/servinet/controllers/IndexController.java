package org.example.servinet.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.example.servinet.utils.MouseMove;

import java.io.IOException;

public class IndexController {


    /*--------------------------------------
        Método principal para cargar perfil de usuario y manejar el movimiento de la ventana.
     --------------------------------------*/

    @FXML
    private Circle userImage;
    @FXML
    private HBox titleBar;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnMaximize;
    @FXML
    private VBox root;
    private boolean maximized = true;
    @FXML
    private BorderPane brPanel;


    public void initialize() {

        Image image = new Image(
                getClass()
                        .getResource("/multimedia/images/Screenshot_2.png")
                        .toExternalForm()
        );
        userImage.setFill(new ImagePattern(image));
        MouseMove.Control(titleBar);
    }
    // ====================================== Métodos para top (tab) ==========================================
    @FXML
    protected void onCloseClick() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    @FXML
    protected void onMinimizeClick() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

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
    // ====================================== Métodos de los botones del Sidebar ==========================================


    @FXML
    protected void onDashboardClick() {
        RendericeFxml("dashboard.fxml");
    }
    @FXML
    protected  void onAdministracionClick(){
        RendericeFxml("administration.fxml");
    }
    @FXML
    protected void onAnunciosClick() {
        RendericeFxml("anuncios.fxml");
    }
    @FXML
    protected void onAntenasClick() {
        RendericeFxml("antenas.fxml");
    }
    @FXML
    protected void onClientesClick() {
        RendericeFxml("cliente.fxml");
    }
    @FXML
    protected void onBackupsClick() {
        RendericeFxml("backups.fxml");
    }

    private void RendericeFxml(String file){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/center/" + file)
            );

            Parent view = loader.load();

            brPanel.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}