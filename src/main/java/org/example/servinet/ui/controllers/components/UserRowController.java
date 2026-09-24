package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class UserRowController {
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Label userRol;

    public void initialize(){

    }

    public void setUser( String nombre, String email,String rol){
        this.userName.setText(nombre);
        this.userEmail.setText(email);
        this.userRol.setText(rol);
    }
}
