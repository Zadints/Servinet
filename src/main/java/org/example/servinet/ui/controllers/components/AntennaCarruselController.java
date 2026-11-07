package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.Node;
import javafx.scene.control.Button;

import javafx.scene.layout.VBox;
import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.Permission;

import java.util.function.Consumer;

public class AntennaCarruselController {

    public VBox cardAntenna;
    @FXML private ImageView imgAntenna1;
    @FXML private Label lblAntennaName;
    @FXML private Label lblAntennaLocate;
    @FXML private Label lblAntennaStatus;
    @FXML private Label lblClientsAntenna;

    @FXML private Button btnCardStartMaintenance;
    @FXML private Button btnCardToggleAntenna;


    private Consumer<Antenna> onAntennaSelected;
    private Antenna antenna;

    public void setAntenna(Antenna antenna) {
        this.antenna = antenna;
    }

    public void setOnAntennaSelected(Consumer<Antenna> callback) {
        this.onAntennaSelected = callback;
    }

    @FXML
    private void initialize() {
        cardAntenna.setOnMouseClicked(event -> {
            if (onAntennaSelected != null && antenna != null) {
                onAntennaSelected.accept(antenna);
            }
        });
    }
    
    @FXML protected void showAntennaOptions(){

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
