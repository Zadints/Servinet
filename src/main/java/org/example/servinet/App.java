package org.example.servinet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.core.application.dto.DniDataDto;
import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.usecase.ConsultDniUseCase;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.application.usecase.StartUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.ApiException;
import org.example.servinet.infrastructure.api.DniApiClient;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
    private static void createAndLoadRoles(){
        //crea rol base mijines para que funcione la app (OWNER)
        Set<Permission> p = new HashSet<Permission>();
        p.add(Permission.BYPASS);

        RolesUseCase.createRol(new RoleDto(
             p,"#22A5F1", "Dueño"
        ));
    }

    private static void createAndLoadUserOne(Role rol){
        //crea el usuario base
        Path path = Path.of("D:/inglés/foto.jpg");

        SessionUseCase.registerUser(new UserDto(
                "Augusto",
                "Cesar2014abc.",
                rol,
                "tester@gmail.com",
                path
        ));

    }

    public static void main(String[] args) {
        loadConfig();
        LoadDb.startConnection();
        //crear db
        try {
           //crear roles
           createAndLoadRoles();
           //crear usuario
           createAndLoadUserOne(RolesUseCase.getRol("Dueño"));
        } catch (Exception e){
        }
        //iniciar app.
        if (!StartUseCase.checkSessionActive()){
            pathFxml = "login.fxml";
        } else {
            pathFxml = "index.fxml";
        }

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
//Admin
//Cesar2014abc.