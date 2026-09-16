package org.example.servinet.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

public class SplashController {
    @FXML
    private ImageView imgLogo;

    @FXML
    private Label lblApplicationName;

    @FXML
    private Label lblStatus;

    @FXML
    private ProgressBar progressBar;

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setStatus(String status) {
        lblStatus.setText(status);
    }
}
