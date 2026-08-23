package org.example.mineide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {
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
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("index.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().addAll(
                getClass().getResource("/styles/index.css").toExternalForm(),
                getClass().getResource("/styles/center-styles.css").toExternalForm()
        );
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        stage.setWidth(1300);
        stage.setHeight(700);
        stage.setMaximized(false);
        stage.setTitle("MineIDE ");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

//https://getnova.zip/