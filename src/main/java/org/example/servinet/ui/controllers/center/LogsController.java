package org.example.servinet.ui.controllers.center;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.servinet.application.usecase.GetLogUseCase;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.ui.controllers.components.LogEntryController;

import java.io.IOException;
import java.util.*;

public class LogsController {
    @FXML
    private ComboBox<LogType> cmbLogType;
    @FXML private VBox logsContainer;
    @FXML private Label lblTotalLogs;
    @FXML private Label lblEmptyLogs;
    private final GetLogUseCase getLogUseCase;
    private Map<LogType, List<LogEntry>> LOGS = new EnumMap<>(LogType.class);

    public LogsController(GetLogUseCase getLogUseCase) {
        this.getLogUseCase = getLogUseCase;
    }

    @FXML
    private void initialize() {
        cmbLogType.setItems(FXCollections.observableArrayList(LogType.values()));
        setLogs();
    }

    public void setLogs() {
        LOGS = getLogUseCase.getLogs(200);
        renderLogs();
    }
    @FXML
    private void filterLogs() {
        renderLogs();
    }

    @FXML
    private void clearFilter() {
        cmbLogType.getSelectionModel().clearSelection();
        renderLogs();
    }

    private void renderLogs() {
        if (logsContainer == null) return;
        LogType selected = cmbLogType.getValue();

        List<LogEntry> filtered;

        if (selected == null) {
            filtered = LOGS.values().stream()
                    .flatMap(List::stream)
                    .toList();
        } else {
            filtered = LOGS.getOrDefault(selected, List.of());
        }

        logsContainer.getChildren().clear();
        for (LogEntry log : filtered) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/org/example/servinet/components/log-entry.fxml"));
                Parent node = loader.load();
                LogEntryController controller = loader.getController();
                controller.setLog(log);
                logsContainer.getChildren().add(node);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo cargar el componente de log", e);
            }
        }
        lblTotalLogs.setText(filtered.size() + " registros");
        boolean empty = filtered.isEmpty();
        lblEmptyLogs.setVisible(empty);
        lblEmptyLogs.setManaged(empty);
    }
}
