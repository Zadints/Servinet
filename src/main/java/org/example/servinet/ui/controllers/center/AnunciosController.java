package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AnnuncioCardController;

import org.example.servinet.application.security.PermissionPolicy;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AnunciosController {

    private static final String ALL_PRIORITIES = "Todas";

    @FXML
    private VBox anunciosContainer;

    @FXML
    private Button btnCreateAnnounce;

    @FXML
    private ComboBox<String> cbxPriorityFilter;

    private IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }


    @FXML
    public void initialize() {

        // Obtener permisos del usuario.
        PermissionPolicy policy = PermissionPolicy.current();

        boolean puedeVer = policy.allows(
                Permission.ANNOUNCE_VIEW
        );

        boolean puedeCrear = policy.allows(
                Permission.ANNOUNCE_CREATE
        );

        // Mostrar únicamente controles autorizados.
        mostrar(btnCreateAnnounce, puedeCrear);
        mostrar(cbxPriorityFilter, puedeVer);

        if (puedeVer) {

            // Configurar filtros de prioridad.
            cbxPriorityFilter.getItems().clear();

            cbxPriorityFilter.getItems().add(
                    ALL_PRIORITIES
            );

            cbxPriorityFilter.getItems().addAll(
                    AnnouncementsUseCase.PRIORITIES
            );

            cbxPriorityFilter.setValue(ALL_PRIORITIES);

            cbxPriorityFilter.valueProperty().addListener(
                    (obs, anterior, nuevo) -> renderFeed()
            );

            // Cargar anuncios autorizados.
            loadFeed();

        } else {

            anunciosContainer.getChildren().clear();

            Label mensaje = new Label(
                    puedeCrear
                            ? "Puedes crear anuncios, pero tu rol no tiene permiso para consultar publicaciones."
                            : "Tu rol no tiene permiso para consultar anuncios."
            );

            mensaje.setWrapText(true);
            mensaje.getStyleClass().add("tertiary-text");

            anunciosContainer.getChildren().add(mensaje);
        }
    }

    private void mostrar(
            javafx.scene.Node componente,
            boolean permitido
    ) {

        componente.setVisible(permitido);
        componente.setManaged(permitido);
    }


    private void loadFeed() {

        try {
            AnnouncementsUseCase.loadAnnouncements();
        } catch (RuntimeException e) {
            anunciosContainer.getChildren().clear();
            showMessage(Alert.AlertType.ERROR, "No se pudieron cargar los anuncios: " + e.getMessage());
            return;
        }

        renderFeed();
    }

    private void renderFeed() {

        anunciosContainer.getChildren().clear();

        String filter = cbxPriorityFilter.getValue();
        boolean showAll = filter == null || filter.equals(ALL_PRIORITIES);

        List<Announcement> shown = AnnouncementsUseCase.getAllAnnouncements().stream()
                .filter(a -> showAll || a.getPriority().equals(filter))
                .toList();

        if (shown.isEmpty()) {
            Label empty = new Label(showAll
                    ? "Todavía no hay anuncios publicados."
                    : "No hay anuncios con prioridad " + filter.toLowerCase() + ".");
            empty.getStyleClass().add("tertiary-text");
            anunciosContainer.getChildren().add(empty);
            return;
        }

        for (Announcement announcement : shown) {
            addCard(announcement);
        }
    }

    private void addCard(Announcement announcement) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/anuncio-card.fxml")
            );

            Parent card = loader.load();

            AnnuncioCardController controller = loader.getController();
            controller.setAnnouncement(announcement, this::editAnnounce, this::deleteAnnounce);

            anunciosContainer.getChildren().add(card);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void createAnnounce() {

        if (!AuthorizationService.hasPermission(
                Permission.ANNOUNCE_CREATE)) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para crear anuncios."
            );
            return;
        }

        if (indexController == null) {
            return;
        }

        indexController.abrirModalAnnounce(
                this::loadFeed,
                null
        );
    }



    private void editAnnounce(Announcement announcement) {

        if (announcement == null ||
                !AnnouncementsUseCase.canEdit(announcement)) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para editar este anuncio."
            );
            return;
        }

        if (indexController != null) {

            indexController.abrirModalAnnounce(
                    this::loadFeed,
                    announcement
            );
        }
    }


    private void deleteAnnounce(Announcement announcement) {
        if (announcement == null ||
                !AnnouncementsUseCase.canDelete(announcement)) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para eliminar este anuncio."
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres eliminar el anuncio \"" + announcement.getTitle() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AnnouncementsUseCase.deleteAnnouncement(announcement);
                loadFeed();
            } catch (RuntimeException e) {
                showMessage(Alert.AlertType.WARNING, e.getMessage());
            }
        }
    }

    private void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
