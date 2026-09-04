package org.example.servinet.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import org.example.servinet.domain.entities.antenna.Antenna;
import org.example.servinet.application.services.AntennasServices;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.servinet.controllers.components.AntenaCardController;
import org.example.servinet.utils.ImageConverter;

import java.io.IOException;
import java.util.List;

public class AntenasController {
    @FXML
    private Label lblAntenas;
    @FXML
    private FlowPane flowAntenas;
    @FXML
    public void initialize() {
        LoadAntennas();
    }


    private void LoadAntennas() {

        List<Antenna> tempAntenas = AntennasServices.getAllAntennas();
        for (Antenna ant : tempAntenas){
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/ui/components/antena-card.fxml")
                );

                VBox card = loader.load();

                AntenaCardController controller = loader.getController();

                controller.setAntena(
                        ImageConverter.toImage(ant.getImage()),
                        ant.getName(),
                        ant.getStatus(),
                        ant.getUuid()
                );

                flowAntenas.getChildren().add(card);

            } catch (IOException e) {
                //renderizar erro h.noLoadCards(e, ant.getName());
            }
        }

    }


    @FXML
    private void crearAntena(){

    }
}
