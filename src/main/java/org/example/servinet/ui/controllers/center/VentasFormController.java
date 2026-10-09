package org.example.servinet.ui.controllers.center;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.example.servinet.ui.controllers.IndexController;

public class VentasFormController {
    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
    public void selectRouterImage(){

    }
    public void removeRouterImage(){

    }
    public void closeForm(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar salida");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que quieres salir?");

        alert.getButtonTypes().setAll(
                new ButtonType("Sí, salir", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                indexController.renderizarFxml("sell.fxml");
            }
        });
    }
    public void registerSale(){

    }


}
