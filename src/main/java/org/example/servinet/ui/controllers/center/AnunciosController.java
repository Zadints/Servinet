package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.example.servinet.ui.controllers.components.AnnuncioCardController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class AnunciosController {

    @FXML
    private VBox anunciosContainer;

    public void createAnnounce(){
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/servinet/components/anuncio-card.fxml"
                    )
            );

            Parent anuncio = loader.load();

            AnnuncioCardController controller =
                    loader.getController();

            controller.setData(
                    "Augusto",
                    "Nuevo anuncio de prueba",
                    "Este es un anuncio creado únicamente para probar el feed.",
                    "Hace unos segundos"
            );

            anunciosContainer.getChildren().add(0, anuncio);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
