package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.servinet.application.usecase.antenna.MaintenanceAntennaUseCase;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.ui.events.MouseMove;

public class AntennaMaintenanceController {
    @FXML private GridPane menuAntennaStartMant;
    @FXML private  Button btnClose;
    private Antenna antenna;
    private final MaintenanceAntennaUseCase maintenanceAntennaUseCase;

    public AntennaMaintenanceController(MaintenanceAntennaUseCase maintenanceAntennaUseCase) {
        this.maintenanceAntennaUseCase = maintenanceAntennaUseCase;
    }

    public void setAntenna(Antenna antenna) {
        this.antenna = antenna;
    }

    public void initialize() {
        MouseMove mouseMove = new MouseMove(menuAntennaStartMant);
        mouseMove.start();
    }
    public void saveMaintenance(ActionEvent actionEvent) {

        try {
            maintenanceAntennaUseCase.on(antenna);
        }catch (Exception e){
            System.out.println(e.getMessage() + e.getCause());
        }
        closeForm(actionEvent);
    }

    public void closeForm(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
