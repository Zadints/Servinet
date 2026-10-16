package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.example.servinet.core.domain.entities.Antenna;
import org.example.servinet.core.domain.entities.technicians.Technical;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AntennaCarruselController;
import org.example.servinet.ui.controllers.components.TechnicalCardController;

import java.io.IOException;

public class AntenasController {

    @FXML private IndexController indexController;
    @FXML private HBox antennaList;

    @FXML public void initialize(){
        loadAntennas();
    }

    @FXML public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
    @FXML protected void deleteAntenna(){

    }
    @FXML protected void editAntenna(){
        indexController.abrirModal(FormType.ANTENNA_EDIT);
    }
    @FXML protected void finishMaintenance(){

    }
    @FXML protected void startMaintenance(){

    }
    @FXML protected void createAntenna(){
        indexController.abrirModal(FormType.ANTENNA_CREATE);
    }
    @FXML protected void previousAntenna(){

    }
    @FXML protected void nextAntenna(){

    }

    private void loadAntennas(){

        //obtener valores del USecase;

        for (int i = 0; i < 5; i++){
            renderingAntennas();
        }
    }

    private void renderingAntennas(){
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/antena-carrusel.fxml")
            );



            Node row = loader.load();
            AntennaCarruselController con = loader.getController();

            con.setImgAntenna1(null);
            con.setLblAntennaLocate("a");
            con.setLblAntennaName("a");
            con.setLblAntennaStatus("a");
            con.setLblClientsAntenna("a");

            antennaList.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
