package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class DashboardController {

    private boolean alternateStatus;
    @FXML
    private VBox incomeView;

    @FXML
    private VBox techniciansView;

    @FXML
    private VBox sellersView;
    @FXML
    private Button btnAlternate;

    public void initialize() {
        alternateStatus = true;
        alternateOptions();
    }


    public void alternateOptions(){

        if (alternateStatus)  {
            incomeView.setVisible(true);
            incomeView.setManaged(true);

            techniciansView.setVisible(false);
            techniciansView.setManaged(false);

            sellersView.setVisible(false);
            sellersView.setManaged(false);
            alternateStatus = false;
            btnAlternate.setText(" Ver tops ");
            return;
        }
        incomeView.setVisible(false);
        incomeView.setManaged(false);

        techniciansView.setVisible(true);
        techniciansView.setManaged(true);

        sellersView.setVisible(true);
        sellersView.setManaged(true);
        alternateStatus = true;
        btnAlternate.setText(" Ver gráfico ");
    }
}
