package org.example.servinet.utils;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MouseMove {

    private double x;
    private double y;
    private HBox titleBar;
    private AnchorPane panelPrincipal;


    public void ControlHBox(HBox title)
    {
        this.titleBar = title;

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

    public void ControlAnchorPane(AnchorPane Pane)
    {
        this.panelPrincipal = Pane;
        panelPrincipal.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        panelPrincipal.setOnMouseDragged(event -> {
            Stage stage = (Stage) panelPrincipal.getScene().getWindow();

            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);
            }
        });
    }
}
