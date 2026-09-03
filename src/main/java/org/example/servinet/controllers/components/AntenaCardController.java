package org.example.servinet.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.*;


public class AntenaCardController {
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblEstado;

    AntenaCardController(String nombre, String estado) {
        lblNombre.setText(nombre);
        lblEstado.setText(estado);
    }

    @FXML
    private void editar() {
        System.out.println("Editar antena");
    }
}
