package org.example.servinet.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.servinet.utils.MouseMove;

public class FormController {

    @FXML
    public void initialize() {
    }

    @FXML
    private GridPane formContent;

    public void setParent(StackPane parent) {

        formContent.prefWidthProperty().bind(
                parent.widthProperty().multiply(0.5)
        );

        formContent.prefHeightProperty().bind(
                parent.heightProperty().multiply(0.5)
        );
    }
}
