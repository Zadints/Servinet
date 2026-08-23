package org.example.mineide.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import java.io.IOException;

public class IndexController {


    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


    private double x;
    private double y;
    @FXML
    private HBox titleBar;
    public void initialize() {

        titleBar.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();

            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            }
        });
    }


    @FXML
    private Button btnClose;
    @FXML
    protected void onCloseClick() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    @FXML
    private Button btnMinimize;
    @FXML
    protected void onMinimizeClick() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    /*--------------------------------------
         Este metodo contiene atributos para su funcionamiento tanto de JavaFX como
         privados booleanos
     ------------------------------------*/
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


    @FXML
    private BorderPane brPanel;
    @FXML
    protected void onServersClick() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/mineide/center/servers.fxml")
            );

            Parent view = loader.load();

            brPanel.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected  void onFilesClick(){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/mineide/center/files.fxml")
            );

            Parent view = loader.load();

            brPanel.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void onConsoleClick () {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/mineide/center/console.fxml")
            );

            Parent view = loader.load();

            brPanel.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackupsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/mineide/center/backups.fxml")
            );

            Parent view = loader.load();

            brPanel.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}