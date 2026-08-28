package org.example.servinet.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.utils.MouseMove;

public class AntenasController {
    @FXML
    private Label lblAntenas;

    public void initialize() {
        LoadAntennas();
    }

    private void LoadAntennas(){

        lblAntenas.setVisible(true);
    }
}
