package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.ui.events.MouseMove;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.util.Duration;

import java.io.IOException;
import javafx.animation.*;
import org.example.servinet.ui.navigator.Navigator;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

    @FXML private AnchorPane mainPanel;
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Button botonIngresar;
    @FXML private Label lblError;
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
    private final Navigator navigator;

    public LoginController(AppExecutor appExecutor, LoginStaffUseCase loginStaffUseCase, Navigator navigator) {
        this.appExecutor = appExecutor;
        this.loginStaffUseCase = loginStaffUseCase;
        this.navigator = navigator;
    }

    public void initialize() {
        MouseMove newMove = new MouseMove(mainPanel);
        newMove.start();

        startAnimation();


        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                //StartAppManager.setAlreadyStartApp();
                return loginStaffUseCase.automaticStaffLogin();
            }
        };

        task.setOnSucceeded(event -> {

            boolean sessionActive = task.getValue();
            if (sessionActive) {
                ocultarLoading();
                try {
                    navigator.navigateToPage("index.fxml", 0.98, 0.98);
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


    @FXML protected void loginAccount() {
        startAnimation();

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {

                try {

                    return loginStaffUseCase.loginStaff(
                            txtUser.getText(),
                            txtPassword.getText()
                    );

                } catch (InvalidCredentialsException | DatabaseException e) {
                    System.out.println(e.getMessage());
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {

            boolean boolAccess = task.getValue();
            if (boolAccess) {
                ocultarLoading();
                try {

                    navigator.navigateToPage("index.fxml", 0.98, 0.98);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                lblError.setText("Contraseña o usuario incorrecta");
                lblError.setVisible(true);
                ocultarLoading();
            }
        });

        appExecutor.execute(task);
    }


    private void openMain() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/index.fxml")
            );

            Stage stagePrincipal = new Stage();

            stagePrincipal.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(fxmlLoader.load());

            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/index.css").toExternalForm(),
                    getClass().getResource("/styles/center-styles.css").toExternalForm(),
                    getClass().getResource("/styles/exception.css").toExternalForm()
            );

            scene.setFill(Color.TRANSPARENT);

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            double width = screen.getWidth() * 0.98;
            double height = screen.getHeight() * 0.98;

            stagePrincipal.setWidth(width);
            stagePrincipal.setHeight(height);

            stagePrincipal.setX(
                    screen.getMinX() +
                            (screen.getWidth() - width) / 2
            );

            stagePrincipal.setY(
                    screen.getMinY() +
                            (screen.getHeight() - height) / 2
            );

            stagePrincipal.setMinWidth(400);
            stagePrincipal.setMinHeight(500);

            stagePrincipal.setTitle("Servinet - Panel Principal");
            stagePrincipal.setScene(scene);

            Stage stageLogin = (Stage) botonIngresar.getScene().getWindow();

            stagePrincipal.show();
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