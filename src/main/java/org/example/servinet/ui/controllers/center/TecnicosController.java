package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.servinet.core.application.usecase.TechnicalUseCase;
import org.example.servinet.core.domain.entities.Staff;
import org.example.servinet.core.domain.entities.technicians.Order;
import org.example.servinet.core.domain.entities.technicians.Technical;
import org.example.servinet.core.domain.enums.TechnicalStatus;
import org.example.servinet.ui.controllers.components.TechnicalCardController;
import org.example.servinet.ui.controllers.components.TechnicalHistoryController;

import java.io.IOException;
import java.util.List;

public class TecnicosController {
    @FXML private ComboBox<TechnicalStatus> cmbTechnicalFilter;
    @FXML private VBox technicianList;
    /*Information technical*/
    @FXML private Label lblTechnicianName;
    @FXML private Label lblTechnicianEmail;
    @FXML private Label lblTechnicianPhone;
    @FXML private Label lblTechnicianUuid;

    @FXML private Label lblCurrentInstallation;
    @FXML private Label lblCurrentInstallationStatus;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblCurrentLocation;
    @FXML private Button btnRefreshHistorial;

    @FXML protected void initialize(){
        cmbTechnicalFilter.getItems().setAll(TechnicalStatus.values());
        loadListTechnical();
    }

    @FXML protected void clearTechnicalFilters(){

    }
    @FXML protected void showTechnicalInfo(){

    }
    @FXML protected void showInstallationHistory(){

    }


    /*
    * Methods from load
    */

    private void loadListTechnical(){

        List<Technical> technicals = TechnicalUseCase.getTechnicalsAll();
        for (Technical t: technicals){
            loadTechnical(t);
        }

    }


    private void loadTechnical(Technical technical){
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/technical-card.fxml")
            );

            Node row = loader.load();
            TechnicalCardController controller = loader.getController();

            controller.setTechnical(technical);

            row.setOnMouseClicked(event -> {

                Technical selectedTechnical = controller.getTechnical();
                renderTechnicalForm(selectedTechnical);
            });

            technicianList.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void renderTechnicalForm(Technical t){

        loadTechnicalInfo(t.getTechnical());
        loadActualInstalled(t.getInstNow());
        loadInstalledHistory(t.getAllInstLog());
    }

    private void loadTechnicalInfo(Staff technical){

        if ( technical == null){
            lblTechnicianName.setText("Error LOADER ...");
            lblTechnicianUuid.setText("Error LOADER ...");
            lblTechnicianPhone.setText("Error LOADER ...");
            lblTechnicianEmail.setText("Error LOADER ...");
            return;
        }

        lblTechnicianName.setText(technical.getName());
        lblTechnicianUuid.setText(technical.getUuid());
        lblTechnicianPhone.setText("No defined");
        lblTechnicianEmail.setText(technical.getEmail());
    }

    private void loadActualInstalled(Order actualOrder){
        if (actualOrder == null) {
            lblCurrentInstallation.setText("Sin instalación");
            lblCurrentInstallationStatus.setText("Sin actividad");
            lblCurrentClient.setText("Sin información");
            lblCurrentLocation.setText("Sin información");
            return;
        }
        lblCurrentLocation.setText(actualOrder.getClientAddress());
        lblCurrentClient.setText(actualOrder.getClientName());
        lblCurrentInstallationStatus.setText(actualOrder.getOrderStatus());
        lblCurrentInstallation.setText(actualOrder.getOrderId());
    }

    private void loadInstalledHistory(List<Order>actualOrder){

        if (actualOrder.isEmpty()) return;

        for (Order o : actualOrder)
            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/servinet/components/technical-card.fxml")
                );

                Node row = loader.load();
                TechnicalHistoryController controller = loader.getController();

                controller.setTechnicalHistory(o);

                row.setOnMouseClicked(event -> {

                    Technical selectedTechnical = controller.getTechnical();
                    renderTechnicalForm(selectedTechnical);
                });

                technicianList.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
    }


}
