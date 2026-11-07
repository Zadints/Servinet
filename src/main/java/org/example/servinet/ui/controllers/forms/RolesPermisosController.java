package org.example.servinet.ui.controllers.forms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.servinet.application.dto.RoleDto;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.ui.events.MouseMove;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;
import java.util.Set;

public class RolesPermisosController {
    @FXML private VBox menuAppRolPermission;
    // Título
    @FXML private Label lblRoleFormTitle;

    // Selección de rol
    @FXML private ComboBox<Role> cbxRoleManage;
    @FXML private Button btnNewRole;
    @FXML private Button btnClose;
    // Datos del rol
    @FXML private TextField txtRoleName;
    @FXML private TextField txtRoleColor;
    @FXML private ComboBox<?> cbxRoleTemplate;

    // Buscador de permisos
    @FXML private TextField txtPermissionSearch;
    @FXML private Label lblPermissionsCount;

    // Lista de permisos
    @FXML private ScrollPane scrollRolePermissions;
    @FXML private VBox permissionGroups;

    // Botones
    @FXML private Button btnDeleteRole;
    @FXML private Button btnUpdateRole;
    @FXML private Button btnCreateRole;

    private Role roleToEdit;
    private final Navigator navigator;

    public RolesPermisosController(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {

        MouseMove mouseMove = new MouseMove(menuAppRolPermission);
        mouseMove.start();

        cbxRoleManage.setOnAction(event -> loadRole());
    }

    private void loadRole() {
      /*
        Role selectedRole = cbxRoleManage.getValue();

        if (selectedRole == null) return;

        roleToEdit = selectedRole;
        txtRoleName.setText(
                selectedRole.getName()
        );

        txtRoleColor.setText(
                selectedRole.getHexColor()
        );

        Set<Permission> rolePermissions =
                selectedRole.getPermissions();


        for (CheckBox chk : getPermissionCheckBoxes()) {

            Object permissionData =
                    chk.getUserData();


            if (permissionData == null) {

                chk.setSelected(false);

                continue;
            }


            try {

                Permission permission =
                        Permission.valueOf(
                                permissionData.toString()
                        );


                chk.setSelected(
                        rolePermissions.contains(permission)
                );


            } catch (IllegalArgumentException e) {

                chk.setSelected(false);

                System.err.println(
                        "Permiso inválido: "
                                + permissionData
                );
            }
        }


        cbxRoleTemplate.setValue(null);
        lblRoleFormTitle.setText(
                "Editando: "
                        + selectedRole.getName()
        );

        btnNewRole.setVisible(true);
        btnNewRole.setManaged(true);

        btnCreateRole.setVisible(false);
        btnCreateRole.setManaged(false);

        btnUpdateRole.setVisible(true);
        btnUpdateRole.setManaged(true);

        boolean isProtectedRole =
                selectedRole.hasPermission(
                        Permission.BYPASS
                );


        btnDeleteRole.setVisible(
                !isProtectedRole
        );

        btnDeleteRole.setManaged(
                !isProtectedRole
        );
*/
    }
    @FXML
    public void prepareNewRole() {
    }

    @FXML
    public void selectAllPermissions() {
    }

    @FXML
    public void clearVisiblePermissions() {
    }

    @FXML
    public void closeForm() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void deleteRole() {
    }

    @FXML
    public void updateRole() {

/*
        if (selectedRole == null) {
            showAlert("Debes seleccioanr un rol", "Editar rol", (Stage) btnUpdateRole.getScene().getWindow());
            return;
        }

        if (roleToEdit == null) {
            showAlert("No hay ningún rol seleccionado para editar.", "Editar rol", (Stage) btnUpdateRole.getScene().getWindow());
            return;
        }


        try {

            RoleDto dto = new RoleDto(

                    getSelectedPermissions(),

                    txtRoleColor
                            .getText()
                            .trim(),

                    txtRoleName
                            .getText()
                            .trim()
            );


            try {
                updateRoleUseCase.update(roleToEdit, dto);
            }catch (PermissionException e){
                System.out.println(e.getMessage());
            }

            refreshRoleManager();

            loadRoleTemplates();



            lblRoleFormTitle.setText(
                    "Editando: "
                            + roleToEdit.getName()
            );


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Rol actualizado correctamente."
            );


            if (onSaved != null) {
                onSaved.run();
            }


        } catch (RuntimeException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    e.getMessage()
            );
        }*/
    }

    private void showAlert(String message, String title, Stage stage) {
        try {

            navigator.showError(message, "Error al "+title, stage );


        }catch (IOException j){
            System.out.println(j.getMessage());
            System.out.println( j.getCause());
        }
    }

    @FXML
    public void createRole() {
    }
}
