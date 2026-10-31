package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.application.dto.EditAntennaDto;
import org.example.servinet.application.usecase.antenna.EditAntennaUseCase;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.File;
import java.io.IOException;

public class AntennasEditController {


    @FXML private Button btnEdit;
    @FXML private Button btnImage;
    @FXML private TextField txtEditAntennaName;
    @FXML private TextField txtEditAntennaLatitude;
    @FXML private TextField txtEditAntennaLongitude;
    @FXML private GridPane menuAntennaEdit;
    @FXML private Button btnClose;
    private Antenna antenna;
    private byte[] image;

    private final EditAntennaUseCase editAntennaUseCase;
    private final Navigator navigator;

    public AntennasEditController(EditAntennaUseCase editAntennaUseCase, Navigator navigator) {
        this.navigator = navigator;

        this.editAntennaUseCase = editAntennaUseCase;
    }

    @FXML
    public void initialize() {
        MouseMove mouseMove = new MouseMove(menuAntennaEdit);
        mouseMove.start();
    }

    public void setAntenna(Antenna antenna) {
        this.antenna = antenna;
    }

    @FXML
    public void selectEditAntennaImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(btnImage.getScene().getWindow());
        if (file != null) {

            try {
                image = ImageConverter.toBytes(file.toPath());
            }catch (IOException e){
                System.out.println(e.getMessage());
            }

            ((Button) event.getSource()).setText(file.getName());
        }
    }

    @FXML
    public void editAntenna(ActionEvent event) {

        try {

            Double latitude = parseCoordinate(
                    txtEditAntennaLatitude.getText(), "Latitud", -90, 90
            );
            Double longitude = parseCoordinate(
                    txtEditAntennaLongitude.getText(), "Longitud", -180, 180
            );
            editAntennaUseCase.edit(new EditAntennaDto(
                    txtEditAntennaName.getText(),
                    latitude,
                    longitude,
                    image,
                    antenna
            ), antenna);
            closeForm(event);
        }catch (Exception e){
            System.out.println(e.getMessage() + e.getCause()); try {
                navigator.showError(
                        e.getMessage(),
                        "Error al registrar usuario",
                        (Stage) btnEdit.getScene().getWindow());
            }catch (IOException j){
                System.out.println("Debes seleccionar un rol" + j.getMessage());
            }
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

    @FXML
    public void closeForm(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}