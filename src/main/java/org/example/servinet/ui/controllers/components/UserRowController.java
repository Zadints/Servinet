package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.Permission;

import java.util.function.Consumer;

public class UserRowController {
    @FXML private Circle userAvatar;
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Label userRol;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private User user;
    private Consumer<User> onEditAction;
    private Consumer<User> onDeleteAction;

    public void setUser(User user, Consumer<User> onEdit, Consumer<User> onDelete) {
        this.user = user;
        this.onEditAction = onEdit;
        this.onDeleteAction = onDelete;

        boolean isMe = user.getUuid().equalsIgnoreCase(SessionUseCase.getUserUuid());

        userName.setText(isMe ? user.getName() + " (tú)" : user.getName());
        userEmail.setText(user.getEmail());
        userRol.setText(user.getRolName());

        userRol.setStyle("-fx-text-fill: " + user.getRol().getHexColor() + "; -fx-font-weight: bold;");

        if (user.getPerfilImg() != null) {
            userAvatar.setFill(new ImagePattern(user.getPerfilImg()));
        }

        boolean canEdit = PermissionValidation.hasPermission(Permission.AD_EDIT_USER);
        boolean canDelete = PermissionValidation.hasPermission(Permission.AD_DELETE_USER)
                && !isMe
                && !user.getRol().hasPermission(Permission.BYPASS);

        btnEdit.setVisible(canEdit);
        btnEdit.setManaged(canEdit);
        btnDelete.setVisible(canDelete);
        btnDelete.setManaged(canDelete);
    }

    @FXML
    private void onEdit() {
        if (onEditAction != null) onEditAction.accept(user);
    }

    @FXML
    private void onDelete() {
        if (onDeleteAction != null) onDeleteAction.accept(user);
    }
}