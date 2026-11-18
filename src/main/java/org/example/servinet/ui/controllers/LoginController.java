package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.servinet.application.dto.staff.StaffDto;
import org.example.servinet.application.usecase.ApplicationTemplateUseCase;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.application.usecase.staff.RegisterStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.ui.events.MouseMove;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

import javafx.animation.*;
import org.example.servinet.ui.navigator.Navigator;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

   /*
    @FXML private TextField txtRegisterDni;
    @FXML private TextField txtRegisterName;
    @FXML private TextField txtRegisterLastName;
    @FXML private TextField txtRegisterPhone;
    @FXML private TextField txtRegisterEmail;
    @FXML private PasswordField txtRegisterPassword;
    @FXML private PasswordField txtRepeatPassword;


    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
*/

    @FXML private AnchorPane mainPanel;
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogIn;// btnRegister, btnAuthMode;
    @FXML private Label lblError, lblAuthMode;
    @FXML private StackPane loadingOverlay;
    @FXML private Circle loadingCircle;
    @FXML private Label lblLoading;

    private static String pathFxml;
    private RotateTransition rotate;
    private FadeTransition iconFade;
    private ScaleTransition pulse;
    private Timeline dotsAnimation;


    private final AppExecutor appExecutor;
    private final LoginStaffUseCase loginStaffUseCase;
    private final RegisterStaffUseCase registerStaffUseCase;
    private final Navigator navigator;
    private Boolean AUTH_MODE = true;
    private final ApplicationTemplateUseCase applicationTemplateUseCase;

    public LoginController(AppExecutor appExecutor, LoginStaffUseCase loginStaffUseCase, RegisterStaffUseCase registerStaffUseCase, Navigator navigator, ApplicationTemplateUseCase applicationTemplateUseCase) {
        this.appExecutor = appExecutor;
        this.loginStaffUseCase = loginStaffUseCase;
        this.registerStaffUseCase = registerStaffUseCase;
        this.navigator = navigator;
        this.applicationTemplateUseCase = applicationTemplateUseCase;
    }

    public void initialize() {
        MouseMove newMove = new MouseMove(mainPanel);
        newMove.start();

        startAnimation();


        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                applicationTemplateUseCase.executeTemplate();
                try {
                    return loginStaffUseCase.automaticStaffLogin();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    return false;
                }
            }
        };

        task.setOnSucceeded(event -> {

            boolean sessionActive = task.getValue();
            if (sessionActive) {
                ocultarLoading();
                try {
                    navigator.navigateToPage("/org/example/servinet/index.fxml", 0.98, 0.98);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                ocultarLoading();
            }
        });


       appExecutor.execute(task);

    }

    private void ocultarLoading() {

        if (rotate != null) rotate.stop();
        if (iconFade != null) iconFade.stop();
        if (pulse != null) pulse.stop();
        if (dotsAnimation != null) dotsAnimation.stop();

        FadeTransition fade = new FadeTransition(
                Duration.millis(400),
                loadingOverlay
        );

        fade.setFromValue(1);
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        });

        fade.play();
    }

    private void startAnimation() {

        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
        loadingOverlay.setOpacity(1);


        loadingCircle.getStrokeDashArray().setAll(12.0, 8.0);

        RotateTransition rotate = new RotateTransition(
                Duration.seconds(1.2),
                loadingCircle
        );

        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();


        FontIcon wifiIcon = (FontIcon)
                ((StackPane) loadingCircle.getParent()).getChildren().get(1);

        FadeTransition iconFade = new FadeTransition(
                Duration.millis(900),
                wifiIcon
        );

        iconFade.setFromValue(0.35);
        iconFade.setToValue(1);
        iconFade.setAutoReverse(true);
        iconFade.setCycleCount(Animation.INDEFINITE);
        iconFade.play();

        ScaleTransition pulse = new ScaleTransition(
                Duration.millis(900),
                loadingCircle
        );

        pulse.setFromX(0.92);
        pulse.setFromY(0.92);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();

        Timeline dotsAnimation = new Timeline();

        dotsAnimation.getKeyFrames().addAll(

                new KeyFrame(
                        Duration.ZERO,
                        event -> lblLoading.setText(
                                "Verificando sesión de dispositivo."
                        )
                ),

                new KeyFrame(
                        Duration.millis(500),
                        event -> lblLoading.setText(
                                "Verificando sesión de dispositivo.."
                        )
                ),

                new KeyFrame(
                        Duration.millis(1000),
                        event -> lblLoading.setText(
                                "Verificando sesión de dispositivo..."
                        )
                ),

                new KeyFrame(
                        Duration.millis(1500),
                        event -> lblLoading.setText(
                                "Verificando sesión de dispositivo"
                        )
                )
        );

        dotsAnimation.setCycleCount(Animation.INDEFINITE);
        dotsAnimation.play();


        loadingOverlay.setOpacity(0);

        FadeTransition overlayFade = new FadeTransition(
                Duration.millis(350),
                loadingOverlay
        );

        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);

        overlayFade.play();
    }
    /*
    @FXML private void registerAccount(){
        startAnimation();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {

                if (!Objects.equals(txtPassword.getText(), txtRepeatPassword.getText())){
                    return false;
                }

                String password = txtPassword.getText();

                try {
                    return registerStaffUseCase.register(
                            new StaffDto(
                                txtRegisterDni.getText(),
                                txtRegisterName.getText(),
                                password,
                                txtRegisterLastName.getText(),
                                txtRegisterPhone.getText(),


                            );
                    );
                } catch (InvalidCredentialsException | DatabaseException e) {

                    System.out.println(e.getMessage());
                    return false;
                }

            }
        };

        task.setOnSucceeded(event -> {

            boolean boolAccess = task.getValue();
            if (boolAccess) {
                ocultarLoading();
                try {

                    navigator.navigateToPage("/org/example/servinet/index.fxml", 0.98, 0.98);
                } catch (Exception e) {
                    System.out.println(e.getMessage());

                }
            } else {
                lblError.setText("Error al registrar el usuario");
                lblError.setVisible(true);
                ocultarLoading();
            }
        });

        appExecutor.execute(task);
    }
*/
    @FXML private void loginAccount() {
        startAnimation();

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return loginStaffUseCase.loginStaff(
                        txtUser.getText(),
                        txtPassword.getText()
                );
            }
        };
        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            exception.printStackTrace();
            System.out.println(exception.getMessage() + exception.getCause());
            lblError.setText(exception.getMessage());
        });

        task.setOnSucceeded(event -> {

            boolean boolAccess = task.getValue();
            if (boolAccess) {
                ocultarLoading();
                try {

                    navigator.navigateToPage("/org/example/servinet/index.fxml", 0.98, 0.98);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage() + e.getCause());

                }
            } else {
                lblError.setText("Contraseña o usuario incorrecta");
                lblError.setVisible(true);
                ocultarLoading();
            }
        });

        appExecutor.execute(task);
    }


    @FXML
    public void cerrarAplicacion(ActionEvent event) {
        Platform.exit();
    }
/*

    @FXML private void changueAuthMode(ActionEvent actionEvent) {
        boolean register = !registerForm.isVisible();
        setActive(register, loginForm);
        setActive(register, registerForm);
        lblAuthMode.setText(register
                ? "¿Ya tienes una cuenta?"
                : "¿No tienes una cuenta?");

        btnAuthMode.setText(register
                ? "Iniciar sesión"
                : "Registrarme");
        lblError.setVisible(false);
    }

    private void setActive(boolean isActive, Node node){
        node.setVisible(isActive);
        node.setManaged(isActive);

    }
    */

}


