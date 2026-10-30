
package org.example.servinet.ui.animations;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.servinet.domain.exception.AnimationException;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoadingAnimation {

    private RotateTransition rotate;
    private FadeTransition iconFade;
    private ScaleTransition pulse;
    private Timeline dotsAnimation;
    private PauseTransition timeout;
    private FadeTransition overlayFade;

    private StackPane loadingOverlay;
    private Circle loadingCircle;
    private Label lblLoading;

    public LoadingAnimation() {
        loadingOverlay = null;
        loadingCircle = null;
        lblLoading = null;
    }

    public void onAnimation(
            StackPane loadingOverlay,
            Circle loadingCircle,
            Label lblLoading
    ) {

        this.loadingOverlay = loadingOverlay;
        this.loadingCircle = loadingCircle;
        this.lblLoading = lblLoading;

        stopTimers();

        startAnimation(loadingOverlay, loadingCircle, lblLoading);

        timeout = new PauseTransition(Duration.seconds(30));
        timeout.setOnFinished(event ->
                finishAnimation()
        );
        timeout.play();
    }

    private void startAnimation(
            StackPane loadingOverlay,
            Circle loadingCircle,
            Label lblLoading
    ) {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
        loadingOverlay.setOpacity(0);

        loadingCircle.getStrokeDashArray().setAll(12.0, 8.0);

        rotate = new RotateTransition(Duration.seconds(1.2), loadingCircle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        FontIcon wifiIcon = (FontIcon)
                ((StackPane) loadingCircle.getParent()).getChildren().get(1);

        iconFade = new FadeTransition(Duration.millis(900), wifiIcon);
        iconFade.setFromValue(0.35);
        iconFade.setToValue(1);
        iconFade.setAutoReverse(true);
        iconFade.setCycleCount(Animation.INDEFINITE);
        iconFade.play();

        pulse = new ScaleTransition(Duration.millis(900), loadingCircle);
        pulse.setFromX(0.92);
        pulse.setFromY(0.92);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();

        dotsAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        event -> lblLoading.setText("Cargando anuncios.")),
                new KeyFrame(Duration.millis(500),
                        event -> lblLoading.setText("Verificando anuncios..")),
                new KeyFrame(Duration.millis(1000),
                        event -> lblLoading.setText("Cargando anuncios...")),
                new KeyFrame(Duration.millis(1500),
                        event -> lblLoading.setText("Renderizando.."))
        );

        dotsAnimation.setCycleCount(Animation.INDEFINITE);
        dotsAnimation.play();

        overlayFade = new FadeTransition(
                Duration.millis(350), loadingOverlay
        );
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
    }

    public void finishAnimation() {

        if (loadingCircle == null){
            throw new AnimationException("No iniciaste la animación para terminarla");
        }

        stopTimers();

        if (rotate != null) rotate.stop();
        if (iconFade != null) iconFade.stop();
        if (pulse != null) pulse.stop();
        if (dotsAnimation != null) dotsAnimation.stop();
        if (overlayFade != null) overlayFade.stop();

        FadeTransition fade = new FadeTransition(
                Duration.millis(400), loadingOverlay
        );

        fade.setFromValue(loadingOverlay.getOpacity());
        fade.setToValue(0);

        fade.setOnFinished(event -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        });

        fade.play();
    }

    private void stopTimers() {
        if (timeout != null) {
            timeout.stop();
            timeout = null;
        }
    }
}
