package org.example.servinet.ui.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.example.servinet.application.dto.*;
import org.example.servinet.application.security.PermissionValidation;
import org.example.servinet.application.service.RoleTemplates;
import org.example.servinet.application.usecase.*;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.*;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.RoleNoPermission;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class FormController {
    private File selectArchive = null;
    private Path image;
    private StackPane parent;

    private Runnable onSaved;
    private Staff userToEdit;
    private Role roleToEdit;
    private Client clientToEdit;
    private Path userImagePath;



    @FXML private TextField txtUserNameEdit;
    @FXML private TextField txtUserEmailEdit;
    @FXML private ComboBox<Role> cbxUserRoleEdit;
    @FXML private PasswordField txtUserPasswordEdit;

    @FXML private TextField txtRoleName;
    @FXML private TextField txtRoleColor;
    @FXML private ComboBox<Role> cbxRoleManage;

    @FXML private Button btnNewRole;
    @FXML private Button btnCreateRole;
    @FXML private Button btnUpdateRole;
    @FXML private Button btnDeleteRole;



    @FXML private Label lblRoleFormTitle;

    @FXML private TextField txtUserName;
    @FXML private TextField txtUserEmail;
    @FXML private ComboBox<Role> cbxUserRole;
    @FXML private PasswordField txtUserPassword;
    @FXML private ComboBox<String> cbxAppTheme;
    @FXML private TextArea txtSectorLocation;
    @FXML private TextField txtSectorName;
    @FXML private Label lblError;
    @FXML private CheckBox chkAutoStart;
    @FXML private CheckBox chkNotifications;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    @FXML private TextField txtAppName;
    @FXML private TextField txtAppPassword;
    @FXML private Label lblErrorRenameApp;

    @FXML private Label lblErrorCreateAntena;
    @FXML private TextField txtAntennaPriorityCreate;
    @FXML private TextField txtAntennaCName;
    @FXML private CheckBox chkAntennaRepair;
    @FXML private CheckBox chkAntennaMaintenance;
    @FXML private TextField txtAntennaDaysOn;
    @FXML private DatePicker dpAntennaLastMaintenance;
    @FXML private ComboBox<StatusAntenna> cbxAntennaStatus;
    @FXML private ComboBox<LocalTime> cbxMaintenanceTime;
    @FXML private Button btnSelectImage;
    @FXML private ImageView imgPreview;

    @FXML private Label lblErrorCreateClient;
    @FXML private TextField txtClientFirstNameCreate;
    @FXML private TextField txtClientLastNameCreate;
    @FXML private TextField txtClientDniCreate;
    @FXML private TextField txtClientPhoneCreate;
    @FXML private TextField txtClientEmailCreate;
    @FXML private TextField txtClientAddressCreate;

    @FXML private DatePicker dpClientInstallationCreate;
    @FXML private Label lblErrorEditClient;
    @FXML private TextField txtClientFirstNameEdit;
    @FXML private TextField txtClientLastNameEdit;
    @FXML private TextField txtClientDniEdit;
    @FXML private TextField txtClientPhoneEdit;
    @FXML private TextField txtClientEmailEdit;
    @FXML private TextField txtClientAddressEdit;
    @FXML private ComboBox<ClientPlan> cbxClientPlanEdit;
    @FXML private TextField txtClientNumWhatsApp;


    @FXML private Label lblAnnounceFormTitle;
    @FXML private TextField txtAnnounceTitle;
    @FXML private ComboBox<String> cbxAnnouncePriority;
    @FXML private TextArea txtAnnounceContent;
    @FXML private Button btnSelectAnnounceImage;
    @FXML private Label lblAnnounceTitleCount;
    @FXML private Label lblAnnounceContentCount;
    @FXML private Label lblAnnounceError;
    @FXML private Button btnAnnounceSubmit;

    private Announcement announcementToEdit;
    private Path announceImagePath;

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

    @FXML private VBox menuAppRolPermission;
    @FXML private VBox permissionGroups;
    @FXML private TextField txtPermissionSearch;
    @FXML private Label lblPermissionsCount;

    private final Map<Permission, CheckBox> permissionChecks =
            new LinkedHashMap<>();

    private final Map<String, TitledPane> permissionSections =
            new LinkedHashMap<>();

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

    @FXML
    private GridPane menuClientEdit;

    @FXML private ComboBox<String> cbxRoleTemplate;
    private final Map<String, Set<Permission>> templateMap = new LinkedHashMap<>();


    @FXML public void initialize() {
        txtClientPhoneEdit.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        txtClientNumWhatsApp.setText(newValue)
        );
    }
    public void setClientToEdit(Client clientToEdit) {
        this.clientToEdit = clientToEdit;
    }

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
        menuClientEdit.setVisible(false);
        menuClientEdit.setManaged(false);


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
                    txtUserNameEdit.setText(userToEdit.getDisplay());
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

                menuAppRolPermission.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.70)
                );

                menuAppRolPermission.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.74)
                );

                // Construimos los permisos del enum
                buildPermissionEditor();

                // Cargamos los roles existentes
                refreshRoleManager();

                cbxRoleManage.setOnAction(e -> {

                    if (cbxRoleManage.getValue() != null) {
                        loadSelectedRole();
                    }

                });

                // Preparar creación de un rol
                prepareNewRole();

                // Cargar plantillas
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

            case CLIENT_EDIT -> {

                menuClientEdit.setVisible(true);
                menuClientEdit.setManaged(true);

                menuClientEdit.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                if (clientToEdit != null) {




                    cbxClientPlanEdit.setItems(PlanUseCase.getAllPlans());
                    txtClientFirstNameEdit.setText(clientToEdit.getFirstName());
                    txtClientLastNameEdit.setText(clientToEdit.getLastName());
                    txtClientDniEdit.setText(clientToEdit.getDni());
                    String phone = clientToEdit.getPhone();
                    txtClientPhoneEdit.setText(phone);
                    txtClientEmailEdit.setText(clientToEdit.getEmail());
                    txtClientAddressEdit.setText(clientToEdit.getAddress());
                    cbxClientPlanEdit.setPromptText(clientToEdit.getPlan().getName());
                    txtClientNumWhatsApp.setText(phone);


                }
            }
        }
    }

    private void setupAnnounceForm() {

        txtAnnounceTitle.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= AnnouncementsUseCase.TITLE_MAX ? change : null));
        txtAnnounceContent.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= AnnouncementsUseCase.DESCRIPTION_MAX ? change : null));

        txtAnnounceTitle.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceTitleCount, newValue, AnnouncementsUseCase.TITLE_MAX));
        txtAnnounceContent.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceContentCount, newValue, AnnouncementsUseCase.DESCRIPTION_MAX));

        updateCounter(lblAnnounceTitleCount, "", AnnouncementsUseCase.TITLE_MAX);
        updateCounter(lblAnnounceContentCount, "", AnnouncementsUseCase.DESCRIPTION_MAX);

        cbxAnnouncePriority.setItems(FXCollections.observableArrayList(AnnouncementsUseCase.PRIORITIES));

        if (announcementToEdit != null) {
            lblAnnounceFormTitle.setText("Editar anuncio");
            btnAnnounceSubmit.setText("Guardar cambios");
            txtAnnounceTitle.setText(announcementToEdit.getTitle());
            txtAnnounceContent.setText(announcementToEdit.getDescription());
            cbxAnnouncePriority.setValue(announcementToEdit.getPriority());
            btnSelectAnnounceImage.setText("Cambiar imagen (opcional)");
        } else {
            cbxAnnouncePriority.setValue("Media");
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

    private void updateCounter(Label label, String text, int max) {
        int length = text == null ? 0 : text.length();
        label.setText(length + "/" + max);
        label.setStyle(length >= max ? "-fx-text-fill: #E5484D; -fx-font-weight: bold;" : "");
    }

    public void selectAnnounceImage(){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del anuncio");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(parent.getScene().getWindow());
        if (file != null) {
            announceImagePath = file.toPath();
            btnSelectAnnounceImage.setText(file.getName());
        }
    }

    public void saveAnnounce(){

        AnnouncementDto dto = new AnnouncementDto(
                txtAnnounceTitle.getText(),
                txtAnnounceContent.getText(),
                cbxAnnouncePriority.getValue(),
                announceImagePath
        );

        try {
            if (announcementToEdit != null) {
                AnnouncementsUseCase.editAnnouncement(announcementToEdit, dto);
            } else {
                AnnouncementsUseCase.createAnnouncement(dto);
            }

            closeForm();
            if (onSaved != null) onSaved.run();

        } catch (RuntimeException e) {
            lblAnnounceError.setText(e.getMessage());
            lblAnnounceError.setVisible(true);
            lblAnnounceError.setManaged(true);
        }
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


    public void clearClientForm() {
        txtClientFirstNameCreate.clear();
        txtClientLastNameCreate.clear();
        txtClientDniCreate.clear();
        txtClientPhoneCreate.clear();
        txtClientEmailCreate.clear();
        txtClientAddressCreate.clear();
        dpClientInstallationCreate.setValue(null);

        lblErrorCreateClient.setVisible(false);
        lblErrorCreateClient.setManaged(false);
    }

    public void saveEditClient() {


        if (clientToEdit == null) {
            showAlert(Alert.AlertType.WARNING, "No hay ningún cliente seleccionado para editar.");
            return;
        }

        ClientPlan clientPlan = cbxClientPlanEdit.getValue();
        if (clientPlan == null) {
            clientPlan = clientToEdit.getPlan();
        }

        ClientCreateDto dto = new ClientCreateDto(

                txtClientDniEdit.getText(),
                txtClientFirstNameEdit.getText(),
                txtClientLastNameEdit.getText(),
                txtClientPhoneEdit.getText(),
                txtClientAddressEdit.getText(),
                txtClientEmailEdit.getText(),
                txtClientNumWhatsApp.getText(),
                clientPlan
        );

        try {
            ClientsUseCase.editClient(dto);
            closeForm();
            if (onSaved != null) onSaved.run();
        } catch (InvalidValueException | RoleNoPermission e) {
            lblErrorEditClient.setVisible(true);
            lblErrorEditClient.setManaged(true);
            lblErrorEditClient.setText(e.getMessage());
        }


    }

    public void setAnnouncementToEdit(Announcement announcementToEdit) {
        this.announcementToEdit = announcementToEdit;
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
            LoginStaffUseCase.changeOwnPassword(
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

    public void setUserToEdit(Staff userToEdit) {
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
            LoginStaffUseCase.createUser(new UserDto(
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
            LoginStaffUseCase.editUser(
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
    }

    public void prepareNewRole() {

        /*
         * ahi dejare de editar cualquier rol.
         */
        roleToEdit = null;


        /*
         * limpia la selección del ComboBox.
         */
        cbxRoleManage.setValue(null);


        /*
         * se limpia nombre, color,
         * plantilla y permisos.
         */
        clearRoleForm();


        /*
         * se cambia título.
         */
        lblRoleFormTitle.setText(
                "Crear nuevo rol"
        );


        /*

         * MODO CREAR
         */

        btnNewRole.setVisible(false);
        btnNewRole.setManaged(false);


        btnCreateRole.setVisible(true);
        btnCreateRole.setManaged(true);


        btnUpdateRole.setVisible(false);
        btnUpdateRole.setManaged(false);


        btnDeleteRole.setVisible(false);
        btnDeleteRole.setManaged(false);


        /*
         * BYPASS solo puede gestionarlo
         * quien tenga BYPASS.
         */
        for (CheckBox chk : getPermissionCheckBoxes()) {

            if ("BYPASS".equals(String.valueOf(chk.getUserData()))) {

                boolean canManageBypass =
                        PermissionValidation.hasPermission(
                                Permission.BYPASS
                        );

                chk.setDisable(
                        !canManageBypass
                );
            }
        }
    }


    private String getPermissionCategory(Permission permission) {

        String name = permission.name();

        if (permission == Permission.BYPASS)
            return "01 - Propietario";

        if (name.startsWith("BTN_SIDEBAR_"))
            return "02 - Menú lateral";

        if (name.startsWith("DASH_"))
            return "03 - Dashboard";

        if (name.startsWith("AD_"))
            return "04 - Administración";

        if (name.startsWith("APP_"))
            return "05 - Configuración";

        if (name.startsWith("ANNOUNCE_"))
            return "06 - Anuncios";

        if (name.startsWith("ANT_"))
            return "07 - Antenas";

        if (name.startsWith("BACKUPS_"))
            return "08 - Backups";

        if (name.startsWith("CLIENT"))
            return "09 - Clientes";

        if (name.startsWith("SELL_"))
            return "10 - Ventas";

        if (name.startsWith("TECHNICIAN_"))
            return "11 - Técnicos";

        return "12 - Otros";
    }


    // CREAR AUTOMÁTICAMENTE TODOS LOS PERMISOS
    private void buildPermissionEditor() {

        permissionGroups.getChildren().clear();
        permissionChecks.clear();
        permissionSections.clear();

        Map<String, FlowPane> groups = new LinkedHashMap<>();

        for (Permission permission : Permission.values()) {

            String category = getPermissionCategory(permission);

            FlowPane group = groups.computeIfAbsent(
                    category,
                    key -> {
                        FlowPane pane = new FlowPane(18, 12);
                        pane.setPadding(new Insets(14));
                        pane.setPrefWrapLength(690);
                        return pane;
                    }
            );

            CheckBox checkBox = new CheckBox(
                    permission.name().replace("_", " ")
            );

            checkBox.setUserData(permission.name());

            checkBox.setTooltip(
                    new Tooltip(permission.name())
            );

            checkBox.getStyleClass().add(
                    "role-permission-check"
            );

            // BYPASS solo puede otorgarlo el dueño
            if (permission == Permission.BYPASS) {
                checkBox.setDisable(
                        !PermissionValidation.hasPermission(
                                Permission.BYPASS
                        )
                );
            }

            checkBox.selectedProperty().addListener(
                    (obs, oldValue, newValue) ->
                            updatePermissionCount()
            );

            permissionChecks.put(permission, checkBox);

            group.getChildren().add(checkBox);
        }

        // Convertir cada categoría en sección desplegable
        groups.forEach((category, group) -> {

            TitledPane section =
                    new TitledPane(category, group);

            section.setExpanded(false);

            section.getStyleClass().add(
                    "role-permission-group"
            );

            permissionGroups.getChildren().add(section);
            permissionSections.put(category, section);
        });

        // Conectar buscador
        txtPermissionSearch.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        filterPermissions(newValue)
        );

        updatePermissionCount();
    }


    // BUSCAR PERMISOS
    private void filterPermissions(String text) {

        String search = text == null
                ? ""
                : text.trim().toLowerCase(Locale.ROOT);

        permissionSections.forEach((category, section) -> {

            boolean hasResults = false;

            for (Map.Entry<Permission, CheckBox> entry :
                    permissionChecks.entrySet()) {

                if (!getPermissionCategory(entry.getKey())
                        .equals(category)) {
                    continue;
                }

                CheckBox checkBox = entry.getValue();

                boolean matches =
                        search.isEmpty()
                                || entry.getKey().name()
                                .toLowerCase(Locale.ROOT)
                                .contains(search)
                                || category.toLowerCase(Locale.ROOT)
                                .contains(search);

                checkBox.setVisible(matches);
                checkBox.setManaged(matches);

                if (matches) {
                    hasResults = true;
                }
            }

            section.setVisible(hasResults);
            section.setManaged(hasResults);

            if (!search.isEmpty() && hasResults) {
                section.setExpanded(true);
            }
        });
    }


    // MARCAR PERMISOS FILTRADOS
    @FXML
    private void selectAllPermissions() {
        setVisiblePermissions(true);
    }


    // LIMPIAR PERMISOS FILTRADOS
    @FXML
    private void clearVisiblePermissions() {
        setVisiblePermissions(false);
    }


    private void setVisiblePermissions(boolean selected) {

        for (CheckBox checkBox : permissionChecks.values()) {

            if (checkBox.isVisible()
                    && !checkBox.isDisabled()) {

                checkBox.setSelected(selected);
            }
        }

        updatePermissionCount();
    }


    // ACTUALIZAR CONTADOR
    private void updatePermissionCount() {

        long selected = permissionChecks.values()
                .stream()
                .filter(CheckBox::isSelected)
                .count();

        lblPermissionsCount.setText(
                selected + " de "
                        + permissionChecks.size()
                        + " permisos seleccionados"
        );
    }


    // DEVOLVER CHECKBOX DE LOS PERMISOS
    private List<CheckBox> getPermissionCheckBoxes() {

        return new ArrayList<>(permissionChecks.values());
    }



    private Set<Permission> getSelectedPermissions() {

        Set<Permission> selected = new HashSet<>();


        for (CheckBox chk : getPermissionCheckBoxes()) {

            if (!chk.isSelected()) {
                continue;
            }


            Object permissionData = chk.getUserData();


            if (permissionData == null) {

                System.err.println(
                        "El CheckBox "
                                + chk.getId()
                                + " no tiene userData."
                );

                continue;
            }


            try {

                Permission permission =
                        Permission.valueOf(
                                permissionData.toString()
                        );

                selected.add(permission);

            } catch (IllegalArgumentException e) {

                System.err.println(
                        "Permiso desconocido: "
                                + permissionData
                );
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

        Set<Permission> permissions = templateMap.get(name);

        if (permissions == null) {
            return;
        }

        for (CheckBox checkBox : getPermissionCheckBoxes()) {

            if (checkBox.isDisabled()) {
                continue;
            }

            Permission permission = Permission.valueOf(
                    checkBox.getUserData().toString()
            );

            checkBox.setSelected(
                    permissions.contains(permission)
            );
        }

        updatePermissionCount();
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