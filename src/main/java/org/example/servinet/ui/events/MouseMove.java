package org.example.servinet.ui.events;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MouseMove {

    private final Node node;

    private double x;
    private double y;

    public MouseMove(Node node) {
        this.node = node;
    }

    public void start() {

        node.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        node.setOnMouseDragged(event -> {

            Stage stage = (Stage) node.getScene().getWindow();

            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            }
        });
    }

    public void stop() {
        node.setOnMousePressed(null);
        node.setOnMouseDragged(null);
    }
}
