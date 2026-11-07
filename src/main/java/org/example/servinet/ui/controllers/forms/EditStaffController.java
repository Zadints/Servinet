package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.servinet.application.usecase.role.GetRoleUseCase;
import org.example.servinet.application.usecase.staff.EditStaffUseCase;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.File;
import java.io.IOException;

public class EditStaffController {

    @FXML private Button btnClose;
    @FXML private TextField txtUserEmailEdit;
    @FXML private ComboBox<Role> cbxUserRoleEdit;
    @FXML private PasswordField txtUserPasswordEdit;
    @FXML private  Button btnSelectImage;
    @FXML private  Button btnEdit;
    @FXML private GridPane menuUserEdit;

    private byte[] image;
    private final EditStaffUseCase editStaffUseCase;
    private final Navigator navigator;
    private Staff staff;
    private final GetRoleUseCase getRoleUseCase;

    public EditStaffController(EditStaffUseCase editStaffUseCase, Navigator navigator, GetRoleUseCase getRoleUseCase) {
        this.editStaffUseCase = editStaffUseCase;
        this.navigator = navigator;
        this.getRoleUseCase = getRoleUseCase;
    }
    public void initialize() {
        MouseMove mouseMove = new MouseMove(menuUserEdit);
        mouseMove.start();
        cbxUserRoleEdit.getItems().setAll(getRoleUseCase.getRoles());
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void selectUserImage(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());
        if (file != null) {

            try {
                image = ImageConverter.toBytes(file.toPath());
            }catch (IOException e){
                System.out.println(e.getMessage());
            }

            ((Button) event.getSource()).setText(file.getName());
        }
    }

    public void editUser() {

        try {
            editStaffUseCase.editUser(
                    staff,
                    txtUserEmailEdit.getText(),
                    cbxUserRoleEdit.getValue(),
                    txtUserPasswordEdit.getText().trim(),
                    image
            );

            closeForm();

            if (!txtUserPasswordEdit.getText().isEmpty()){
                navigator.backPage();
            }

        } catch (Exception e) {
             try {System.out.println(e.getMessage());
                 System.out.println(e.getCause());
                 navigator.showError(e.getMessage(), "Error al editar usuario", (Stage) btnEdit.getScene().getWindow());


             }catch (IOException j){
                 System.out.println(j.getMessage());
                 System.out.println( j.getCause());
            }
        }
    }

    public void closeForm(){
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
