package org.example.servinet.ui.controllers.center;

import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.usecase.announce.EventsAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.ui.animations.LoadingAnimation;
import org.example.servinet.ui.animations.SlideUpAnimation;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.components.AnnuncioCardController;

import org.example.servinet.ui.navigator.Navigator;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AnunciosController {

    private static final String ALL_PRIORITIES = "Todas";

    @FXML private VBox anunciosContainer;
    @FXML private Button btnCreateAnnounce;
    @FXML private ComboBox<String> cbxPriorityFilter;
    /**
     *  Atributos de animacion fxml
     **/
    @FXML private StackPane loadingOverlay;
    @FXML private Circle loadingCircle;
    @FXML private Label lblLoading;



    private IndexController indexController;
    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetAnnounceUseCase getAnnounceUseCase;
    private final EventsAnnounceUseCase eventsAnnounceUseCase;
    private final LoadingAnimation loadingAnimation;
    private final SlideUpAnimation slideUpAnimation;

    public AnunciosController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService, GetAnnounceUseCase getAnnounceUseCase, EventsAnnounceUseCase eventsAnnounceUseCase, LoadingAnimation loadingAnimation, SlideUpAnimation slideUpAnimation) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
        this.getAnnounceUseCase = getAnnounceUseCase;
        this.eventsAnnounceUseCase = eventsAnnounceUseCase;
        this.loadingAnimation = loadingAnimation;
        this.slideUpAnimation = slideUpAnimation;
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }


    @FXML
    public void initialize() {

        boolean puedeVer = authorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW
        );

        boolean puedeCrear = authorizationService.hasPermission(
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
                    getAnnounceUseCase.getPrioritiesMax()
            );

            cbxPriorityFilter.setValue(ALL_PRIORITIES);

            cbxPriorityFilter.valueProperty().addListener(
                    (obs, anterior, nuevo) -> loadAnnounces()
            );

            loadAnnounces();

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


    private void loadAnnounces() {

        loadingAnimation.onAnimation(loadingOverlay,loadingCircle,lblLoading);
        Task<List<Announcement>> task = new Task<List<Announcement>>() {
            @Override
            protected List<Announcement> call() throws Exception {

                return getAnnounceUseCase.getAnnouncements();
            }
        };

        task.setOnSucceeded( event -> {



            List<Announcement> announcements = task.getValue();

            anunciosContainer.getChildren().clear();

            String filter = cbxPriorityFilter.getValue();
            boolean showAll = filter == null || filter.equals(ALL_PRIORITIES);

            if (!showAll && !announcements.isEmpty()){
                announcements = announcements.stream()
                        .filter(a -> a.getPriority().equals(filter))
                        .toList();
            }
            loadingAnimation.finishAnimation();
            if (announcements.isEmpty()) {
                Label empty = new Label(showAll
                        ? "No hay anuncios publicados."
                        : "No hay anuncios con prioridad " + filter.toLowerCase() + ".");
                empty.getStyleClass().add("tertiary-text");
                anunciosContainer.getChildren().add(empty);
                return;
            }

            for (Announcement announcement : announcements) {
                addCard(announcement);
            }

        });

        appExecutor.execute(task);

    }

    private void addCard(Announcement announcement) {
        try {
            AnnuncioCardController controller = navigator.loadComponent(
                    "/org/example/servinet/components/anuncio-card.fxml",
                    anunciosContainer.getChildren()::add
            );

            controller.setAnnouncement(
                    announcement,
                    this::editAnnounce,
                    this::deleteAnnounce
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void createAnnounce() {

        if (indexController == null) {
            return;
        }

        indexController.abrirModalAnnounce(
                this::loadAnnounces,
                null
        );
    }



    private void editAnnounce(Announcement announcement) {

        /*
        if (announcement == null ||
                !editAnnounceUseCase.editAnnouncement(announcement) {

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
        }*/
    }


    private void deleteAnnounce(Announcement announcement) {
        if (announcement == null ||
                !eventsAnnounceUseCase.canDelete(announcement)) {

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
                eventsAnnounceUseCase.deleteAnnouncement(announcement);
                loadAnnounces();
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
