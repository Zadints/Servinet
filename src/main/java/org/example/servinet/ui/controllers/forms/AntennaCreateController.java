package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.application.usecase.antenna.CreateAntennaUseCase;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.ui.events.ImageConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

public class AntennaCreateController {
    public Label lblErrorCreateAntena;
    public Button btnCreate;
    @FXML private ComboBox<Integer> cmbAntennaPriorityCreate;
    @FXML private  Button btnClose;
    @FXML private TextField txtAntennaCName;
    @FXML private CheckBox chkAntennaRepair;
    @FXML private CheckBox chkAntennaMaintenance;
    @FXML private TextField txtAntennaDaysOn;
    @FXML private DatePicker dpAntennaLastMaintenance;
    @FXML private ComboBox<StatusAntenna> cbxAntennaStatus;
    @FXML private ComboBox<LocalTime> cbxMaintenanceTime;
    @FXML private Button btnSelectImage;
    @FXML private ImageView imgPreview;
    @FXML private GridPane menuAntennaCreate;
    @FXML private TextField txtAntennaLatitude;
    @FXML private TextField txtAntennaLongitude;

    private final CreateAntennaUseCase createAntennaUseCase;
    private byte[] image;
    public AntennaCreateController(CreateAntennaUseCase createAntennaUseCase) {
        this.createAntennaUseCase = createAntennaUseCase;

    }
    public void initialize() {
        cmbAntennaPriorityCreate.getItems().setAll(1, 2, 3);
        cmbAntennaPriorityCreate.getSelectionModel().select(1);
        cbxAntennaStatus.getItems().setAll(StatusAntenna.values());
        cbxAntennaStatus.getSelectionModel().select(StatusAntenna.ACTIVE);
        cbxMaintenanceTime.getItems().setAll(
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0)
        );
        cbxAntennaStatus.getSelectionModel().select(3);
    }

    public void selectAntennaImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());
        if (file != null) {

            try {
                image = ImageConverter.toBytes(file.toPath());
            }catch (IOException e){
                System.out.println(e.getMessage());
            }

            ((Button) event.getSource()).setText(file.getName());
        }
    }

    public void createAntenna(ActionEvent actionEvent) {

        try {
            if (cmbAntennaPriorityCreate.getValue() == null) {
                lblErrorCreateAntena.setText("Selecciona una prioridad");
                lblErrorCreateAntena.setVisible(true);
                return;
            }

            if (cbxAntennaStatus.getValue() == null) {
                lblErrorCreateAntena.setText("Selecciona un estado");
                lblErrorCreateAntena.setVisible(true);
                return;
            }

            Double latitude = parseCoordinate(
                    txtAntennaLatitude.getText(), "Latitud", -90, 90
            );

            Double longitude = parseCoordinate(
                    txtAntennaLongitude.getText(), "Longitud", -180, 180
            );
            Integer daysOn = parseDaysOn(txtAntennaDaysOn.getText());
            createAntennaUseCase.create(
                    new AntennaDto(
                            cmbAntennaPriorityCreate.getValue(),
                            txtAntennaCName.getText(),
                            chkAntennaRepair.isSelected(),
                            chkAntennaMaintenance.isSelected(),
                            dpAntennaLastMaintenance.getValue(),
                            cbxMaintenanceTime.getValue(),
                            image,
                            daysOn,
                            0,
                            cbxAntennaStatus.getValue(),
                            latitude,
                            longitude
                    )
            );

            closeForm(actionEvent);
        } catch (IllegalArgumentException e) {
            lblErrorCreateAntena.setText(e.getMessage());
            lblErrorCreateAntena.setVisible(true);
        } catch (Exception e) {
            lblErrorCreateAntena.setText("No se pudo crear la antena");
            lblErrorCreateAntena.setVisible(true);
            e.printStackTrace();
        }
    }

    private Double parseCoordinate(String value, String fieldName, double min, double max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            double coordinate = Double.parseDouble(value.trim());

            if (!Double.isFinite(coordinate) || coordinate < min || coordinate > max) {
                throw new IllegalArgumentException(
                        fieldName + " debe estar entre " + min + " y " + max
                );
            }

            return coordinate;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    fieldName + " debe ser un número válido"
            );
        }
    }

    private Integer parseDaysOn(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int days = Integer.parseInt(value.trim());

            if (days < 0) {
                throw new IllegalArgumentException(
                        "Los días encendida no pueden ser negativos"
                );
            }

            return days;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Los días encendida deben ser un número entero"
            );
        }
    }
    public void closeForm(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    public void clearForm(ActionEvent actionEvent) {

    }
}
