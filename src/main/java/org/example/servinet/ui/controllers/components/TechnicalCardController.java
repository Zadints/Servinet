package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.domain.entities.technicians.Technical;


public class TechnicalCardController {

    @FXML private Label lblTechnicalName;
    @FXML private Label lblTechnicalStatus;
    @FXML private Label lblTechnicalCountInstalledToday;
    @FXML private Label lblTechnicalCountInstalled;
    private Technical technicalData;

    public void setTechnical(Technical technical){
        this.lblTechnicalCountInstalled.setText(
                String.valueOf(
                        technical.getAllInstLog() != null
                                ? technical.getAllInstLog().size()
                                : 0
                )
        );
        this.lblTechnicalCountInstalledToday.setText(
                String.valueOf(
                        technical.getInstEnded() != null
                                ? technical.getInstEnded().size()
                                : 0
                )
        );
        this.lblTechnicalName.setText(technical.getTechnical().getDisplay());
        this.lblTechnicalStatus.setText(technical.getStatus().toString());
        this.technicalData = technical;
    }
    public Technical getTechnical() {
        return technicalData;
    }

    @FXML public void showTechnical(){
    }


}
