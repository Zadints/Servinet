package org.example.servinet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.application.usecase.StartupUseCase;
import org.example.servinet.core.domain.enums.Role;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.io.IOException;
import java.nio.file.Path;

import static org.example.servinet.infrastructure.database.config.ConfigLoad.loadConfig;

public class App extends Application {
    private static String pathFxml;
    @Override
    public void start(Stage stage) throws IOException {

        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"),14
        );
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Poppins-Light.ttf"),14
        );

        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"),14
        );
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Poppins-Black.ttf"),14
        );
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/PixelifySans-Regular.ttf"),14
        );
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(pathFxml));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().addAll(
                getClass().getResource("/styles/index.css").toExternalForm(),
                getClass().getResource("/styles/center-styles.css").toExternalForm(),
                getClass().getResource("/styles/exception.css").toExternalForm()
        );
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        stage.setWidth(1300);
        stage.setHeight(700);
        stage.setMaximized(false);
        stage.setTitle("Servinet ");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        loadConfig();
        LoadDb.startConnection();

        if (!StartupUseCase.checkAutomaticLogin()){
            pathFxml = "login.fxml";
        } else {
            pathFxml = "index.fxml";
        }

        launch();
    }
}
//Admin
//Cesar2014abc.