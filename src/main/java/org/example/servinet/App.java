package org.example.servinet;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.servinet.bootstrap.compositionroot.AppCompositionRoot;
import org.example.servinet.bootstrap.ControllerFactory;
import org.example.servinet.bootstrap.StartBootStrap;
import org.example.servinet.bootstrap.compositionroot.BootstrapCompositionRoot;
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
        for (String s: fonts){
            Font.loadFont(getClass().getResourceAsStream(s), 14);
        }

        BootstrapCompositionRoot bootstrapRoot = new BootstrapCompositionRoot();
        StartBootStrap startBootStrap = new StartBootStrap(bootstrapRoot.getDatabaseSetupService());
        startBootStrap.startBootStrap(stage);

        AppCompositionRoot root = new AppCompositionRoot(
                bootstrapRoot.getAppExecutor(),
                bootstrapRoot.getDatabaseSetupService(),
                startBootStrap.getDatabaseFactory(),
                startBootStrap.getAppCacheFactory()
        );


        AnimationFactory animationFactory = new AnimationFactory();

        ControllerFactory factory = new ControllerFactory(root, animationFactory);


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