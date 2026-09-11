package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AnnuncioCardController {
    public void eliminarAnuncio(){

    }
    public void editarAnuncio(){

    }
    public void mostrarOpciones(){

    }
    @FXML
    private Label lblAutor;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblContenido;

    public void setData(
            String autor,
            String titulo,
            String contenido,
            String fecha
    ) {
        lblAutor.setText(autor);
        lblTitulo.setText(titulo);
        lblContenido.setText(contenido);
        lblFecha.setText(fecha);
    }

}
