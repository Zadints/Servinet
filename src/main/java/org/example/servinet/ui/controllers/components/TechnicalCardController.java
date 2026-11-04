package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.domain.entities.technicians.Technical;


public class TechnicalCardController {

    @FXML private Label lblTechnicalName;
    @FXML private Label lblTechnicalStatus;
    @FXML private Label lblTechnicalCountInstalled;
    private Technical technicalData;

    public void setTechnical(Technical technical){
        this.lblTechnicalCountInstalled.setText(technical.getInstalledCount() + "");

        this.lblTechnicalName.setText(technical.getTechnical().getDisplay());
        this.lblTechnicalStatus.setText("ID: " + technical.getTechnicalUuid());
        this.technicalData = technical;
    }
    public Technical getTechnical() {
        return technicalData;
    }

    @FXML public void showTechnical(){
    }


}
