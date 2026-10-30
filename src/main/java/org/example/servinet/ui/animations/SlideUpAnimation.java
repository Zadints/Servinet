
package org.example.servinet.ui.animations;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SlideUpAnimation {

    private final Duration duration;
    private final double distance;

    public SlideUpAnimation() {
        this(Duration.millis(550), 25);
    }

    public SlideUpAnimation(Duration duration, double distance) {
        this.duration = duration;
        this.distance = distance;
    }

    public void play(Node component) {

        component.setOpacity(0);
        component.setTranslateY(distance);

        TranslateTransition slide = new TranslateTransition(
                duration,
                component
        );

        slide.setFromY(distance);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);


        FadeTransition fade = new FadeTransition(
                duration,
                component
        );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(slide, fade).play();
    }
}
