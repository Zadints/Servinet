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
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.port.in.RoleTemplates;
import org.example.servinet.application.usecase.*;
import org.example.servinet.application.usecase.announce.CreateAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EditAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.application.usecase.antenna.CreateAntennaUseCase;
import org.example.servinet.application.usecase.appsetting.AppGeneralUseCase;
import org.example.servinet.application.usecase.clients.EditClientUseCase;
import org.example.servinet.application.usecase.role.CreateRoleUseCase;
import org.example.servinet.application.usecase.role.DeleteRoleUseCase;
import org.example.servinet.application.usecase.role.GetRoleUseCase;
import org.example.servinet.application.usecase.role.UpdateRoleUseCase;
import org.example.servinet.application.usecase.staff.EditStaffUseCase;
import org.example.servinet.application.usecase.staff.RegisterStaffUseCase;
import org.example.servinet.domain.entities.*;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.antenna.StatusAntenna;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.servinet.ui.events.ImageConverter;

public class FormController {
 /*   private File selectArchive = null;
    private byte[] image;
    private StackPane parent;

    private Runnable onSaved;
    private Staff userToEdit;
    private Role roleToEdit;
    private Client clientToEdit;

    @FXML private TextField txtRoleName;
    @FXML private TextField txtRoleColor;
    @FXML private ComboBox<Role> cbxRoleManage;

    @FXML private Button btnNewRole;
    @FXML private Button btnCreateRole;
    @FXML private Button btnUpdateRole;
    @FXML private Button btnDeleteRole;



    @FXML private Label lblRoleFormTitle;


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

    @FXML private GridPane menuAppChangeImage;
    @FXML private GridPane menuAppSecurity;
    @FXML private GridPane menuAppSetting;

    @FXML private VBox menuAppRolPermission;
    @FXML private VBox permissionGroups;
    @FXML private TextField txtPermissionSearch;
    @FXML private Label lblPermissionsCount;
    private final Map<Permission, CheckBox> permissionChecks = new LinkedHashMap<>();
    private final Map<String, TitledPane> permissionSections = new LinkedHashMap<>();
    @FXML private GridPane menuAnnounceCreate;
    @FXML private GridPane menuAntena;
    @FXML private GridPane menuAntennaCreate;
    @FXML private GridPane menuAntennaEdit;
    @FXML private GridPane menuAntennaStartMant;
    @FXML private GridPane menuBackuptCreate;
    @FXML private GridPane menuClientEdit;
    @FXML private ComboBox<String> cbxRoleTemplate;
    private final Map<String, Set<Permission>> templateMap = new LinkedHashMap<>();


    private final CreateRoleUseCase createRoleUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final PlanUseCase planUseCase;
    private final GetAnnounceUseCase getAnnounceUseCase;
    private final CreateAntennaUseCase createAntennaUseCase;
    private final EditAnnounceUseCase editAnnounceUseCase;
    private final CreateAnnounceUseCase createAnnounceUseCase;
    private final AppGeneralUseCase appGeneralUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final EditClientUseCase editClientUseCase;

    private final UpdateRoleUseCase updateRoleUseCase;
    private final AuthorizationSecurity authorizationSecurity;

    public FormController(CreateRoleUseCase createRoleUseCase, GetRoleUseCase getRoleUseCase, PlanUseCase planUseCase, GetAnnounceUseCase getAnnounceUseCase, CreateAntennaUseCase createAntennaUseCase, EditAnnounceUseCase editAnnounceUseCase, CreateAnnounceUseCase createAnnounceUseCase, AppGeneralUseCase appGeneralUseCase, DeleteRoleUseCase deleteRoleUseCase, EditClientUseCase editClientUseCase, RegisterStaffUseCase registerStaffUseCase, EditStaffUseCase editStaffUseCase, UpdateRoleUseCase updateRoleUseCase, AuthorizationSecurity authorizationSecurity) {
        this.createRoleUseCase = createRoleUseCase;
        this.getRoleUseCase = getRoleUseCase;
        this.planUseCase = planUseCase;
        this.getAnnounceUseCase = getAnnounceUseCase;
        this.createAntennaUseCase = createAntennaUseCase;
        this.editAnnounceUseCase = editAnnounceUseCase;
        this.createAnnounceUseCase = createAnnounceUseCase;
        this.appGeneralUseCase = appGeneralUseCase;
        this.deleteRoleUseCase = deleteRoleUseCase;
        this.editClientUseCase = editClientUseCase;

        this.updateRoleUseCase = updateRoleUseCase;
        this.authorizationSecurity = authorizationSecurity;
    }


    @FXML public void initialize() {
        txtClientPhoneEdit.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        txtClientNumWhatsApp.setText(newValue)
        );
    }

    public void setClientToEdit(Client clientToEdit) {
        this.clientToEdit = clientToEdit;
    }



    private void setupAnnounceForm() {

        txtAnnounceTitle.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= getAnnounceUseCase.getTitleMax() ? change : null));
        txtAnnounceContent.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= getAnnounceUseCase.getDescriptionMax() ? change : null));

        txtAnnounceTitle.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceTitleCount, newValue, getAnnounceUseCase.getTitleMax()));
        txtAnnounceContent.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceContentCount, newValue, getAnnounceUseCase.getDescriptionMax()));

        updateCounter(lblAnnounceTitleCount, "", getAnnounceUseCase.getTitleMax());
        updateCounter(lblAnnounceContentCount, "", getAnnounceUseCase.getDescriptionMax());

        cbxAnnouncePriority.setItems(FXCollections.observableArrayList(getAnnounceUseCase.getPrioritiesMax()));

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
            try {
                image = ImageConverter.toBytes(selectArchive.toPath());
            }catch (IOException e){
                System.out.println(e);
            }
        }
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
                editAnnounceUseCase.editAnnouncement(announcementToEdit, dto);
            } else {
                createAnnounceUseCase.createAnnouncement(dto);
            }

            closeForm();
            if (onSaved != null) onSaved.run();

        } catch (RuntimeException e) {
            lblAnnounceError.setText(e.getMessage());
            lblAnnounceError.setVisible(true);
            lblAnnounceError.setManaged(true);
        }
    }




    public void setAnnouncementToEdit(Announcement announcementToEdit) {
        this.announcementToEdit = announcementToEdit;
    }

    public void renameApp(){
        String appName = txtAppName.getText();
        String password = txtAppPassword.getText();

        try {
            appGeneralUseCase.appRename(appName);
            closeForm();
        } catch (PermissionException | IllegalArgumentException |InvalidValueException e){
            lblErrorRenameApp.setVisible(true);
            lblErrorRenameApp.setText("Debes rellenar todos los campos");
        }
    }




    private void refreshRoleManager() {

        cbxRoleManage.getItems().setAll(
                getRoleUseCase.getRoles()
        );
    }

    public void loadSelectedRole() {

        Role selectedRole =
                cbxRoleManage.getValue();


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


        roleToEdit = null;



        cbxRoleManage.setValue(null);



        clearRoleForm();



        lblRoleFormTitle.setText(
                "Crear nuevo rol"
        );




        btnNewRole.setVisible(false);
        btnNewRole.setManaged(false);


        btnCreateRole.setVisible(true);
        btnCreateRole.setManaged(true);


        btnUpdateRole.setVisible(false);
        btnUpdateRole.setManaged(false);


        btnDeleteRole.setVisible(false);
        btnDeleteRole.setManaged(false);


        for (CheckBox chk : getPermissionCheckBoxes()) {

            if ("BYPASS".equals(String.valueOf(chk.getUserData()))) {

                boolean canManageBypass =
                        authorizationSecurity.hasPermission(
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
                        !authorizationSecurity.hasPermission(
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
                    createRoleUseCase.createRol(

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
        for (Role r : getRoleUseCase.getRoles()) {
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
                    deleteRoleUseCase.delete(
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
*/
}