package org.example.servinet.ui.controllers.center;

import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;

public class AntenasController {

    private IndexController indexController;
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    public void deleteAntenna(){

    }
    public void editAntenna(){
        indexController.abrirModal(FormType.ANTENNA_EDIT);
    }
    public void finishMaintenance(){

    }
    public void startMaintenance(){

    }
    public void toggleAntenna(){

    }
    public void showAntennaOptions(){

    }
    public void createAntenna(){
        indexController.abrirModal(FormType.ANTENNA_CREATE);
    }
    public void previousAntenna(){

    }
    public void nextAntenna(){

    }

}
