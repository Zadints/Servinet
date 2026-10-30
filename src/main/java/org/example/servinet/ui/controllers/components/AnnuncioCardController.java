package org.example.servinet.ui.controllers.components;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.servinet.application.usecase.announce.EventsAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.ui.events.ImageConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class AnnuncioCardController {

    @FXML private Label lblAutor;
    @FXML private Label lblFecha;
    @FXML private Label lblPrioridad;
    @FXML private Label lblTitulo;
    @FXML private Label lblContenido;
    @FXML private StackPane mediaContainer;

    @FXML private Button btnLike;
    @FXML private Button btnComentarios;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    @FXML private VBox commentsPanel;
    @FXML private VBox commentsList;
    @FXML private TextField txtNewComment;

    private Announcement announcement;
    private Consumer<Announcement> onEdit;
    private Consumer<Announcement> onDelete;
    private boolean commentsLoaded = false;

    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EventsAnnounceUseCase eventsAnnounceUseCase;
    private final GetAnnounceUseCase getAnnounceUseCase;

    public AnnuncioCardController(EventsAnnounceUseCase eventsAnnounceUseCase, GetAnnounceUseCase getAnnounceUseCase) {
        this.eventsAnnounceUseCase = eventsAnnounceUseCase;
        this.getAnnounceUseCase = getAnnounceUseCase;
    }


    public void setAnnouncement(Announcement announcement,
                                Consumer<Announcement> onEdit,
                                Consumer<Announcement> onDelete) {

        this.announcement = announcement;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.commentsLoaded = false;

        lblAutor.setText(announcement.getAuthorName());
        lblFecha.setText(formatRelative(announcement.getSendAt())
                + (announcement.getUpdateAt() != null ? " · editado" : ""));
        lblTitulo.setText(announcement.getTitle());
        lblContenido.setText(announcement.getDescription());

        lblPrioridad.setText("Prioridad " + announcement.getPriority().toLowerCase());
        lblPrioridad.setStyle("-fx-font-weight: bold; -fx-text-fill: " + priorityColor(announcement.getPriority()) + ";");

        mediaContainer.getChildren().clear();
        boolean hasImage = announcement.getImage() != null;
        if (hasImage) {
            ImageView imageView = new ImageView(ImageConverter.toImage(announcement.getImage()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(480);
            imageView.setFitHeight(320);
            mediaContainer.getChildren().add(imageView);
        }
        mediaContainer.setVisible(hasImage);
        mediaContainer.setManaged(hasImage);

        updateLikeButton();
        updateCommentsButton();

        //boolean canEdit = eventsAnnounceUseCase.canEdit(announcement);
        boolean canDelete = eventsAnnounceUseCase.canDelete(announcement);
        //btnEditar.setVisible(canEdit);
        //btnEditar.setManaged(canEdit);
        btnEliminar.setVisible(canDelete);
        btnEliminar.setManaged(canDelete);
    }

    private static String priorityColor(String priority) {
        return switch (priority) {
            case "Alta" -> "#E5484D";
            case "Baja" -> "#30A46C";
            default -> "#F5A524";
        };
    }

    private void updateLikeButton() {
        btnLike.setText("👍 " + announcement.getLikeCount());
        btnLike.setStyle(announcement.isLikedByMe() ? "-fx-text-fill: #22A5F1; -fx-font-weight: bold;" : "");
    }

    private void updateCommentsButton() {
        btnComentarios.setText("💬 " + announcement.getCommentCount());
    }

    @FXML
    private void onLikeClicked() {
        try {
            eventsAnnounceUseCase.toggleLike(announcement);
            updateLikeButton();
        } catch (RuntimeException e) {
            warn(e.getMessage());
        }
    }

    @FXML
    private void onToggleComments() {
        boolean show = !commentsPanel.isVisible();
        commentsPanel.setVisible(show);
        commentsPanel.setManaged(show);

        if (show && !commentsLoaded) {
            loadComments();
        }
    }

    private void loadComments() {
        commentsList.getChildren().clear();
        try {
            for (AnnouncementComment comment : getAnnounceUseCase.getComments(announcement)) {
                appendCommentRow(comment);
            }
            commentsLoaded = true;
        } catch (RuntimeException e) {
            warn("No se pudieron cargar los comentarios.");
        }
    }

    private void appendCommentRow(AnnouncementComment comment) {
        Label author = new Label(comment.getAuthorName() + " · " + formatRelative(comment.getCreateAt()));
        author.getStyleClass().add("secondary-text");

        Label content = new Label(comment.getContent());
        content.getStyleClass().add("tertiary-text");
        content.setWrapText(true);

        VBox body = new VBox(2, author, content);
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox row = new HBox(8, body);
        row.setAlignment(Pos.TOP_LEFT);

        boolean canEdit = eventsAnnounceUseCase.canEditComment(comment);
        boolean canDelete = eventsAnnounceUseCase.canDeleteComment(comment);

        if (canEdit || canDelete) {
            Label dots = new Label("···");
            dots.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-cursor: hand; -fx-padding: 0 8 0 8;");
            dots.setVisible(false);

            ContextMenu menu = new ContextMenu();

            if (canEdit) {
                MenuItem edit = new MenuItem("Editar");
                edit.setOnAction(e -> startEditComment(row, content, comment));
                menu.getItems().add(edit);
            }
            if (canDelete) {
                MenuItem delete = new MenuItem("Eliminar");
                delete.setOnAction(e -> deleteComment(row, comment));
                menu.getItems().add(delete);
            }

            dots.setOnMouseClicked(e -> menu.show(dots, Side.BOTTOM, 0, 0));
            row.setOnMouseEntered(e -> dots.setVisible(true));
            row.setOnMouseExited(e -> {
                if (!menu.isShowing()) dots.setVisible(false);
            });
            menu.setOnHidden(e -> {
                if (!row.isHover()) dots.setVisible(false);
            });

            row.getChildren().add(dots);
        }

        commentsList.getChildren().add(row);
    }

    private void startEditComment(HBox row, Label content, AnnouncementComment comment) {
        TextField field = new TextField(comment.getContent());
        field.getStyleClass().add("secondary-input");
        HBox.setHgrow(field, Priority.ALWAYS);

        Button save = new Button("Guardar");
        save.getStyleClass().add("primary-button");

        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("secondary-button");

        HBox editor = new HBox(8, field, save, cancel);
        editor.setAlignment(Pos.CENTER);

        commentsList.getChildren().set(commentsList.getChildren().indexOf(row), editor);
        field.requestFocus();

        Runnable restore = () ->
                commentsList.getChildren().set(commentsList.getChildren().indexOf(editor), row);

        Runnable saveChanges = () -> {
            try {
                eventsAnnounceUseCase.editComment(comment, field.getText());
                content.setText(comment.getContent());
                restore.run();
            } catch (RuntimeException e) {
                warn(e.getMessage());
            }
        };

        cancel.setOnAction(e -> restore.run());
        save.setOnAction(e -> saveChanges.run());
        field.setOnAction(e -> saveChanges.run());
    }

    private void deleteComment(HBox row, AnnouncementComment comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Eliminar este comentario?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                eventsAnnounceUseCase.deleteComment(announcement, comment);
                commentsList.getChildren().remove(row);
                updateCommentsButton();
            } catch (RuntimeException e) {
                warn(e.getMessage());
            }
        }
    }

    @FXML
    private void onAddComment() {
        String text = txtNewComment.getText();
        if (text == null || text.isBlank()) return;

        try {
            AnnouncementComment comment = eventsAnnounceUseCase.addComment(announcement, text);
            appendCommentRow(comment);
            updateCommentsButton();
            txtNewComment.clear();
        } catch (RuntimeException e) {
            warn(e.getMessage());
        }
    }

    @FXML
    private void editarAnuncio() {
        if (onEdit != null) onEdit.accept(announcement);
    }

    @FXML
    private void eliminarAnuncio() {
        if (onDelete != null) onDelete.accept(announcement);
    }

    private void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String formatRelative(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return "Hace unos segundos";

        long minutes = seconds / 60;
        if (minutes < 60) return "Hace " + minutes + (minutes == 1 ? " minuto" : " minutos");

        long hours = minutes / 60;
        if (hours < 24) return "Hace " + hours + (hours == 1 ? " hora" : " horas");

        long days = hours / 24;
        if (days < 30) return "Hace " + days + (days == 1 ? " día" : " días");

        return dateTime.format(FULL_DATE);
    }
}
