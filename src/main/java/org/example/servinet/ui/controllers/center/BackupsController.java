package org.example.servinet.ui.controllers.center;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.domain.enums.Permission;


public class BackupsController {


    @FXML private VBox sectionBackupAcciones;
    @FXML private VBox sectionBackupEstado;
    @FXML private VBox sectionBackupFiltros;
    @FXML private VBox sectionBackupHistorial;

    @FXML private VBox backupList;

    @FXML private Button btnCrearBackup;
    @FXML private Button btnVerificarBackup;
    @FXML private Button btnRestaurarBackup;

    @FXML private Label lblSinPermisosBackup;
    @FXML private Label lblTotalBackups;
    @FXML private Label lblBackupSize;
    @FXML private Label lblBackupStatus;
    @FXML private Label lblBackupDirectory;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbBackupType;
    @FXML private ComboBox<String> cmbBackupStatus;



    @FXML
    private void initialize() {

        PermissionPolicy policy = PermissionPolicy.current();

        boolean puedeAdministrarBackups = policy.allows(
                Permission.BACKUPS_BYPASS
        );

        // Mostrar solamente las secciones autorizadas.
        mostrar(
                sectionBackupAcciones,
                puedeAdministrarBackups
        );

        mostrar(
                sectionBackupEstado,
                puedeAdministrarBackups
        );

        mostrar(
                sectionBackupHistorial,
                puedeAdministrarBackups
        );

        // Los filtros todavía no tienen lógica
        // de consulta ni datos reales.
        mostrar(sectionBackupFiltros, false);

        mostrar(
                lblSinPermisosBackup,
                !puedeAdministrarBackups
        );

        if (!puedeAdministrarBackups) {
            return;
        }

        // El historial original contiene datos ficticios.
        // Los retiramos para no mostrar backups inexistentes.
        backupList.getChildren().clear();

        Label pendiente = new Label(
                "Todavía no se ha implementado la consulta " +
                        "de copias de seguridad reales."
        );

        pendiente.setWrapText(true);
        pendiente.getStyleClass().add("tertiary-text");

        backupList.getChildren().add(pendiente);

        // No mostrar cifras ni rutas inventadas.
        lblTotalBackups.setText("—");
        lblBackupSize.setText("—");
        lblBackupStatus.setText("Sin implementar");
        lblBackupDirectory.setText(
                "Directorio de respaldos sin configurar"
        );

        // Las operaciones todavía no tienen implementación.
        configurarBotonPendiente(btnCrearBackup);
        configurarBotonPendiente(btnVerificarBackup);
        configurarBotonPendiente(btnRestaurarBackup);
    }


    private void mostrar(Node nodo, boolean permitido) {

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }


    private void configurarBotonPendiente(Button boton) {

        boton.setDisable(true);

        boton.setTooltip(
                new Tooltip(
                        "Operación pendiente de implementación"
                )
        );
    }


    private boolean exigirPermisoBackup() {

        if (AuthorizationService.hasPermission(
                Permission.BACKUPS_BYPASS)) {

            return true;
        }

        Alert alerta = new Alert(Alert.AlertType.WARNING);

        alerta.setTitle("Acceso denegado");
        alerta.setHeaderText("Permiso insuficiente");

        alerta.setContentText(
                "Tu rol no permite administrar copias de seguridad."
        );

        alerta.showAndWait();

        return false;
    }


    private void avisarPendiente(String operacion) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("Función pendiente");
        alerta.setHeaderText(operacion);

        alerta.setContentText(
                "La operación aún no está implementada. " +
                        "No se ha modificado la base de datos."
        );

        alerta.showAndWait();
    }



    @FXML
    public void createBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Crear copia de seguridad");
    }


    @FXML
    public void verifyBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Verificar copia de seguridad");
    }


    @FXML
    public void restoreBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Restaurar base de datos");
    }


    @FXML
    public void deleteSelectedBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Eliminar copia de seguridad");
    }


    @FXML
    public void restoreSelectedBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Restaurar copia seleccionada");
    }


    @FXML
    public void verifySelectedBackup() {

        if (!exigirPermisoBackup()) {
            return;
        }

        avisarPendiente("Verificar copia seleccionada");
    }


    @FXML
    public void clearFilters() {

        if (!exigirPermisoBackup()) {
            return;
        }

        txtSearch.clear();

        cmbBackupType.getSelectionModel()
                .clearSelection();

        cmbBackupStatus.getSelectionModel()
                .clearSelection();
    }



}
