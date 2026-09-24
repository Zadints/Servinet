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
import org.example.servinet.core.application.dto.DniDataDto;
import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.application.usecase.api.ConsultDniUseCase;
import org.example.servinet.core.application.usecase.api.WhatsAppUseCase;
import org.example.servinet.core.application.usecase.important.StartAppUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.ApiException;
import org.example.servinet.infrastructure.api.DniApiClient;
import org.example.servinet.infrastructure.api.ultraMsgClient.SendWhatsAppMessageUseCase;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.example.servinet.infrastructure.database.config.ConfigLoad.loadConfig;

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

        Platform.setImplicitExit(false);
        for (String i : fonts) {
            Font.loadFont(getClass().getResourceAsStream(i), 14);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("login.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().addAll(
                getClass().getResource("/styles/index.css").toExternalForm(),
                getClass().getResource("/styles/center-styles.css").toExternalForm(),
                getClass().getResource("/styles/exception.css").toExternalForm()
        );

        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        double width = screen.getWidth() * 0.98;
        double height = screen.getHeight() * 0.98;

        stage.setWidth(width);
        stage.setHeight(height);

        stage.setX(
                screen.getMinX() +
                        (screen.getWidth() - width) / 2
        );

        stage.setY(
                screen.getMinY() +
                        (screen.getHeight() - height) / 2
        );

        stage.setMinHeight(400);
        stage.setMinWidth(500);
        stage.setMaximized(false);
        stage.setTitle("Servinet");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {


        //StartAppUseCase.loadAllConfigApp();
        /*
        try {
            ConsultDniUseCase test = new ConsultDniUseCase(new DniApiClient());
            DniDataDto bto = test.execute("18126564");
            System.out.println(bto);
        } catch(ApiException e){
            System.out.println(e.getMessage());
        }


*/


        launch();
    }
}
//Augusto
//Cesar2014abc.
//Cesar2026pee.