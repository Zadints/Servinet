package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;

public class VentasMenuController {

    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    protected void newContract(){
        indexController.renderizarFxml("sell-form.fxml");
    }
    @FXML
    protected void showSelling(){

    }
    @FXML
    protected void showOffers(){

    }
    @FXML
    protected void showContracts(){

    }
    @FXML
    protected void selectOffer(){

    }
    @FXML
    protected void editContract(){

    }
    @FXML
    protected void showStatistics(){

    }
    @FXML
    protected void showProspects(){

    }

}
