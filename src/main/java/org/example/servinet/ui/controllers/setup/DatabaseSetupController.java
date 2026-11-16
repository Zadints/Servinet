package org.example.servinet.ui.controllers.setup;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.AuthenticationMode;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.ui.events.MouseMove;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DatabaseSetupController {


    @FXML private HBox root;
    @FXML private PrivacyPolicyController privacyController; // fx:include fx:id="privacy"
    @FXML private VBox stepPrivacy;
    @FXML private ScrollPane stepMode, stepSqlServer;
    @FXML private VBox stepFinish;
    @FXML private Label navPrivacy, navMode, navSqlServer, navFinish;
    @FXML private Label lblSetupSummary, lblStatus;
    @FXML private RadioButton rbSingleDevice, rbMultiDevice;
    @FXML private TextField txtServerHost, txtServerPort, txtDatabase, txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbAuthentication;
    @FXML private Label lblUsername, lblPassword;
    @FXML private Button btnBack, btnNext, btnContinue, btnCancel;
    @FXML private VBox progressContainer;
    @FXML private ProgressBar progressInstall;

    private Consumer<DatabaseSetup> saveHandler;
    private Consumer<DatabaseSetup> cancelHandler;

    private DeploymentMode selectedMode = DeploymentMode.SINGLE_DEVICE;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private int currentStep = 0;
    private boolean processing;

    @FXML
    private void initialize() {
        MouseMove move = new MouseMove(root);
        move.start();

        cmbAuthentication.getItems().setAll("SQL Server");
        cmbAuthentication.getSelectionModel().selectFirst();

        renderStep();
    }

    public void setSaveHandler(Consumer<DatabaseSetup> handler) {
        this.saveHandler = handler;
    }

    public void setCancelHandler(Consumer<DatabaseSetup> handler) {
        this.cancelHandler = handler;
    }


    @FXML
    private void onNextStep() {
        if (processing) return;
        switch (currentStep) {
            case 0 -> {
                if (!privacyController.isAccepted()) {
                    showStatus("Debes aceptar la política de privacidad para continuar.");
                    return;
                }
                currentStep = 1;
            }
            case 1 -> {
                selectedMode = rbMultiDevice.isSelected()
                        ? DeploymentMode.MULTI_DEVICE : DeploymentMode.SINGLE_DEVICE;
                currentStep = selectedMode == DeploymentMode.SINGLE_DEVICE ? 3 : 2;
            }
            case 2 -> {
                host = txtServerHost.getText().trim();
                databaseName = txtDatabase.getText().trim();
                username = txtUsername.getText().trim();
                password = txtPassword.getText();
                try {
                    port = Integer.parseInt(txtServerPort.getText().trim());
                } catch (NumberFormatException ex) {
                    showStatus("El puerto debe ser numérico.");
                    return;
                }
                if (host.isBlank() || databaseName.isBlank() || port < 1 || port > 65535) {
                    showStatus("Completa servidor, puerto válido y base de datos.");
                    return;
                }
                currentStep = 3;
            }
            default -> { return; }
        }
        showStatus("");
        renderStep();
    }

    @FXML
    private void onPreviousStep() {
        if (processing || currentStep == 0) return;
        currentStep = currentStep == 3
                ? (selectedMode == DeploymentMode.SINGLE_DEVICE ? 1 : 2)
                : currentStep - 1;
        showStatus("");
        renderStep();
    }

    @FXML
    private void onContinue() {
        if (processing || currentStep != 3) return;
        if (saveHandler == null) {
            showStatus("No se ha configurado el procesador de instalación.");
            return;
        }
        DatabaseSetup setup = new DatabaseSetup(
                selectedMode,
                selectedMode == DeploymentMode.MULTI_DEVICE ? host : null,
                selectedMode == DeploymentMode.MULTI_DEVICE ? port : null,
                selectedMode == DeploymentMode.MULTI_DEVICE ? databaseName : null,
                selectedMode == DeploymentMode.MULTI_DEVICE ? username : null,
                selectedMode == DeploymentMode.MULTI_DEVICE ? password : null
        );
        setProcessing(true);

        Platform.runLater(() -> {
            try {
                saveHandler.accept(setup);
            } catch (RuntimeException ex) {
                showStatus("Error durante la instalación: " + ex.getMessage());
                setProcessing(false);
            }
        });
    }

    @FXML
    private void onCancel() {
        if (processing) return;
        if (cancelHandler != null) cancelHandler.accept(null);
    }

    public void showStatus(String message) {
        lblStatus.setText(message == null ? "" : message);
    }

    /** Llamar desde el FX Application Thread al terminar o fallar la tarea. */
    public void setProcessing(boolean value) {
        processing = value;
        progressContainer.setVisible(value);
        progressContainer.setManaged(value);
        progressInstall.setProgress(-1); // indeterminado
        btnContinue.setDisable(value);
        btnNext.setDisable(value);
        btnBack.setDisable(value);
        btnCancel.setDisable(value);
    }

    private void renderStep() {
        toggle(stepPrivacy, currentStep == 0);
        toggle(stepMode, currentStep == 1);
        toggle(stepSqlServer, currentStep == 2);
        toggle(stepFinish, currentStep == 3);
        toggle(btnBack, currentStep != 0);
        toggle(btnNext, currentStep != 3);
        toggle(btnContinue, currentStep == 3);
        setNav(navPrivacy, 0);
        setNav(navMode, 1);
        setNav(navSqlServer, 2);
        setNav(navFinish, 3);

        navSqlServer.setText(selectedMode == DeploymentMode.SINGLE_DEVICE
                ? "03  SQL Server (omitido)" : "03  Conexión SQL Server");
    }

    private static void toggle(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void setNav(Label label, int index) {
        String css = index == currentStep ? "sidebar-step-active"
                : index < currentStep ? "sidebar-step-completed" : "sidebar-step";
        label.getStyleClass().removeAll("sidebar-step", "sidebar-step-active", "sidebar-step-completed");
        label.getStyleClass().add(css);
    }

}
