package org.example.servinet.ui.controllers.setup;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.AuthenticationMode;
import org.example.servinet.application.enums.DeploymentMode;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DatabaseSetupController {

    @FXML private RadioButton rbSingleDevice, rbMultiDevice;
    @FXML private VBox singleDeviceSection, multiDeviceSection;
    @FXML private TextField txtLocalInstance, txtServerHost, txtServerPort, txtDatabase, txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbAuthentication;
    @FXML private Label lblUsername, lblPassword, lblStatus;
    @FXML private TextField txtDataDirectory;
    @FXML private Button btnBrowseDirectory;
/**
    Integra este controlador con tu caso de uso de conexión/guardado del bootstrap.
    Los callbacks reciben los datos validados y NO ejecutan SQL por sí solos.
 **/

    private Consumer<DatabaseSetup> saveHandler;

    @FXML
    private void initialize() {
        String localAppData = System.getenv("LOCALAPPDATA");

        Path root = Path.of(
                localAppData != null
                        ? localAppData
                        : System.getProperty("user.home"),
                "Servinet"
        );

        txtDataDirectory.setText(root.toString());

        cmbAuthentication.getItems().setAll("Autenticación de Windows", "Usuario y contraseña SQL Server");
        cmbAuthentication.getSelectionModel().select(1);
        rbSingleDevice.selectedProperty().addListener((obs, oldValue, selected) -> updateMode());
        rbMultiDevice.selectedProperty().addListener((obs, oldValue, selected) -> updateMode());
        cmbAuthentication.valueProperty().addListener((obs, oldValue, value) -> updateAuth());
        updateMode();
        updateAuth();
    }

    private void updateMode() {
        boolean single = rbSingleDevice.isSelected();
        singleDeviceSection.setVisible(single);
        singleDeviceSection.setManaged(single);
        multiDeviceSection.setVisible(!single);
        multiDeviceSection.setManaged(!single);
    }

    private void updateAuth() {
        boolean sqlAuth = cmbAuthentication.getSelectionModel().getSelectedIndex() == 1;
        lblUsername.setVisible(sqlAuth); lblUsername.setManaged(sqlAuth);
        txtUsername.setVisible(sqlAuth); txtUsername.setManaged(sqlAuth);
        lblPassword.setVisible(sqlAuth); lblPassword.setManaged(sqlAuth);
        txtPassword.setVisible(sqlAuth); txtPassword.setManaged(sqlAuth);
    }

    private DatabaseSetup capture() {
        boolean single = rbSingleDevice.isSelected();
        String host = (single ? txtLocalInstance.getText() : txtServerHost.getText()).trim();
        String database = txtDatabase.getText().trim();
        if (host.isBlank() || database.isBlank())
            throw new IllegalArgumentException("Completa el servidor/instancia y el nombre de la base de datos.");

        Integer port = null;
        if (!single) {
            try {
                port = Integer.parseInt(txtServerPort.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("El puerto debe ser un número entre 1 y 65535.");
            }
            if (port < 1 || port > 65535)
                throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        }
        boolean sqlAuth = cmbAuthentication.getSelectionModel().getSelectedIndex() == 1;
        String username = sqlAuth ? txtUsername.getText().trim() : "";
        String password = sqlAuth ? txtPassword.getText() : "";
        if (sqlAuth && (username.isBlank() || password.isBlank()))
            throw new IllegalArgumentException("Ingresa el usuario y la contraseña SQL Server.");

        return new DatabaseSetup(single ? DeploymentMode.SINGLE_DEVICE : DeploymentMode.MULTI_DEVICE,
                host, port, database,
                sqlAuth ? AuthenticationMode.SQL_SERVER : AuthenticationMode.WINDOWS,
                username, password);
    }

    @FXML private void onContinue() { dispatch(saveHandler, "Conecta Guardar y continuar a tu bootstrap."); }
    @FXML private void onBrowseDirectory() {

        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle("Seleccionar carpeta de datos de Servinet");

        File selected = chooser.showDialog(
                btnBrowseDirectory.getScene().getWindow()
        );

        if (selected != null) {
            txtDataDirectory.setText(selected.getAbsolutePath());
        }
    }

    private void dispatch(Consumer<DatabaseSetup> handler, String missingHandlerMessage) {
        try {
            DatabaseSetup data = capture();
            if (handler == null) {
                lblStatus.setText(missingHandlerMessage);
                return;
            }
            lblStatus.setText("");
            handler.accept(data);
        } catch (IllegalArgumentException exception) {
            lblStatus.setText(exception.getMessage());
        }
    }

    public void setSaveHandler(Consumer<DatabaseSetup> handler) {
        this.saveHandler = handler;
    }
    public void showStatus(String message) { lblStatus.setText(message); }
}
