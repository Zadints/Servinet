package org.example.servinet.ui.controllers.center;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.servinet.application.security.PermissionValidation;
import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.RolesUseCase;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.FormType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.UserRowController;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdministrationController {
    private IndexController indexController;

    @FXML private GridPane activityGrid;
    @FXML private VBox usersList;
    @FXML private ComboBox<Staff> cmbActivityUser;
    @FXML private Label lblRolesCount;
    @FXML private Label lblUsersWithRoleCount;
    /* Filtros de usuarios */
    @FXML private TextField txtUserSearch;
    @FXML private ComboBox<String> cmbRoleFilter;
    @FXML private Label lblUsersSummary;

    @FXML private VBox sectionPersonal;
    @FXML private Button btnCreateUser;
    @FXML private VBox optName;
    @FXML private VBox optLogo;
    @FXML private VBox optSecurity;
    @FXML private VBox optConfig;
    @FXML private VBox optRoles;
    @FXML private HBox hboxSearchUser;
    @FXML private HBox hbxActivityG1;
    @FXML private HBox hbxActivityG2;
    @FXML private HBox hbxActivityG3;
    @FXML private Button btnActivity;
    /*------------------------------------------
    * Atributos de clase dedicados a manejar permisos de cuenta del staff:
    * - vboxUserRegistered => bloque fxml que contiene opciones para buscar y mostrar usuarios de la app
    * - vboxUserActivity=> bloque que muestra actividad de conexiones en el día.
    * - vboxOwnerOptions => bloque Ui de opciones del dueño de la app
    --------------------------------------------*/
    @FXML private ScrollPane vboxUserRegistered;
    @FXML private VBox vboxUserActivity;
    @FXML private VBox vboxOwnerOptions;

    private static final String ALL_ROLES = "Todos los roles";
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        /*
        * Renderizar cpomponentes de la Ui dependiendo el permiso,
        * simplificando asi el renderizado y evitando lògica compleja
        */
        renderizedByPermissions();
    }



    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
    @FXML
    protected void onActivityUserSelected() {

        cargarActividad(cmbActivityUser.getValue());
    }


    private void renderizedByPermissions() {

        // Obtener los permisos una sola vez.
        PermissionPolicy policy = PermissionPolicy.current();

        // ============================
        // SECCIÓN: PERSONAL
        // ============================

        boolean crearPersonal = policy.allows(
                Permission.AD_CREATE_PERSONAL
        );

        boolean buscarPersonal = policy.allows(
                Permission.AD_SEARCH_PERSONAL
        );

        boolean mostrarPersonal = policy.allowsAny(
                Permission.AD_VIEW_PERSONAL,
                Permission.AD_SEARCH_PERSONAL,
                Permission.AD_EDIT_PERSONAL,
                Permission.AD_DELETE_PERSONAL
        );

        // La sección aparece si existe alguna función utilizable.
        show(sectionPersonal, crearPersonal || mostrarPersonal);

        show(btnCreateUser, crearPersonal);

        show(hboxSearchUser, buscarPersonal && mostrarPersonal);

        show(vboxUserRegistered, mostrarPersonal);

        if (mostrarPersonal) {

            refreshRoleFilter();

            if (buscarPersonal) {
                setupUserFilters();
            }

            renderUsers();
        }

        // ============================
        // SECCIÓN: ACTIVIDAD
        // ============================

        boolean verActividad = policy.allows(
                Permission.AD_ACTIVITY_PERSONAL
        );

        boolean verLogs = policy.allows(
                Permission.AD_VIEW_LOG_PERSONAL
        );

        show(vboxUserActivity, verActividad || verLogs);

        show(hbxActivityG1, verActividad);
        show(hbxActivityG2, verActividad);
        show(hbxActivityG3, verActividad);

        show(btnActivity, verLogs);

        if (verActividad || verLogs) {

            cmbActivityUser.getItems().setAll(
                    LoginStaffUseCase.getAllStaff()
            );

            fillActivityCombo();
        }

        // ============================
        // SECCIÓN: CONFIGURACIÓN
        // ============================

        boolean cambiarNombre = policy.allows(
                Permission.APP_CHANGE_NAME
        );

        boolean cambiarLogo = policy.allows(
                Permission.APP_CHANGE_LOGO
        );

        boolean configurarSeguridad = policy.allows(
                Permission.APP_SECURITY_CONFIG
        );

        boolean configuracionGeneral = policy.allows(
                Permission.APP_GENERAL_CONFIG
        );

        boolean gestionarRoles = policy.allows(
                Permission.APP_CONF_ROL_PERMS
        );

        // Mostrar el contenedor si hay al menos una opción.
        boolean mostrarConfiguracion =
                cambiarNombre
                        || cambiarLogo
                        || configurarSeguridad
                        || configuracionGeneral
                        || gestionarRoles;

        show(vboxOwnerOptions, mostrarConfiguracion);

        // Cada tarjeta depende de su permiso.
        show(optName, cambiarNombre);
        show(optLogo, cambiarLogo);
        show(optSecurity, configurarSeguridad);
        show(optConfig, configuracionGeneral);
        show(optRoles, gestionarRoles);

        if (gestionarRoles) {
            updateRolesSummary();
        }
    }


    private void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }


    private void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean exigirPermiso(Permission permiso) {

        if (PermissionValidation.hasPermission(permiso)) {
            return true;
        }

        warn("No tienes permiso para realizar esta acción.");
        return false;
    }

    // ===================== USUARIOS =====================




    private void setupUserFilters() {

        cmbRoleFilter.setOnAction(e -> renderUsers());

        txtUserSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> renderUsers()
        );
    }

    private void refreshAll() {

        PermissionPolicy policy = PermissionPolicy.current();

        boolean puedeVerPersonal = policy.allowsAny(
                Permission.AD_VIEW_PERSONAL,
                Permission.AD_SEARCH_PERSONAL,
                Permission.AD_EDIT_PERSONAL,
                Permission.AD_DELETE_PERSONAL
        );

        if (puedeVerPersonal) {

            refreshRoleFilter();
            renderUsers();
        }

        if (policy.allowsAny(
                Permission.AD_ACTIVITY_PERSONAL,
                Permission.AD_VIEW_LOG_PERSONAL)) {

            cmbActivityUser.getItems().setAll(
                    LoginStaffUseCase.getAllStaff()
            );

            fillActivityCombo();
        }

        if (policy.allows(Permission.APP_CONF_ROL_PERMS)) {
            updateRolesSummary();
        }
    }

    private void renderUsers() {
        String search = txtUserSearch.getText();

        if (search == null) return;

        search = search.trim().toLowerCase(Locale.ROOT);


        String selectedRole = cmbRoleFilter.getValue();

        if (selectedRole == null) {
            selectedRole = ALL_ROLES;
        }


        List<Staff> filteredUsers = new ArrayList<>();
        usersList.getChildren().clear();

        for (Staff user : LoginStaffUseCase.getAllStaff()) {

            boolean roleMatches = ALL_ROLES.equals(selectedRole) ||
                    user.getRolName().equalsIgnoreCase(selectedRole);

            String name = user.getDisplay() == null
                    ? ""
                    : user.getDisplay().toLowerCase(Locale.ROOT);

            String email = user.getEmail() == null
                    ? ""
                    : user.getEmail().toLowerCase(Locale.ROOT);


            boolean searchMatches =
                    search.isBlank()
                            || name.contains(search)
                            || email.contains(search);


            if (roleMatches && searchMatches) {
                filteredUsers.add(user);
            }
        }


        /*
         * Primero ordenamos por nombre del rol
         * y luego por nombre del usuario.
         */
        filteredUsers.sort(

                Comparator
                        .comparing(
                                (Staff u) -> u.getRolName().toLowerCase(Locale.ROOT)
                        )
                        .thenComparing(
                                u -> u.getDisplay().toLowerCase(Locale.ROOT)
                        )

        );


        updateUserSummary(filteredUsers.size());


        if (filteredUsers.isEmpty()) {

            Label emptyLabel = new Label(
                    "No se encontraron usuarios con los filtros seleccionados."
            );

            emptyLabel.getStyleClass().add("users-empty-label");

            usersList.getChildren().add(emptyLabel);

            return;
        }


        /*
         * Agrupamos usando el UUID del rol.
         *
         * No usamos solamente el nombre porque
         * el UUID es la verdadera identificación del rol.
         */
        Map<String, List<Staff>> usersByRole = new LinkedHashMap<>();

        Map<String, Role> rolesByUuid = new LinkedHashMap<>();


        for (Staff user : filteredUsers) {

            Role role = user.getRol();

            rolesByUuid.putIfAbsent(
                    role.getUuid(),
                    role
            );

            usersByRole
                    .computeIfAbsent(
                            role.getUuid(),
                            key -> new ArrayList<>()
                    )
                    .add(user);
        }


        /*
         * Creamos visualmente cada grupo.
         */
        for (Map.Entry<String, List<Staff>> entry : usersByRole.entrySet()) {

            Role role = rolesByUuid.get(entry.getKey());

            List<Staff> roleUsers = entry.getValue();

            VBox roleSection = createRoleSection(
                    role,
                    roleUsers
            );

            usersList.getChildren().add(roleSection);
        }
    }

    private VBox createRoleSection(
            Role role,
            List<Staff> users
    )
    {

        VBox section = new VBox(8);

        section.getStyleClass().add("user-role-group");


        /*
         * Color lateral correspondiente al rol.
         */
        String roleColor = role.getHexColor();

        if (roleColor == null
                || !roleColor.matches("^#[A-Fa-f0-9]{6}$")) {

            roleColor = "#22A5F1";
        }


        section.setStyle(
                "-fx-border-color: transparent transparent transparent "
                        + roleColor + ";"
                        + "-fx-border-width: 0 0 0 4;"
        );


        /* ================= CABECERA DEL ROL ================= */

        HBox header = new HBox(10);

        header.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        header.getStyleClass().add("user-role-header");


        /*
         * Pequeño círculo/cuadrado con el color del rol.
         */
        Region roleColorIndicator = new Region();

        roleColorIndicator.setMinSize(10, 10);
        roleColorIndicator.setPrefSize(10, 10);
        roleColorIndicator.setMaxSize(10, 10);

        roleColorIndicator.setStyle(
                "-fx-background-color: "
                        + roleColor + ";"
                        + "-fx-background-radius: 10;"
        );


        Label roleName = new Label(
                role.getName().toUpperCase()
        );

        roleName.getStyleClass().add(
                "user-role-title"
        );


        Label roleCounter = new Label(
                users.size()
                        + (users.size() == 1
                        ? " usuario"
                        : " usuarios")
        );

        roleCounter.getStyleClass().add(
                "user-role-count"
        );


        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        header.getChildren().addAll(
                roleColorIndicator,
                roleName,
                spacer,
                roleCounter
        );


        /* ================= USUARIOS ================= */

        VBox roleUsers = new VBox(6);

        roleUsers.getStyleClass().add(
                "role-users-list"
        );


        for (Staff user : users) {
            addListUser(user, roleUsers);
        }


        section.getChildren().addAll(
                header,
                roleUsers
        );


        return section;
    }

    private void refreshRoleFilter() {

        String previousSelection = cmbRoleFilter.getValue();

        List<String> roleNames = new ArrayList<>();

        roleNames.add(ALL_ROLES);

        RolesUseCase.getRoles()
                .stream()
                .map(Role::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(roleNames::add);

        cmbRoleFilter.getItems().setAll(roleNames);

        if (previousSelection != null
                && roleNames.contains(previousSelection)) {

            cmbRoleFilter.setValue(previousSelection);

        } else {

            cmbRoleFilter.setValue(ALL_ROLES);
        }
    }

    private void addListUser(
            Staff user,
            VBox destination
    ) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/servinet/components/user-row.fxml"
                    )
            );

            Node userRow = loader.load();


            UserRowController controller =
                    loader.getController();


            controller.setUser(
                    user,
                    this::editUser,
                    this::deleteUser
            );


            destination
                    .getChildren()
                    .add(userRow);


        } catch (IOException e) {

            e.printStackTrace();

            warn(
                    "No se pudo cargar visualmente al usuario "
                            + user.getDisplay()
            );
        }
    }

    private void updateUserSummary(int amount) {

        if (amount == 1) {
            lblUsersSummary.setText("1 usuario");
        } else {
            lblUsersSummary.setText(amount + " usuarios");
        }
    }

    @FXML
    private void userCreate() {

        if (!exigirPermiso(Permission.AD_CREATE_PERSONAL)) {
            return;
        }

        indexController.abrirModal(
                FormType.USER_CREATE,
                this::refreshAll,
                null
        );
    }

    private void editUser(Staff user) {

        if (!exigirPermiso(Permission.AD_EDIT_PERSONAL)) {
            return;
        }

        if (user == null || user.getRol() == null) {
            return;
        }

        // Proteger al propietario de ediciones
        // hechas por cuentas sin BYPASS global.
        boolean esPropietario = user.getRol()
                .hasPermission(Permission.BYPASS);

        if (esPropietario &&
                !PermissionValidation.hasPermission(Permission.BYPASS)) {

            warn("No puedes modificar una cuenta propietaria.");
            return;
        }

        indexController.abrirModal(
                FormType.USER_EDIT,
                this::refreshAll,
                user
        );
    }

    private void deleteUser(Staff user) {
        if (!exigirPermiso(Permission.AD_DELETE_PERSONAL)) {
            return;
        }

        if (user == null || user.getRol() == null) {
            return;
        }

        if (user.getUuid().equalsIgnoreCase(
                LoginStaffUseCase.getUserUuid())) {

            warn("No puedes eliminar tu propia cuenta.");
            return;
        }

        if (user.getRol().hasPermission(Permission.BYPASS)) {

            warn("No se puede eliminar una cuenta propietaria.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar a " + user.getDisplay() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                LoginStaffUseCase.deleteUser(user);
                refreshAll();
            } catch (RuntimeException e) {
                warn(e.getMessage());
            }
        }
    }

    // ===================== ACTIVIDAD Y LOGS =====================


    private void fillActivityCombo() {
        Staff selected = cmbActivityUser.getValue();

        String selectedUuid;

        if (selected != null) {
            selectedUuid = selected.getUuid();
        } else {
            selectedUuid = LoginStaffUseCase.getUserUuid();
        }

        for (Staff u : cmbActivityUser.getItems()) {
            if (u.getUuid().equalsIgnoreCase(selectedUuid)) {
                cmbActivityUser.setValue(u);
                break;
            }
        }
    }

    private void cargarActividad(Staff user) {

        if (!PermissionValidation.hasPermission(
                Permission.AD_ACTIVITY_PERSONAL)) {
            return;
        }
        activityGrid.getChildren().clear();

        int semanas = 30;
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(DayOfWeek.MONDAY).minusWeeks(semanas - 1);

        Map<LocalDate, Integer> conteo = new HashMap<>();
        if (user != null) {
            try {
                conteo = ActivityLogsService.getActivityByDay(user.getUuid(), inicio);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        for (int semana = 0; semana < semanas; semana++) {

            for (int dia = 0; dia < 7; dia++) {

                LocalDate fecha = inicio.plusWeeks(semana).plusDays(dia);
                int cantidad = fecha.isAfter(hoy) ? 0 : conteo.getOrDefault(fecha, 0);

                Region actividad = new Region();
                actividad.setMinSize(12, 12);
                actividad.setPrefSize(12, 12);
                actividad.setMaxSize(12, 12);

                int nivel;
                if (cantidad == 0) {
                    nivel = 0;
                } else if (cantidad <= 2) {
                    nivel = 1;
                } else if (cantidad <= 5) {
                    nivel = 2;
                } else if (cantidad <= 10) {
                    nivel = 3;
                } else {
                    nivel = 4;
                }

                actividad.getStyleClass().add("activity-level-" + nivel);

                Tooltip.install(actividad, new Tooltip(
                        cantidad + (cantidad == 1 ? " actividad" : " actividades")
                                + " el " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));

                // columna = semana, fila = día
                activityGrid.add(actividad, semana, dia);
            }
        }
    }

    @FXML
    private void viewLogs() {
        if (!exigirPermiso(Permission.AD_VIEW_LOG_PERSONAL)) {
            return;
        }
        Staff selected = cmbActivityUser.getValue();
        if (selected == null) {
            selected = LoginStaffUseCase.getActualSessionStaff();
        }

        List<LogEntry> logs;
        try {
            //logs = ActivityLogsService.getLogs(selected.getUuid(), 200);
        } catch (RuntimeException e) {
            warn("No se pudieron cargar los logs. ¿Ya creaste la tabla users_logs en SQL Server?");
            return;
        }

        TableView<LogEntry> table = new TableView<>();

        TableColumn<LogEntry, String> colDate = new TableColumn<>("Fecha");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().createAt().format(LOG_DATE)));
        colDate.setPrefWidth(150);

        TableColumn<LogEntry, String> colUser = new TableColumn<>("Usuario");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().userName()));
        colUser.setPrefWidth(140);

        TableColumn<LogEntry, String> colType = new TableColumn<>("Acción");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));
        colType.setPrefWidth(140);

        TableColumn<LogEntry, String> colInfo = new TableColumn<>("Detalle");
        colInfo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().information()));
        colInfo.setPrefWidth(340);

        table.getColumns().add(colDate);
        table.getColumns().add(colUser);
        table.getColumns().add(colType);
        table.getColumns().add(colInfo);
        table.getItems().setAll(logs);
        table.setPrefSize(800, 450);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Registro de actividad");
        dialog.setHeaderText("Últimas 200 acciones de " + selected.getDisplay());
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void updateRolesSummary() {

        int totalRoles = RolesUseCase.getRoles().size();

        int totalUsersWithRole = 0;

        for (Staff user : LoginStaffUseCase.getAllStaff()) {

            if (user != null && user.getRol() != null) {
                totalUsersWithRole++;
            }
        }


        lblRolesCount.setText(
                totalRoles == 1
                        ? "1 rol"
                        : totalRoles + " roles"
        );


        lblUsersWithRoleCount.setText(
                totalUsersWithRole == 1
                        ? "1 usuario"
                        : totalUsersWithRole + " usuarios"
        );
    }

    // ===================== OPCIONES DEL PROPIETARIO =====================


    @FXML
    private void appRename() {

        if (!exigirPermiso(Permission.APP_CHANGE_NAME)) {
            return;
        }

        indexController.abrirModal(FormType.APP_RENAME);
    }


    @FXML
    private void appChangeLogo() {

        if (!exigirPermiso(Permission.APP_CHANGE_LOGO)) {
            return;
        }

        indexController.abrirModal(FormType.APP_CHANGE_IMAGE);
    }


    @FXML
    private void appSecurity() {

        if (!exigirPermiso(Permission.APP_SECURITY_CONFIG)) {
            return;
        }

        indexController.abrirModal(FormType.APP_SECURITY);
    }


    @FXML
    private void appSectors() {

        if (!exigirPermiso(Permission.APP_CONF_SECTOR_ANT)) {
            return;
        }

        indexController.abrirModal(FormType.APP_SECTOR_ANTENNA);
    }


    @FXML
    private void appRolPermission() {

        if (!exigirPermiso(Permission.APP_CONF_ROL_PERMS)) {
            return;
        }

        indexController.abrirModal(FormType.APP_ROL_PERMISSION);
    }


    @FXML
    private void appConfig() {

        if (!exigirPermiso(Permission.APP_GENERAL_CONFIG)) {
            return;
        }

        indexController.abrirModal(FormType.APP_SETTING);
    }


}