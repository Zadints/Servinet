package org.example.servinet.bootstrap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqlServerDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqliteDatabaseFactory;
import org.example.servinet.infrastructure.dto.DatabaseConfig;
import org.example.servinet.ui.controllers.setup.DatabaseSetupController;

import java.io.IOException;
import java.nio.file.Path;

public class StartBootStrap {

    private DatabaseFactory databaseFactory;
    private final DatabaseSetupService databaseSetupService;

    public StartBootStrap(DatabaseSetupService databaseSetupService) {
        this.databaseSetupService = databaseSetupService;
    }

    public void startBootstrap(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/org/example/servinet/database-setup.fxml"
                )
        );

        Parent parent = loader.load();

        DatabaseSetupController controller = loader.getController();

        controller.setSaveHandler(setup -> {
            setDatabaseFactory(setup, controller);
        });

        stage.setScene(new Scene(parent));
        stage.show();
    }

    private void setDatabaseFactory(DatabaseSetup setup, DatabaseSetupController controller){
        switch (setup.mode()){
            case DeploymentMode.MULTI_DEVICE ->{
                try {
                    DatabaseConfig dto = databaseSetupService.saveSqlServerDatabase(setup);
                    this.databaseFactory = new SqlServerDatabaseFactory(dto);
                }catch (Exception e){
                    controller.showStatus(e.getMessage());
                }
            }
            case DeploymentMode.SINGLE_DEVICE ->{
                try {
                    Path path = databaseSetupService.saveSqliteDatabase(setup);
                    this.databaseFactory = new SqliteDatabaseFactory(path);
                }catch (Exception e){
                    controller.showStatus(e.getMessage());
                }
            }
            default -> {
                System.out.println("No se seleccionó una base de datos");
                throw new DatabaseException("No se seleccionó una base de datos");
            }
        }
    }
}
