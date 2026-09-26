package org.example.servinet.ui.controllers.center;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.usecase.LogsUseCase;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.LogEntry;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.core.domain.enums.Permission;
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
    @FXML private ComboBox<User> cmbActivityUser;

    @FXML
    private Label lblRolesCount;

    @FXML
    private Label lblUsersWithRoleCount;

    /* Filtros de usuarios */
    @FXML private TextField txtUserSearch;
    @FXML private ComboBox<String> cmbRoleFilter;
    @FXML private Label lblUsersSummary;

    @FXML private Button btnCreateUser;
    @FXML private VBox optName;
    @FXML private VBox optLogo;
    @FXML private VBox optSecurity;
    @FXML private VBox optConfig;
    @FXML private VBox optRoles;

    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {

        setupUserFilters();

        loadUsers();

        fillActivityCombo();

        updateRolesSummary();

        cmbActivityUser.setOnAction(e ->
                cargarActividad(cmbActivityUser.getValue())
        );

        cargarActividad(cmbActivityUser.getValue());

        applyPermissions();
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    // ===================== PERMISOS =====================

    private boolean can(Permission p) {
        return PermissionValidation.hasPermission(p);
    }

    private void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void applyPermissions() {
        show(btnCreateUser, can(Permission.AD_CREATE_USER));
        show(optName, can(Permission.APP_CHANGE_NAME));
        show(optLogo, can(Permission.APP_CHANGE_LOGO));
        show(optSecurity, can(Permission.APP_SECURITY_CONFIG));
        show(optConfig, can(Permission.APP_GENERAL_CONFIG));
        show(optRoles, can(Permission.APP_CONF_ROL_PERMS));
    }

    private void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===================== USUARIOS =====================

    private static final String ALL_ROLES = "Todos los roles";


    private void setupUserFilters() {

        cmbRoleFilter.setOnAction(e -> renderUsers());

        txtUserSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> renderUsers()
        );
    }

    private void refreshAll() {

        loadUsers();

        fillActivityCombo();

        updateRolesSummary();
    }

    private void loadUsers() {

        SessionUseCase.loadAllUsers();

        refreshRoleFilter();

        renderUsers();
    }

    private void renderUsers() {

        usersList.getChildren().clear();

        if (!can(Permission.AD_VIEW_USERS)) {

            Label noPermission = new Label(
                    "No tienes permiso para visualizar los usuarios."
            );

            noPermission.getStyleClass().add("secondary-text");

            usersList.getChildren().add(noPermission);

            lblUsersSummary.setText("Sin acceso");

            return;
        }


        String search = txtUserSearch.getText();

        if (search == null) {
            search = "";
        }

        search = search
                .trim()
                .toLowerCase(Locale.ROOT);


        String selectedRole = cmbRoleFilter.getValue();

        if (selectedRole == null) {
            selectedRole = ALL_ROLES;
        }


        List<User> filteredUsers = new ArrayList<>();


        for (User user : SessionUseCase.getUsersList()) {

            if (user == null || user.getRol() == null) {
                continue;
            }


            /* FILTRAR POR ROL */

            boolean roleMatches =
                    ALL_ROLES.equals(selectedRole)
                            || user.getRolName()
                            .equalsIgnoreCase(selectedRole);


            /* FILTRAR POR TEXTO */

            String name = user.getName() == null
                    ? ""
                    : user.getName().toLowerCase(Locale.ROOT);

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
                                (User u) -> u.getRolName().toLowerCase(Locale.ROOT)
                        )
                        .thenComparing(
                                u -> u.getName().toLowerCase(Locale.ROOT)
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
        Map<String, List<User>> usersByRole = new LinkedHashMap<>();

        Map<String, Role> rolesByUuid = new LinkedHashMap<>();


        for (User user : filteredUsers) {

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
        for (Map.Entry<String, List<User>> entry : usersByRole.entrySet()) {

            Role role = rolesByUuid.get(entry.getKey());

            List<User> roleUsers = entry.getValue();

            VBox roleSection = createRoleSection(
                    role,
                    roleUsers
            );

            usersList.getChildren().add(roleSection);
        }
    }

    private VBox createRoleSection(
            Role role,
            List<User> users
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


        for (User user : users) {
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
            User user,
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
                            + user.getName()
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
        indexController.abrirModal(FormType.USER_CREATE, this::refreshAll, null);
    }

    private void editUser(User user) {
        indexController.abrirModal(FormType.USER_EDIT, this::refreshAll, user);
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar a " + user.getName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SessionUseCase.deleteUser(user);
                refreshAll();
            } catch (RuntimeException e) {
                warn(e.getMessage());
            }
        }
    }

    // ===================== ACTIVIDAD Y LOGS =====================


    private void fillActivityCombo() {
        User selected = cmbActivityUser.getValue();
        String selectedUuid = selected != null ? selected.getUuid() : SessionUseCase.getUserUuid();

        if (can(Permission.AD_ACTIVITY_VUSERS)) {
            cmbActivityUser.getItems().setAll(SessionUseCase.getUsersList());
        } else {
            cmbActivityUser.getItems().setAll(SessionUseCase.getActualSessionUser());
        }

        for (User u : cmbActivityUser.getItems()) {
            if (u.getUuid().equalsIgnoreCase(selectedUuid)) {
                cmbActivityUser.setValue(u);
                break;
            }
        }
    }

    private void cargarActividad(User user) {

        activityGrid.getChildren().clear();

        int semanas = 30;
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.with(DayOfWeek.MONDAY).minusWeeks(semanas - 1);

        Map<LocalDate, Integer> conteo = new HashMap<>();
        if (user != null) {
            try {
                conteo = LogsUseCase.getActivityByDay(user.getUuid(), inicio);
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
        User selected = cmbActivityUser.getValue();
        if (selected == null) {
            selected = SessionUseCase.getActualSessionUser();
        }

        List<LogEntry> logs;
        try {
            logs = LogsUseCase.getLogs(selected.getUuid(), 200);
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
        dialog.setHeaderText("Últimas 200 acciones de " + selected.getName());
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void updateRolesSummary() {

        int totalRoles = RolesUseCase.getRoles().size();

        int totalUsersWithRole = 0;

        for (User user : SessionUseCase.getUsersList()) {

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
    private void appRename(){
        indexController.abrirModal(FormType.APP_RENAME);
    }
    @FXML
    private void appChangeLogo(){
        indexController.abrirModal(FormType.APP_CHANGE_IMAGE);
    }
    @FXML
    private void appSecurity(){
        indexController.abrirModal(FormType.APP_SECURITY);
    }
    @FXML
    private void appSectors(){
        indexController.abrirModal(FormType.APP_SECTOR_ANTENNA);
    }
    @FXML
    private void appRolPermission(){
        indexController.abrirModal(FormType.APP_ROL_PERMISSION);
    }
    @FXML
    private void appConfig(){
        indexController.abrirModal(FormType.APP_SETTING);
    }

}