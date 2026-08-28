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
import org.example.servinet.utils.MouseMove;

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
        MouseMove newMove = new MouseMove();
        newMove.ControlAnchorPane(panelPrincipal);
    }


    @FXML
    public void iniciarSesion(ActionEvent event) {
        String usuario = campoUsuario.getText();
        String password = campoPassword.getText();


        if (usuario.equals("admin") && password.equals("1234")) {
            System.out.println("¡Credenciales correctas! Entrando al sistema...");
            abrirPantallaPrincipal();
        } else {

            System.out.println("Error: Usuario o contraseña incorrectos.");

        }
    }


    private void abrirPantallaPrincipal() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/servinet/index.fxml"));
            Scene scene = new Scene(fxmlLoader.load());


            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/index.css").toExternalForm(),
                    getClass().getResource("/styles/center-styles.css").toExternalForm(),
                    getClass().getResource("/styles/exception.css").toExternalForm()
            );


            Stage stagePrincipal = new Stage();
            stagePrincipal.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            stagePrincipal.setWidth(1300);
            stagePrincipal.setHeight(700);
            stagePrincipal.setTitle("Servinet - Panel Principal");
            stagePrincipal.setScene(scene);


            stagePrincipal.show();


            Stage stageLogin = (Stage) botonIngresar.getScene().getWindow();
            stageLogin.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar abrir la pantalla principal.");
        }
    }


    @FXML
    public void cerrarAplicacion(ActionEvent event) {
        Platform.exit();
    }


}