package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.domain.entities.LogEntry;

import java.time.format.DateTimeFormatter;

public class LogEntryController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private Label lblUserName;
    @FXML private Label lblInformation;
    @FXML private Label lblType;
    @FXML private Label lblCreateAt;

    public void setLog(LogEntry log) {
        lblUserName.setText(log.userName() == null ? "Desconocido" : log.userName());
        lblInformation.setText(log.information() == null ? "" : log.information());
        lblType.setText(log.type() == null ? "Sin tipo" : log.type().getDisplayName());
        lblCreateAt.setText(log.createAt() == null ? "Sin fecha" : log.createAt().format(DATE_FORMAT));
    }
}
