package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.Node;
import javafx.scene.control.Button;

import org.example.servinet.core.application.security.PermissionPolicy;
import org.example.servinet.core.domain.enums.Permission;

public class AntennaCarruselController {

    @FXML private ImageView imgAntenna1;
    @FXML private Label lblAntennaName;
    @FXML private Label lblAntennaLocate;
    @FXML private Label lblAntennaStatus;
    @FXML private Label lblClientsAntenna;

    @FXML private Button btnCardStartMaintenance;
    @FXML private Button btnCardToggleAntenna;

    @FXML protected void showAntennaOptions(){

    }
    @FXML protected void startMaintenance(){

    }
    @FXML protected void toggleAntenna(){

    }

    public void setLblAntennaName(String text) {
        this.lblAntennaName.setText(text);
    }

    public void setLblAntennaLocate(String text) {
        this.lblAntennaLocate.setText(text);
    }

    public void setLblAntennaStatus(String text) {
        this.lblAntennaStatus.setText(text);
    }

    public void setLblClientsAntenna(String text) {
        this.lblClientsAntenna.setText(text);
    }

    public void setImgAntenna1(Image img) {
        if (img == null) return;

        this.imgAntenna1.setImage(img);
    }


    public void configurarPermisos(PermissionPolicy policy) {

        boolean iniciarMantenimiento = policy.allows(
                Permission.ANT_START_MAINTE
        );

        boolean cambiarEstado = policy.allowsAny(
                Permission.ANT_GO_ACTIVE,
                Permission.ANT_GO_DESACTIVE
        );

        mostrar(
                btnCardStartMaintenance,
                iniciarMantenimiento
        );

        mostrar(
                btnCardToggleAntenna,
                cambiarEstado
        );

        // Temporal: ambas funciones siguen vacías
        // en el controlador original.
        btnCardStartMaintenance.setDisable(true);
        btnCardToggleAntenna.setDisable(true);
    }


    private void mostrar(Node nodo, boolean permitido) {

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }

}
