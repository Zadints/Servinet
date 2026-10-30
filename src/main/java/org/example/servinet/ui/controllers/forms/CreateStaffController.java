package org.example.servinet.ui.controllers.forms;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.application.dto.StaffDto;
import org.example.servinet.application.usecase.api.ConsultDniUseCase;
import org.example.servinet.application.usecase.role.GetRoleUseCase;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.application.usecase.staff.RegisterStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;
import org.example.servinet.ui.validators.InputValidator;


import java.io.IOException;

public class CreateStaffController {

    @FXML Button btnClose;
    @FXML Button btnCreateStaff;
    @FXML TextField txtUserDni;
    @FXML TextField txtUserName;
    @FXML TextField txtUserLastName;
    @FXML TextField txtUserPhone;
    @FXML ComboBox<Role> cbxUserRole;
    @FXML TextField txtUserEmail;
    @FXML
    GridPane menuUsuario;

    private byte[] image;

    private final RegisterStaffUseCase registerStaffUseCase;
    private final ConsultDniUseCase consultDniUseCase;
    private final Navigator navigator;
    private final AppExecutor appExecutor;
    private final GetRoleUseCase roleUseCase;

    public CreateStaffController(RegisterStaffUseCase registerStaffUseCase, ConsultDniUseCase consultDniUseCase, Navigator navigator, AppExecutor appExecutor, GetRoleUseCase roleUseCase) {
        this.registerStaffUseCase = registerStaffUseCase;
        this.consultDniUseCase = consultDniUseCase;
        this.navigator = navigator;
        this.appExecutor = appExecutor;
        this.roleUseCase = roleUseCase;
    }

    @FXML public void initialize() {

        MouseMove newMove = new MouseMove(menuUsuario);
        cbxUserRole.getItems().setAll(roleUseCase.getRoles());

        newMove.start();

        txtUserDni.textProperty().addListener(
                (observable, oldValue, newValue) -> {

            if (newValue.length() == 8) {

                Task<DniDataDto> task = new Task<DniDataDto>() {
                    @Override
                    protected DniDataDto call() throws Exception {
                        return consultDniUseCase.execute(newValue);
                    }
                };
                task.setOnSucceeded(event -> {

                    DniDataDto dto = task.getValue();
                    if (dto == null) return;

                    txtUserName.setText(dto.getNombres());
                    txtUserLastName.setText(dto.getApellidoMaterno() + " " + dto.getApellidoPaterno());
                });

                appExecutor.execute(task);
            }
        });
    }

    public void createStaff() {

        if (cbxUserRole.getValue() == null) {
            try {
                navigator.showError(
                        "Debes seleccionar un rol",
                        "Error al registrar usuario",
                        (Stage) btnCreateStaff.getScene().getWindow());
            }catch (IOException e){
                System.out.println("Debes seleccionar un rol" + e.getMessage());
            }

            return;
        }

        StaffDto dto = new StaffDto(
            txtUserDni.getText(),
                txtUserName.getText(),
                txtUserLastName.getText(),
                txtUserPhone.getText(),
                cbxUserRole.getValue(),
                txtUserEmail.getText()
        );

        try {
            registerStaffUseCase.register(dto);
            closeForm();
        } catch (Exception e) {
            try {
                navigator.showError(
                        e.getMessage(),
                        "Error al registrar usuario",
                        (Stage) btnCreateStaff.getScene().getWindow());
            }catch (IOException j){
                System.out.println("Debes seleccionar un rol" + j.getMessage());
            }

        }
    }


    public void closeForm(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }


}
