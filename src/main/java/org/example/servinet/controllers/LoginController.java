package org.example.servinet.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;

import java.io.IOException;

public class LoginController {

    @FXML
    private AnchorPane panelPrincipal;

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Button botonIngresar;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
    }

    // --- LÓGICA DEL LOGIN (¡ACTUALIZADA!) ---
    @FXML
    public void iniciarSesion(ActionEvent event) {
        String usuario = campoUsuario.getText();
        String password = campoPassword.getText();

        // 1. Simulamos la validación (Más adelante esto se conectará a la Base de Datos)
        if (usuario.equals("admin") && password.equals("1234")) {
            System.out.println("¡Credenciales correctas! Entrando al sistema...");
            abrirPantallaPrincipal();
        } else {
            // Si se equivocan, por ahora lo mostramos en consola
            System.out.println("Error: Usuario o contraseña incorrectos.");
            // Aquí luego podemos poner un mensaje en rojo en la pantalla
        }
    }

    // --- NUEVO MÉTODO: ABRIR EL DASHBOARD ---
    private void abrirPantallaPrincipal() {
        try {
            // 1. Cargamos tu archivo principal (index.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/servinet/index.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // 2. Cargamos tus estilos (igual que en tu App.java)
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/index.css").toExternalForm(),
                    getClass().getResource("/styles/center-styles.css").toExternalForm(),
                    getClass().getResource("/styles/exception.css").toExternalForm()
            );

            // 3. Creamos una nueva ventana (Stage)
            Stage stagePrincipal = new Stage();
            stagePrincipal.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            stagePrincipal.setWidth(1300);
            stagePrincipal.setHeight(700);
            stagePrincipal.setTitle("Servinet - Panel Principal");
            stagePrincipal.setScene(scene);

            // 4. Mostramos la nueva ventana
            stagePrincipal.show();

            // 5. Cerramos la ventana de Login actual
            Stage stageLogin = (Stage) botonIngresar.getScene().getWindow();
            stageLogin.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar abrir la pantalla principal.");
        }
    }

    // --- LÓGICA PARA CERRAR (BOTÓN X) ---
    @FXML
    public void cerrarAplicacion(ActionEvent event) {
        Platform.exit();
    }

    // --- LÓGICA PARA MOVER LA VENTANA ---
    @FXML
    public void alPresionarRaton(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void alArrastrarRaton(MouseEvent event) {
        Stage stage = (Stage) panelPrincipal.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
}