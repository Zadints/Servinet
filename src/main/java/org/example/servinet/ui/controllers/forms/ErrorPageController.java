package org.example.servinet.ui.controllers.forms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.servinet.ui.events.MouseMove;

public class ErrorPageController {

    @FXML private AnchorPane root;
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnClose;

    public void initialize() {
        MouseMove mouseMove = new MouseMove(root);
        mouseMove.start();
    }
    public void setException(String error, String title) {
        lblMessage.setText(error);
        lblTitle.setText(title);
    }

    @FXML protected void onClose(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML protected void onDetails(){
        
    }
}
