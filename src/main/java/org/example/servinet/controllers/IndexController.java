package org.example.servinet.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.servinet.utils.MouseMove;

import java.io.IOException;

public class IndexController {

    @FXML
    private BorderPane brPanel;
    @FXML
    private HBox titleBar;
    @FXML
    public void initialize() {
        MouseMove newMove = new MouseMove();
        newMove.ControlHBox(titleBar);
    }

    // --- MÉTODOS DE LOS BOTONES DEL MENÚ LATERAL ---

    @FXML
    public void onDashboardClick(ActionEvent event) {
        renderizarFxml("dashboard.fxml");
    }

    @FXML
    public void onAdministracionClick(ActionEvent event) {
        System.out.println("Clic en Administración");

    }

    @FXML
    public void onAnunciosClick(ActionEvent event) {
        System.out.println("Clic en Anuncios");
    }

    @FXML
    public void onAntenasClick(ActionEvent event) {
        System.out.println("Clic en Antenas");
    }

    @FXML
    public void onClientesClick(ActionEvent event) {
        System.out.println("Clic en Clientes");
    }

    @FXML
    public void onBackupsClick(ActionEvent event) {
        System.out.println("Clic en Backups");
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


            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + archivo);
        }
    }
}