package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.core.domain.entities.antenna.Antenna;
import org.example.servinet.core.application.usecase.AntennasUseCase;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.servinet.ui.controllers.components.AntenaCardController;
import org.example.servinet.core.domain.utils.ImageConverter;

import java.io.IOException;
import java.util.List;

public class AntenasController {
    @FXML
    private Label lblAntenas;
    @FXML
    private FlowPane flowAntenas;
    private IndexController indexController;


    @FXML
    public void initialize() {
        LoadAntennas();
    }


    private void LoadAntennas() {

        List<Antenna> tempAntenas = AntennasUseCase.getAllAntennas();

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

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    private void crearAntena() {
        indexController.abrirModal(FormType.ANTENNA);
    }
}
