package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class AntennaCarruselController {

    @FXML private ImageView imgAntenna1;
    @FXML private Label lblAntennaName;
    @FXML private Label lblAntennaLocate;
    @FXML private Label lblAntennaStatus;
    @FXML private Label lblClientsAntenna;

    @FXML protected void showAntennaOptions(){

    }
    @FXML protected void startMaintenance(){

    }
    @FXML protected void toggleAntenna(){

    }

    public void setLblAntennaName(String text) {
        this.lblAntennaName.setText(text);
    }

    public void setLblAntennaLocate(String text) {
        this.lblAntennaLocate.setText(text);
    }

    public void setLblAntennaStatus(String text) {
        this.lblAntennaStatus.setText(text);
    }

    public void setLblClientsAntenna(String text) {
        this.lblClientsAntenna.setText(text);
    }

    public void setImgAntenna1(Image img) {
        if (img == null) return;

        this.imgAntenna1.setImage(img);
    }
}
