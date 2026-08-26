package org.example.servinet.utils;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MouseMove {

    private static double x;
    private static double y;


    public static void Control(HBox titleBar)
    {

        titleBar.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();

            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            }
        });
    }
}
