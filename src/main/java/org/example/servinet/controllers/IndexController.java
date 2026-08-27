package org.example.servinet.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class IndexController {

    @FXML
    private BorderPane brPanel;

    @FXML
    public void initialize() {

        cargarVista("/org/example/servinet/center/dashboard.fxml");
    }

    // --- MÉTODOS DE LOS BOTONES DEL MENÚ LATERAL ---

    @FXML
    public void onDashboardClick(ActionEvent event) {
        cargarVista("/org/example/servinet/center/dashboard.fxml");
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

    private void cargarVista(String rutaFxml) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Node vista = loader.load();


            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + rutaFxml);
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
    public void onMaximizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();

        stage.setMaximized(!stage.isMaximized());
    }
}