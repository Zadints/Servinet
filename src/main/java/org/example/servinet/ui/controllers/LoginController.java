package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.exception.InvalidCredentialsException;
import org.example.servinet.core.domain.utils.MouseMove;

import java.io.IOException;

public class LoginController {

    @FXML
    private AnchorPane mainPanel;
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button botonIngresar;
    @FXML
    private Label lblError;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        MouseMove newMove = new MouseMove();
        newMove.ControlAnchorPane(mainPanel);
    }


    @FXML
    public void loginAccount(ActionEvent event) {
        try {
            boolean boolAccess = SessionUseCase.loginUser(txtUser.getText(), txtPassword.getText());
            if (!boolAccess){
                lblError.setText("Contraseña o usuario incorrecta");
                lblError.setVisible(true);
                return;
            }
            openMain();


        } catch (InvalidCredentialsException | DatabaseException e) {
            lblError.setText(e.getMessage());
            System.out.println(e.getCause() + " ");
            lblError.setVisible(true);
        }
    }


    private void openMain() {
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