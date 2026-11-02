package org.example.servinet.ui.animations;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ShimmerEffectAnimation {

    private final List<Overlay> overlays = new ArrayList<>();

    public void start(Region... targets) {

        stop();

        for (Region target : targets) {

            if (target == null || !(target.getParent() instanceof Pane parent)) {
                continue;
            }

            target.applyCss();

            double width = target.getWidth();
            double height = target.getHeight();

            if (width <= 0 || height <= 0) {
                continue;
            }

            createShimmer(target, parent, width, height);
        }
    }

    private void createShimmer(
            Region target,
            Pane parent,
            double width,
            double height
    ) {

        double originalOpacity = target.getOpacity();


        target.setOpacity(0);

        Group group = new Group();
        group.setManaged(false);
        group.setMouseTransparent(true);
        group.setViewOrder(-1);

        group.layoutXProperty().bind(target.layoutXProperty());
        group.layoutYProperty().bind(target.layoutYProperty());


        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());

        clip.setArcWidth(18);
        clip.setArcHeight(18);

        group.setClip(clip);


        Rectangle background = new Rectangle();

        background.widthProperty().bind(target.widthProperty());
        background.heightProperty().bind(target.heightProperty());

        background.setArcWidth(18);
        background.setArcHeight(18);
        background.setFill(Color.web("#E1E5EB"));

        Rectangle shine = new Rectangle();

        shine.setWidth(width * 0.42);
        shine.heightProperty().bind(target.heightProperty().multiply(1.5));
        shine.layoutYProperty().bind(target.heightProperty().multiply(-0.25));

        shine.setRotate(-12);

        shine.setFill(new LinearGradient(
                0, 0, 1, 0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.35, Color.color(1, 1, 1, 0.22)),
                new Stop(0.5, Color.color(1, 1, 1, 0.85)),
                new Stop(0.65, Color.color(1, 1, 1, 0.22)),
                new Stop(1, Color.TRANSPARENT)
        ));

        group.getChildren().addAll(background, shine);
        parent.getChildren().add(group);


        TranslateTransition animation =
                new TranslateTransition(Duration.millis(1200), shine);

        animation.setFromX(width);
        animation.setToX(-shine.getWidth());
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setCycleCount(Animation.INDEFINITE);

        animation.play();

        overlays.add(new Overlay(
                target,
                parent,
                group,
                shine,
                animation,
                originalOpacity
        ));
    }

    public void stop() {

        for (Overlay overlay : overlays) {

            overlay.animation.stop();

            overlay.target.setOpacity(overlay.originalOpacity);

            overlay.group.layoutXProperty().unbind();
            overlay.group.layoutYProperty().unbind();

            overlay.group.getChildren().clear();

            if (overlay.target.getParent() == overlay.parent) {
                overlay.parent.getChildren().remove(overlay.group);
            }
        }

        overlays.clear();
    }

    private static class Overlay {

        private final Region target;
        private final Pane parent;
        private final Group group;
        private final Rectangle shine;
        private final TranslateTransition animation;
        private final double originalOpacity;

        private Overlay(
                Region target,
                Pane parent,
                Group group,
                Rectangle shine,
                TranslateTransition animation,
                double originalOpacity
        ) {
            this.target = target;
            this.parent = parent;
            this.group = group;
            this.shine = shine;
            this.animation = animation;
            this.originalOpacity = originalOpacity;
        }
    }
}
