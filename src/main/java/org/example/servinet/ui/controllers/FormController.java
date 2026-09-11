package org.example.servinet.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.servinet.core.domain.enums.FormType;

public class FormController {

    private StackPane parent;
    @FXML
    private GridPane menuUsuario;
    @FXML
    private GridPane menuAntena;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtUserEmail;
    @FXML
    private ComboBox cbxUserRole;
    @FXML
    private PasswordField txtUserPassword;


    public void setParent(StackPane parent, FormType type) {

        this.parent = parent;

        switch (type){
            case FormType.USER_CREATE:

                menuUsuario.setVisible(true);
                menuUsuario.setManaged(true);
                menuAntena.setVisible(false);
                menuAntena.setManaged(false);

                menuUsuario.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
                break;
            case FormType.ANTENNA:

                menuUsuario.setVisible(false);
                menuUsuario.setManaged(false);
                menuAntena.setVisible(true);
                menuAntena.setManaged(true);

                menuAntena.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );

                menuAntena.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.4)
                );
                break;
            default:
                break;
        }

    }




    public void selectUserImage(){

    }
    public void createUser(){

    }
    public void selectAntennaImage(){

    }
    public void createAntenna(){

    }


    public void closeForm(){
        parent.getChildren().clear();
        parent.setVisible(false);
    }
}
