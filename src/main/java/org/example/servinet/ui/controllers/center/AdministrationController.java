package org.example.servinet.ui.controllers.center;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.usecase.LogsUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.LogEntry;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.UserRowController;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdministrationController {
    private IndexController indexController;

    @FXML private GridPane activityGrid;
    @FXML private VBox usersList;
    @FXML private ComboBox<User> cmbActivityUser;


    @FXML private Button btnCreateUser;
    @FXML private VBox optName;
    @FXML private VBox optLogo;
    @FXML private VBox optSecurity;
    @FXML private VBox optConfig;
    @FXML private VBox optRoles;

    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        loadUsers();
        fillActivityCombo();
        cmbActivityUser.setOnAction(e -> cargarActividad(cmbActivityUser.getValue()));
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

    private void refreshAll() {
        loadUsers();
        fillActivityCombo();
    }

    private void loadUsers() {
        SessionUseCase.loadAllUsers();
        usersList.getChildren().clear();

        if (!can(Permission.AD_VIEW_USERS)) {
            return;
        }
        for (User u : SessionUseCase.getUsersList()) {
            addListUser(u);
        }
    }

    private void addListUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/user-row.fxml")
            );
            Node userRow = loader.load();
            UserRowController controller = loader.getController();
            controller.setUser(user, this::editUser, this::deleteUser);
            usersList.getChildren().add(userRow);
        } catch (IOException e) {
            e.printStackTrace();
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