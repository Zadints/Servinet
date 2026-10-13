package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
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

    @FXML private Label lblActiveTechnicians;
    @FXML private Label lblAvailableTechnicians;
    @FXML private Label lblTechniciansWorking;

    /*Information technical*/
    @FXML private Label lblTechnicianName;
    @FXML private Label lblTechnicianEmail;
    @FXML private Label lblTechnicianPhone;
    @FXML private Label lblTechnicianUuid;

    @FXML private Label lblNoHistory;
    @FXML private Label lblCurrentInstallation;
    @FXML private Label lblCurrentInstallationStatus;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblCurrentLocation;
    @FXML private Button btnRefreshHistorial;
    @FXML private VBox installationHistoryList;
    private Technical actualRenderTechnical;

    @FXML protected void initialize(){
        cmbTechnicalFilter.getItems().setAll(TechnicalStatus.values());
        lblNoHistory.setVisible(false);
        lblNoHistory.setManaged(false);
        lblActiveTechnicians.setText(TechnicalUseCase.getTechnicalAlreadyExist() + "");
        lblAvailableTechnicians.setText(TechnicalUseCase.getTechnicalAvailable() + "");
        lblTechniciansWorking.setText(TechnicalUseCase.getTechnicalBusy() + "");
        this.actualRenderTechnical = null;
        loadListTechnical();
    }

    @FXML protected void clearTechnicalFilters(){
        clearTechnicalForm();
    }
    @FXML protected void showInstallationHistory(){
        loadInstalledHistory(actualRenderTechnical);
    }
    @FXML protected void searchTechnicalFilters(){
        loadListTechnical(cmbTechnicalFilter.getValue());
    }

    /*
    * Methods from load
    */

    private void loadListTechnical() {
        technicianList.getChildren().clear();

        List<Technical> technicals = TechnicalUseCase.getTechnicalsAll();

        if (technicals == null || technicals.isEmpty()) {
            showEmptyTechnicians("No hay técnicos registrados");
            return;
        }

        for (Technical t : technicals) {
            loadTechnical(t);
        }
    }

    private void loadListTechnical(TechnicalStatus status) {

        if (status == null) return;

        technicianList.getChildren().clear();

        List<Technical> technicals = TechnicalUseCase.getTechnicalsAll();

        if (technicals == null || technicals.isEmpty()) {
            showEmptyTechnicians("No hay técnicos registrados");
            return;
        }

        List<Technical> filtered = technicals.stream()
                .filter(t -> t.getStatus() == status)
                .toList();

        if (filtered.isEmpty()) {
            showEmptyTechnicians("No hay técnicos con este estado");
            return;
        }

        for (Technical t : filtered) {
            loadTechnical(t);
        }
    }

    private void showEmptyTechnicians(String message) {
        Label lblEmpty = new Label(message);
        lblEmpty.getStyleClass().add("tertiary-text");
        lblEmpty.setWrapText(true);
        lblEmpty.setMaxWidth(300);
        lblEmpty.setAlignment(Pos.CENTER);
        lblEmpty.setTextAlignment(TextAlignment.CENTER);

        StackPane container = new StackPane(lblEmpty);
        container.setMinHeight(180);
        container.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(container, Priority.ALWAYS);

        technicianList.getChildren().add(container);
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

        this.actualRenderTechnical = t;
        loadTechnicalInfo(t.getTechnical());
        loadActualInstalled(t.getInstNow());
        loadInstalledHistory(t);
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

    private void loadActualInstalled(Order actualOrder) {

        if (actualOrder == null) {
            lblCurrentInstallation.setText("Sin instalación");
            lblCurrentInstallationStatus.setText("Sin actividad");
            lblCurrentClient.setText("Sin información");
            lblCurrentLocation.setText("Sin información");
            return;
        }

        lblCurrentLocation.setText(
                valueOrDefault(actualOrder.getClientAddress(), "Sin dirección")
        );

        lblCurrentClient.setText(
                valueOrDefault(actualOrder.getClientName(), "Sin cliente")
        );

        lblCurrentInstallationStatus.setText(
                valueOrDefault(actualOrder.getOrderStatus(), "Sin estado")
        );

        lblCurrentInstallation.setText(
                valueOrDefault(actualOrder.getOrderId(), "Sin código")
        );
    }
    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
    private void loadInstalledHistory(Technical technical){

        if ( technical == null)return;

        List<Order> actualOrder =  technical.getAllInstLog();

        if (actualOrder == null || actualOrder.isEmpty()) {
            lblNoHistory.setVisible(true);
            lblNoHistory.setManaged(true);
            return;
        }

        for (Order o : actualOrder)
            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/servinet/components/technical-card.fxml")
                );

                Node row = loader.load();
                TechnicalHistoryController controller = loader.getController();

                controller.setTechnicalHistory(o);

                installationHistoryList.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void clearTechnicalForm() {

        cmbTechnicalFilter.getSelectionModel().clearSelection();
        this.actualRenderTechnical = null;

        lblTechnicianName.setText("");
        lblTechnicianEmail.setText("");
        lblTechnicianPhone.setText("");
        lblTechnicianUuid.setText("");

        lblCurrentInstallation.setText("");
        lblCurrentInstallationStatus.setText("");
        lblCurrentClient.setText("");
        lblCurrentLocation.setText("");

        installationHistoryList.getChildren().clear();

        lblNoHistory.setVisible(true);
        lblNoHistory.setManaged(true);
    }
}
