package org.example.servinet.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class AntenasController {
    @FXML
    private Label lblAntenas;
    @FXML
    private FlowPane flowAntenas;
    @FXML
    public void initialize() {
        LoadAntennas();
    }


    private void LoadAntennas(){

        lblAntenas.setVisible(true);
    }


}
