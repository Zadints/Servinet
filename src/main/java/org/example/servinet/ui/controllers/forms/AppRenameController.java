package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.servinet.application.usecase.appsetting.AppGeneralUseCase;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;

public class AppRenameController {

    public Label lblErrorRenameApp;
    public TextField txtAppName;
    public GridPane menuAppRename;
    public Button btnRename;
    public Button btnClose;

    private final AppGeneralUseCase appGeneralUseCase;
    private final Navigator navigator;

    public AppRenameController(AppGeneralUseCase appGeneralUseCase, Navigator navigator) {
        MouseMove mouseMove = new MouseMove(menuAppRename);
        mouseMove.start();
        this.appGeneralUseCase = appGeneralUseCase;
        this.navigator = navigator;
    }

    public void renameApp(ActionEvent actionEvent) {

        try {
            appGeneralUseCase.appRename(txtAppName.getText());
        }catch (PermissionException | IllegalArgumentException e){
            try {System.out.println(e.getMessage());
                System.out.println(e.getCause());
                navigator.showError(e.getMessage(), "Error al cambiar nombre de la app", (Stage) btnRename.getScene().getWindow());


            }catch (IOException j){
                System.out.println(j.getMessage());
                System.out.println( j.getCause());
            }
        }
    }

    public void closeForm(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
