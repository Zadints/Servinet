package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;

public class AdministrationController {
    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
    @FXML
    private void userCreate() {
        indexController.abrirModal(FormType.USER_CREATE);

    }
}
