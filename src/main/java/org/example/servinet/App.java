package org.example.servinet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.bootstrap.AppCompositionRoot;
import org.example.servinet.bootstrap.ControllerFactory;
import org.example.servinet.ui.animations.AnimationFactory;
import org.example.servinet.ui.navigator.Navigator;

import java.io.IOException;

public class App extends Application {

    private static String[] fonts = {
            "/fonts/Poppins-Regular.ttf",
            "/fonts/Poppins-Light.ttf",
            "/fonts/Poppins-Bold.ttf",
            "/fonts/Poppins-Black.ttf",
            "/fonts/PixelifySans-Regular.ttf"
    };

    @Override
    public void start(Stage stage) throws IOException {


        AppCompositionRoot root = new AppCompositionRoot();
        AnimationFactory animationFactory = new AnimationFactory();

        ControllerFactory factory = new ControllerFactory(root, animationFactory);

        for (String s: fonts){
            Font.loadFont(getClass().getResourceAsStream(s), 14);
        }
        Navigator navigator = new Navigator(stage, factory);

        factory.setNavigator(navigator);
        navigator.navigateToPage("/org/example/servinet/login.fxml");

    }

    public static void main(String[] args) {

        launch();
    }
}
//tester@gmail.com
//Cesar2014abc.
//Cesar2026pee.

//FabianF
//Fabug#09