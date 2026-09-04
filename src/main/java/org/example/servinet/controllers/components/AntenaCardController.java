package org.example.servinet.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.servinet.domain.entities.antenna.Antenna;
import org.example.servinet.domain.enums.antenna.StatusAntenna;


public class AntenaCardController {
    @FXML
    private Label lblName;
    @FXML
    private Label lblStatus;
    @FXML
    private ImageView imgAntenna;

    private String uuid;

    public void setAntena(Image imgAntenna, String name, StatusAntenna status, String uuid ) {
        this.lblName.setText(name);
        this.lblStatus.setText(status.toString());
        this.uuid = uuid;
        this.imgAntenna.setImage(imgAntenna);
    }

    @FXML
    private void editar() {
        System.out.println("Editar antena");
    }

    public String getUuid() {
        return uuid;
    }
}
