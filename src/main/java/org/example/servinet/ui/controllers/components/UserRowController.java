package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.events.ImageConverter;

import java.util.function.Consumer;

public class UserRowController {
    @FXML private Circle userAvatar;
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Label userRol;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Staff user;
    private Consumer<Staff> onEditAction;
    private Consumer<Staff> onDeleteAction;

    private final GetStaffUseCase getStaffUseCase;
    private final AuthorizationSecurity authorizationSecurity;

    public UserRowController(GetStaffUseCase getStaffUseCase, AuthorizationSecurity authorizationSecurity) {
        this.getStaffUseCase = getStaffUseCase;
        this.authorizationSecurity = authorizationSecurity;
    }

    public void setUser(Staff user, Consumer<Staff> onEdit, Consumer<Staff> onDelete) {
        this.user = user;
        this.onEditAction = onEdit;
        this.onDeleteAction = onDelete;

        boolean isMe = user.getUuid().equalsIgnoreCase(getStaffUseCase.getCurrentStaff().getUuid());

        userName.setText(isMe ? user.getDisplay() + " (tú)" : user.getDisplay());
        userEmail.setText(user.getEmail());
        userRol.setText(user.getRolName());

        userRol.setStyle("-fx-text-fill: " + user.getRol().getHexColor() + "; -fx-font-weight: bold;");

        if (user.getPerfilImg() != null) {
            userAvatar.setFill(new ImagePattern(
                    ImageConverter.toImage(
                            getStaffUseCase.getCurrentStaff().getPerfilImg())));
        }

        boolean esPropietario = user.getRol()
                .hasPermission(Permission.BYPASS);

        boolean canEdit =
                authorizationSecurity.hasPermission(
                        Permission.AD_EDIT_PERSONAL
                )
                        && (!esPropietario ||
                        authorizationSecurity.hasPermission(
                                Permission.BYPASS
                        ));
        boolean canDelete = authorizationSecurity.hasPermission(Permission.AD_DELETE_PERSONAL)
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