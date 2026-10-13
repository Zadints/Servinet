package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.example.servinet.core.domain.enums.TechnicianStatus;

import java.lang.ref.PhantomReference;

public class TecnicosController {
    @FXML private ComboBox<TechnicianStatus> cmbTechnicianFilter;

    @FXML protected void initialize(){
        cmbTechnicianFilter.getItems().setAll(TechnicianStatus.values());
    }


    @FXML protected void clearTechnicianFilters(){

    }
    @FXML protected void showTechnicianInfo(){

    }
    @FXML protected void showInstallationHistory(){

    }

}
