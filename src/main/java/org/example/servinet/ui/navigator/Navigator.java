package org.example.servinet.ui.navigator;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.example.servinet.application.dto.navigate.NavigateCenterDto;
import org.example.servinet.application.dto.navigate.NavigateOptionsDto;
import org.example.servinet.ui.controllers.ErrorPageController;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Navigator {


    private final Stage stage;
    private final Callback<Class<?>, Object> controllerFactory;

    private final Deque<NavigateOptionsDto> historyPage = new ArrayDeque<>();
    private NavigateOptionsDto currentPage;

    private final Deque<NavigateCenterDto> historyCenter = new ArrayDeque<>();
    private NavigateCenterDto currentCenter;

    public Navigator(Stage stage, Callback<Class<?>, Object> controllerFactory ) {
        this.stage = stage;
        this.controllerFactory = controllerFactory;
        this.stage.initStyle(StageStyle.TRANSPARENT);
    }


    public void navigate(NavigateOptionsDto dto) throws IOException {
        loadPage(dto);
    }

    public void navigateToCenter(BorderPane container, String fileName) throws IOException {
        navigateToCenter(container, fileName, true);
    }

    private void navigateToCenter(BorderPane container, String fileName, boolean saveHistory) throws IOException {

        if (saveHistory && currentCenter != null) {
            historyCenter.push(currentCenter);
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        fileName
                )
        );

        loader.setControllerFactory(controllerFactory);

        Parent view = loader.load();
        container.setCenter(view);

        currentCenter = new NavigateCenterDto(container, fileName);

    }

    public void navigateToPage(String fxmlPath, double width, double height) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");
        if (currentPage != null) {
            historyPage.push(currentPage);
        }
        var dto = new NavigateOptionsDto(
                fxmlPath,
                width,
                height,
                false,
                null,
                null,
                null,
                Color.TRANSPARENT,
                false,
                false
        );

        loadPage(dto);
        currentPage = dto;
    }
    public void navigateToPage(String fxmlPath) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");

        if (currentPage != null) {
            historyPage.push(currentPage);
        }

        var dto = new NavigateOptionsDto(
                fxmlPath,
                0.90,
                0.95,
                false,
                null,
                null,
                null,
                Color.TRANSPARENT,
                false,
                false
        );

        loadPage(dto);
        currentPage = dto;
    }

    public void awaitOpenWindow(String fxmlPath, Stage parentStage, double width, double height, boolean isMaximized) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");

        loadPage(new NavigateOptionsDto(
                fxmlPath,
                width,
                height,
                true,
                parentStage,
                Modality.WINDOW_MODAL,
                StageStyle.TRANSPARENT,
                Color.TRANSPARENT,
                isMaximized,
                true
        ));
    }
    public void awaitOpenWindow(String fxmlPath, Stage parentStage, double width, double height) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");

        loadPage(new NavigateOptionsDto(
                fxmlPath,
                width,
                height,
                true,
                parentStage,
                Modality.WINDOW_MODAL,
                StageStyle.TRANSPARENT,
                Color.TRANSPARENT,
                false,
                true
        ));
    }
    public void openWindow(String fxmlPath, Stage parentStage, double width, double height , boolean isMaximized) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");

        loadPage(new NavigateOptionsDto(
                fxmlPath,
                width,
                height,
                true,
                parentStage,
                Modality.WINDOW_MODAL,
                StageStyle.TRANSPARENT,
                Color.TRANSPARENT,
                isMaximized,
                false
        ));
    }

    public void openWindow(String fxmlPath, Stage parentStage, double width, double height) throws IOException{

        if (fxmlPath.isEmpty()) throw new IOException("Path Fxml is Empty");

        loadPage(new NavigateOptionsDto(
                fxmlPath,
                width,
                height,
                true,
                parentStage,
                Modality.WINDOW_MODAL,
                StageStyle.TRANSPARENT,
                Color.TRANSPARENT,
                false,
                false
        ));
    }


    public void showError(
            String message,
            String title,
            Stage parentStage
    ) throws IOException {

        NavigateOptionsDto dto = new NavigateOptionsDto(
                "errorPage.fxml",
                0.30,
                0.30,
                true,
                parentStage,
                Modality.WINDOW_MODAL,
                StageStyle.TRANSPARENT,
                Color.TRANSPARENT,
                false,
                true
        );

        ErrorPageController controller =
                (ErrorPageController) loadPage(dto);

        controller.setException(message, title);
    }

    private Object loadPage(NavigateOptionsDto dto) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        dto.fxmlName()
                )
        );

        loader.setControllerFactory(controllerFactory);

        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.setFill(dto.sceneSetFill());

        scene.getStylesheets().addAll(
                getClass().getResource("/styles/index.css").toExternalForm(),
                getClass().getResource("/styles/center-styles.css").toExternalForm(),
                getClass().getResource("/styles/exception.css").toExternalForm(),
                getClass().getResource("/styles/splash.css").toExternalForm()
        );

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        double width = screen.getWidth() * dto.widthRatio();
        double height = screen.getHeight() * dto.heightRatio();

        Stage targetStage;

        if (dto.newWindow()) {
            targetStage = new Stage();

            if (dto.parentStage() != null) {
                targetStage.initOwner(dto.parentStage());
            }

            if (dto.stageInitModality() != null) {
                targetStage.initModality(dto.stageInitModality());
            }

            if (dto.stageInitStyle() != null) {
                targetStage.initStyle(dto.stageInitStyle());
            }
        } else {
            targetStage = stage;
        }

        targetStage.setWidth(width);
        targetStage.setHeight(height);
        targetStage.setMaximized(dto.stageSetMaximized());
        targetStage.setScene(scene);
        targetStage.centerOnScreen();

        if (dto.stageShowAndAwait()) {
            targetStage.showAndWait();
        } else {
            targetStage.show();
        }

        return loader.getController();
    }


    public void backPage() throws IOException {
        if (historyPage.isEmpty()) {
            return;
        }

        NavigateOptionsDto previousPage = historyPage.pop();

        loadPage(previousPage);
        currentPage = previousPage;
    }


    public void backCenter() throws IOException {
        if (historyCenter.isEmpty()) {
            return;
        }

        NavigateCenterDto previousCenter = historyCenter.pop();

        navigateToCenter(
                previousCenter.container(),
                previousCenter.fileName(),
                false
        );
    }
}
