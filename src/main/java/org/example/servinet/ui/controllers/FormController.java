package org.example.servinet.ui.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.example.servinet.core.application.dto.AntennaDto;
import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.service.RoleTemplates;
import org.example.servinet.core.application.usecase.AntennasUseCase;
import org.example.servinet.core.application.usecase.AppGeneralUseCase;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.enums.antenna.StatusAntenna;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.exception.InvalidValueException;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class FormController {
    private File selectArchive = null;
    private Path image;
    private StackPane parent;

    private Runnable onSaved;
    private User userToEdit;
    private Role roleToEdit;
    private Path userImagePath;

    @FXML private TextField txtUserNameEdit;
    @FXML private TextField txtUserEmailEdit;
    @FXML private ComboBox<Role> cbxUserRoleEdit;
    @FXML private PasswordField txtUserPasswordEdit;

    @FXML private TextField txtRoleName;
    @FXML private TextField txtRoleColor;
    @FXML private ComboBox<Role> cbxRoleManage;

    @FXML private Button btnCreateRole;
    @FXML private Button btnUpdateRole;
    @FXML private Button btnDeleteRole;

    @FXML private Label lblRoleFormTitle;

    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtUserEmail;
    @FXML
    private ComboBox<Role> cbxUserRole;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private ComboBox<String> cbxAppTheme;
    @FXML
    private TextArea txtSectorLocation;
    @FXML
    private TextField txtSectorName;
    @FXML
    private Label lblError;
    @FXML
    private CheckBox chkAutoStart;
    @FXML
    private CheckBox chkNotifications;
    @FXML
    private PasswordField txtCurrentPassword;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmPassword;

    //======================= Rename app==
    @FXML
    private TextField txtAppName;
    @FXML
    private TextField txtAppPassword;
    @FXML
    private Label lblErrorRenameApp;
    //========== Antena create
    @FXML
    private Label lblErrorCreateAntena;
    @FXML
    private TextField txtAntennaPriorityCreate;
    @FXML
    private TextField txtAntennaCName;
    @FXML
    private CheckBox chkAntennaRepair;
    @FXML
    private CheckBox chkAntennaMaintenance;
    @FXML
    private TextField txtAntennaDaysOn;
    @FXML
    private DatePicker dpAntennaLastMaintenance;
    @FXML
    private ComboBox<StatusAntenna> cbxAntennaStatus;
    @FXML
    private ComboBox<LocalTime> cbxMaintenanceTime;
    @FXML
    private Button btnSelectImage;
    @FXML
    private ImageView imgPreview;
    //======================================================================
    @FXML
    private GridPane menuUsuario;

    @FXML
    private GridPane menuUserEdit;

    @FXML
    private GridPane menuAppRename;

    @FXML
    private GridPane menuAppChangeImage;

    @FXML
    private GridPane menuAppSecurity;

    @FXML
    private GridPane menuAppSetting;

    @FXML
    private ScrollPane menuAppRolPermission;

    @FXML
    private GridPane menuAnnounceCreate;

    @FXML
    private GridPane menuAntena;

    @FXML
    private GridPane menuAntennaCreate;

    @FXML
    private GridPane menuAntennaEdit;

    @FXML
    private GridPane menuAntennaStartMant;

    @FXML
    private GridPane menuBackuptCreate;

    @FXML private ComboBox<String> cbxRoleTemplate;
    private final Map<String, Set<Permission>> templateMap = new LinkedHashMap<>();


    public void setParent(StackPane parent, FormType type) {

        this.parent = parent;


        menuUsuario.setVisible(false);
        menuUsuario.setManaged(false);

        menuUserEdit.setVisible(false);
        menuUserEdit.setManaged(false);

        menuAppRename.setVisible(false);
        menuAppRename.setManaged(false);

        menuAppChangeImage.setVisible(false);
        menuAppChangeImage.setManaged(false);

        menuAppSecurity.setVisible(false);
        menuAppSecurity.setManaged(false);

        menuAppSetting.setVisible(false);
        menuAppSetting.setManaged(false);



        menuAppRolPermission.setVisible(false);
        menuAppRolPermission.setManaged(false);

        menuAnnounceCreate.setVisible(false);
        menuAnnounceCreate.setManaged(false);

        menuAntennaCreate.setVisible(false);
        menuAntennaCreate.setManaged(false);

        menuAntena.setVisible(false);
        menuAntena.setManaged(false);

        menuAntennaEdit.setVisible(false);
        menuAntennaEdit.setManaged(false);

        menuAntennaStartMant.setVisible(false);
        menuAntennaStartMant.setManaged(false);

        menuBackuptCreate.setVisible(false);
        menuBackuptCreate.setManaged(false);


        switch (type) {

            case ANTENNA -> {

                menuAntena.setVisible(true);
                menuAntena.setManaged(true);

                menuAntena.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );

                menuAntena.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.4)
                );
            }

            case USER_CREATE -> {

                menuUsuario.setVisible(true);
                menuUsuario.setManaged(true);

                menuUsuario.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                cbxUserRole.getItems().setAll(RolesUseCase.getRoles());
            }

            case USER_EDIT -> {

                menuUserEdit.setVisible(true);
                menuUserEdit.setManaged(true);

                menuUserEdit.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUserEdit.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUserEdit.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                cbxUserRoleEdit.getItems().setAll(RolesUseCase.getRoles());
                if (userToEdit != null) {
                    txtUserNameEdit.setText(userToEdit.getName());
                    txtUserEmailEdit.setText(userToEdit.getEmail());
                    for (Role r : cbxUserRoleEdit.getItems()) {
                        if (r.getUuid().equalsIgnoreCase(userToEdit.getRol().getUuid())) {
                            cbxUserRoleEdit.setValue(r);
                            break;
                        }
                    }
                }
            }

            case APP_RENAME -> {

                menuAppRename.setVisible(true);
                menuAppRename.setManaged(true);

                menuAppRename.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.3)
                );
            }

            case APP_CHANGE_IMAGE -> {

                menuAppChangeImage.setVisible(true);
                menuAppChangeImage.setManaged(true);

                menuAppChangeImage.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.3)
                );
            }

            case APP_SECURITY -> {

                menuAppSecurity.setVisible(true);
                menuAppSecurity.setManaged(true);

                menuAppSecurity.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case APP_SETTING -> {

                menuAppSetting.setVisible(true);
                menuAppSetting.setManaged(true);

                menuAppSetting.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }
            case APP_ROL_PERMISSION -> {

                menuAppRolPermission.setVisible(true);
                menuAppRolPermission.setManaged(true);

                refreshRoleManager();

                cbxRoleManage.setOnAction(e -> {

                    if (cbxRoleManage.getValue() != null) {
                        loadSelectedRole();
                    }

                });

                prepareNewRole();

                loadRoleTemplates();
            }

            case ANNOUNCE_CREATE -> {

                menuAnnounceCreate.setVisible(true);
                menuAnnounceCreate.setManaged(true);

                menuAnnounceCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANTENNA_CREATE -> {

                menuAntennaCreate.setVisible(true);
                menuAntennaCreate.setManaged(true);

                menuAntennaCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAntennaCreate.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAntennaCreate.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAntennaCreate.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.5)
                );
                cbxAntennaStatus.setItems(FXCollections.observableArrayList(StatusAntenna.values()));
                cbxAntennaStatus.getSelectionModel().selectFirst();
                cbxMaintenanceTime.getItems().clear();
                LocalTime horaActual = LocalTime.MIDNIGHT;

                while (true) {
                    cbxMaintenanceTime.getItems().add(horaActual);

                    if (horaActual.equals(LocalTime.of(23, 30))) {
                        break;
                    }

                    horaActual = horaActual.plusMinutes(30);
                }

            }

            case ANTENNA_EDIT -> {

                menuAntennaEdit.setVisible(true);
                menuAntennaEdit.setManaged(true);

                menuAntennaEdit.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANTENNA_START_MANT -> {

                menuAntennaStartMant.setVisible(true);
                menuAntennaStartMant.setManaged(true);

                menuAntennaStartMant.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case BACKUPT_CREATE -> {

                menuBackuptCreate.setVisible(true);
                menuBackuptCreate.setManaged(true);

                menuBackuptCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }
        }
    }



    /*
    * Antenas
    *
    * */
    public void selectAntennaImage(){
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar Imagen de la Antena");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Imágenes compatibles (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        File archivoTemp = fileChooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (archivoTemp != null) {
            this.selectArchive = archivoTemp;

            Image preview = new Image(selectArchive.toURI().toString());
            imgPreview.setImage(preview);
            image = selectArchive.toPath();
        }
    }
    public void createAntenna(){
        String name = txtAntennaCName.getText();
        String daysOn = txtAntennaDaysOn.getText();
        Boolean repair = chkAntennaRepair.isSelected();
        Boolean reqMaintenance = chkAntennaMaintenance.isSelected();
        String priority = txtAntennaPriorityCreate.getText();
        LocalDate fecha = dpAntennaLastMaintenance.getValue();
        LocalTime hora = cbxMaintenanceTime.getValue();
        StatusAntenna selectedStatus = cbxAntennaStatus.getValue();

        AntennaDto ant = new AntennaDto(
                priority,
                name,
                repair,
                reqMaintenance,
                fecha,
                hora,
                image,
                selectedStatus,
                daysOn

        );
        AntennasUseCase.addAntenna(ant);
    }



    public void clearForm(){
        txtAntennaCName.clear();
        txtAntennaDaysOn.clear();
        txtAntennaPriorityCreate.clear();

        chkAntennaRepair.setSelected(false);
        chkAntennaMaintenance.setSelected(false);

        dpAntennaLastMaintenance.setValue(null);
        cbxMaintenanceTime.getSelectionModel().clearSelection();

        cbxAntennaStatus.getSelectionModel().clearSelection();

        imgPreview.setImage(null);
        this.image = null;
    }


    public void renameApp(){
        String appName = txtAppName.getText();
        String password = txtAppPassword.getText();

        try {
            if (!AppGeneralUseCase.appRename(appName, password)){
                lblErrorRenameApp.setVisible(true);
                lblErrorRenameApp.setText("Contraseña incorrecta o el nombre muy extenso");
                return;
            }
            closeForm();
        } catch (InvalidValueException e){
            lblErrorRenameApp.setVisible(true);
            lblErrorRenameApp.setText("Debes rellenar todos los campos");
        }
    }
    public void changeAppImage(){

    }
    public void selectAppImage(){

    }
    public void changeAppSecurity() {
        try {
            SessionUseCase.changeOwnPassword(
                    txtCurrentPassword.getText(),
                    txtNewPassword.getText(),
                    txtConfirmPassword.getText()
            );
            closeForm();
            showAlert(Alert.AlertType.INFORMATION, "Contraseña actualizada correctamente.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }
    public void saveAppSettings(){

    }
    public void createAnnounce(){

    }
    public void selectAnnounceImage(){

    }
    public void saveRolePermissions(){

    }

    public void saveSector(){

    }
    public void deleteSector(){

    }


    private void sectorLabelShow(String text, String color){
        lblError.setVisible(true);
        lblError.setTextFill(Color.web(color));
        lblError.setText(text);
    }


    public void selectEditAntennaImage(){

    }
    public void editAntenna(){

    }
    public void saveMaintenance(){

    }
    public void createBackup(){

    }


    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    public void setUserToEdit(User userToEdit) {
        this.userToEdit = userToEdit;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (parent != null && parent.getScene() != null) {
            alert.initOwner(parent.getScene().getWindow());
        }
        alert.showAndWait();
    }

    // ======================= USUARIOS =======================

    public void selectUserImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(parent.getScene().getWindow());
        if (file != null) {
            userImagePath = file.toPath();
            ((Button) event.getSource()).setText(file.getName());
        }
    }

    public void createUser() {
        try {
            SessionUseCase.createUser(new UserDto(
                    txtUserName.getText().trim(),
                    txtUserPassword.getText(),
                    cbxUserRole.getValue(),
                    txtUserEmail.getText().trim(),
                    userImagePath
            ));
            closeForm();
            if (onSaved != null) onSaved.run();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    public void editUser() {
        try {
            SessionUseCase.editUser(
                    userToEdit,
                    txtUserNameEdit.getText().trim(),
                    txtUserEmailEdit.getText().trim(),
                    cbxUserRoleEdit.getValue(),
                    txtUserPasswordEdit.getText(),
                    userImagePath
            );
            closeForm();
            if (onSaved != null) onSaved.run();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    // ======================= ROLES =======================

    public void updateRole() {

        if (roleToEdit == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No hay ningún rol seleccionado para editar."
            );

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


            String error =
                    RolesUseCase.updateRolFromAdmin(
                            roleToEdit,
                            dto
                    );


            if (!error.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        error
                );

                return;
            }


            refreshRoleManager();

            loadRoleTemplates();


            /*
             * Actualizamos el título por si
             * cambiaron el nombre.
             */
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
        }
    }

    private void refreshRoleManager() {

        cbxRoleManage.getItems().setAll(
                RolesUseCase.getRoles()
        );
    }

    public void loadSelectedRole() {

        Role selectedRole =
                cbxRoleManage.getValue();


        if (selectedRole == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Selecciona un rol."
            );

            return;
        }


        roleToEdit = selectedRole;


        /*
         * =========================
         * CARGAR DATOS
         * =========================
         */

        txtRoleName.setText(
                selectedRole.getName()
        );

        txtRoleColor.setText(
                selectedRole.getHexColor()
        );


        /*
         * =========================
         * CARGAR PERMISOS
         * =========================
         */

        Set<Permission> rolePermissions =
                selectedRole.getPermissions();


        for (CheckBox chk : getPermissionCheckBoxes()) {

            try {

                Permission permission =
                        Permission.valueOf(
                                chk.getText()
                        );


                chk.setSelected(
                        rolePermissions.contains(permission)
                );

            } catch (IllegalArgumentException ignored) {

            }
        }


        cbxRoleTemplate.setValue(null);


        /*
         * =========================
         * MODO EDICIÓN
         * =========================
         */

        lblRoleFormTitle.setText(
                "Editando: "
                        + selectedRole.getName()
        );


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
    }

    public void prepareNewRole() {

        roleToEdit = null;

        cbxRoleManage.setValue(null);

        clearRoleForm();

        lblRoleFormTitle.setText(
                "Crear nuevo rol"
        );


        btnCreateRole.setVisible(true);
        btnCreateRole.setManaged(true);


        btnUpdateRole.setVisible(false);
        btnUpdateRole.setManaged(false);


        btnDeleteRole.setVisible(false);
        btnDeleteRole.setManaged(false);


        /*
         * BYPASS solo debería poder asignarlo
         * un usuario que ya tenga BYPASS.
         */
        for (CheckBox chk : getPermissionCheckBoxes()) {

            if ("BYPASS".equals(chk.getText())) {

                boolean canManageBypass =
                        PermissionValidation.hasPermission(
                                Permission.BYPASS
                        );

                chk.setDisable(!canManageBypass);
            }
        }
    }

    private List<CheckBox> getPermissionCheckBoxes() {
        List<CheckBox> result = new ArrayList<>();
        collectCheckBoxes(menuAppRolPermission.getContent(), result);
        return result;
    }

    private void collectCheckBoxes(Node node, List<CheckBox> result) {
        if (node instanceof CheckBox chk) {
            result.add(chk);
        } else if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                collectCheckBoxes(child, result);
            }
        }
    }


    private Set<Permission> getSelectedPermissions() {
        Set<Permission> selected = new HashSet<>();
        for (CheckBox chk : getPermissionCheckBoxes()) {
            if (chk.isSelected()) {
                try {
                    selected.add(Permission.valueOf(chk.getText()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return selected;
    }

    public void createRole() {

        try {

            String error =
                    RolesUseCase.createRolFromAdmin(

                            new RoleDto(

                                    getSelectedPermissions(),

                                    txtRoleColor
                                            .getText()
                                            .trim(),

                                    txtRoleName
                                            .getText()
                                            .trim()
                            )
                    );


            if (!error.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        error
                );

                return;
            }


            refreshRoleManager();

            loadRoleTemplates();

            prepareNewRole();


            if (onSaved != null) {
                onSaved.run();
            }


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Rol creado correctamente."
            );


        } catch (RuntimeException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    e.getMessage()
            );
        }
    }


    private void loadRoleTemplates() {
        templateMap.clear();
        templateMap.putAll(RoleTemplates.all());
        for (Role r : RolesUseCase.getRoles()) {
            templateMap.put("Copiar rol: " + r.getName(), r.getPermissions());
        }
        cbxRoleTemplate.setOnAction(null);
        cbxRoleTemplate.getItems().setAll(templateMap.keySet());
        cbxRoleTemplate.setOnAction(e -> applyTemplate(cbxRoleTemplate.getValue()));
    }


    private void applyTemplate(String name) {
        Set<Permission> perms = templateMap.get(name);
        if (perms == null) return;

        for (CheckBox chk : getPermissionCheckBoxes()) {
            try {
                chk.setSelected(perms.contains(Permission.valueOf(chk.getText())));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void deleteRole() {

        if (roleToEdit == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Selecciona primero un rol."
            );

            return;
        }


        /*
         * Seguridad adicional.
         */
        if (roleToEdit.hasPermission(
                Permission.BYPASS
        )) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "El rol del Dueño no se puede eliminar."
            );

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmation.setHeaderText(
                "Eliminar rol"
        );


        confirmation.setContentText(
                "¿Seguro que deseas eliminar el rol \""
                        + roleToEdit.getName()
                        + "\"?"
        );


        Optional<ButtonType> result =
                confirmation.showAndWait();


        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }


        try {

            String error =
                    RolesUseCase.deleteRol(
                            roleToEdit
                    );


            if (!error.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        error
                );

                return;
            }


            refreshRoleManager();

            loadRoleTemplates();

            prepareNewRole();


            if (onSaved != null) {
                onSaved.run();
            }


            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Rol eliminado correctamente."
            );


        } catch (RuntimeException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    e.getMessage()
            );
        }
    }

    public void clearRoleForm() {

        txtRoleName.clear();

        txtRoleColor.clear();

        cbxRoleTemplate.setValue(null);


        for (CheckBox chk : getPermissionCheckBoxes()) {

            chk.setSelected(false);
        }
    }

    public void closeForm(){
        parent.getChildren().clear();
        parent.setVisible(false);
    }

}
