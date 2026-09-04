package org.example.servinet.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.controllers.IndexController;
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

        if (tempAntenas == null || tempAntenas.isEmpty()) return;

        for (Antenna ant : tempAntenas){
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/servinet/components/antena-card.fxml")
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
    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    private void crearAntena() {
        indexController.abrirModal();
    }
}
