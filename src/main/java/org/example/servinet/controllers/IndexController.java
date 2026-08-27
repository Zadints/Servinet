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

    // 1. Enlazamos el contenedor principal desde el FXML
    @FXML
    private BorderPane brPanel;

    @FXML
    public void initialize() {
        // Cuando arranca la pantalla principal, cargamos automáticamente el Dashboard
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
        // cargarVista("/org/example/servinet/center/administration.fxml");
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

    // --- EL "MOTOR" QUE CAMBIA LAS PANTALLAS ---
    private void cargarVista(String rutaFxml) {
        try {
            // Cargamos el diseño del archivo que le pasemos (ej. dashboard.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Node vista = loader.load();

            // Reemplazamos lo que haya en el CENTRO del BorderPane con la nueva vista
            brPanel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar cargar la vista: " + rutaFxml);
        }
    }

    // --- CONTROLES DE LA VENTANA (Arriba a la derecha) ---
    @FXML
    public void onCloseClick(ActionEvent event) {
        Platform.exit(); // Cierra el programa
    }

    @FXML
    public void onMinimizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        stage.setIconified(true); // Minimiza la ventana
    }

    @FXML
    public void onMaximizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        // Alterna entre maximizado y tamaño normal
        stage.setMaximized(!stage.isMaximized());
    }
}