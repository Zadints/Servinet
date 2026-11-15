package org.example.servinet.bootstrap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.ServinetStoragePaths;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqlServerDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqliteDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.test.DatabaseConnectionTester;
import org.example.servinet.infrastructure.database.deploy.test.JdbcConnectionTester;
import org.example.servinet.ui.controllers.setup.DatabaseSetupController;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Esta clase solo carga las bases de datos:
 * - valida su existencia
 * - dependiendo de eso recupera o crea las db junto con sus 2 DatabaseFactory
 * - retorna mediante sus getters a los 2 DatabaseFactory
 **/

public class StartBootStrap {

    private DatabaseFactory databaseFactory;
    private DatabaseFactory appCacheFactory;
    private final DatabaseSetupService databaseSetupService;
    private final ServinetStoragePaths servinetStoragePaths;


    public StartBootStrap(DatabaseSetupService databaseSetupService) {
        this.databaseSetupService = databaseSetupService;
        this.servinetStoragePaths = new ServinetStoragePaths();
    }

    public void startBootStrap(Stage stage) throws IOException{
        /*Chicos acuerdense primero se crea el cache
         automaticamentesi no existe o se valida coenexion
         */
        servinetStoragePaths.createDirectories();
        this.appCacheFactory = new SqliteDatabaseFactory(servinetStoragePaths.getCacheDatabaseFile());

        if (!databaseSetupService.isValidConnection(new JdbcConnectionTester(appCacheFactory))){
            System.out.println("No se pudo realizar la coenxion");
            return;
        }


        if (!servinetStoragePaths.userDatabaseExists()){
            renderConfigDatabases(stage);
        }

        /*
        * de aqui parte la creacion de estructustura interna de la db (tablas)
        * luego arreglar para que cree su cuenta inicial el usuario mediante el login y nd más 
        * */



    }

    public void renderConfigDatabases(Stage stage) throws IOException {

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

        try {
            DatabaseFactory candidate = switch (setup.mode()) {

                case MULTI_DEVICE ->
                        new SqlServerDatabaseFactory(
                                databaseSetupService.toDatabaseConfig(setup)
                        );

                case SINGLE_DEVICE ->
                        new SqliteDatabaseFactory(
                                servinetStoragePaths.getDatabaseUser()
                        );
            };

            if (!databaseSetupService.isValidConnection(
                    new JdbcConnectionTester(candidate))) {

                controller.showStatus("Conexión inválida");
                return;
            }

            this.databaseFactory = candidate;

            controller.showStatus("Conexión establecida");

        } catch (Exception e) {
            controller.showStatus(e.getMessage());
        }


    }

    private void isValidConnection(DatabaseSetupController controller, DatabaseConnectionTester  dbTester){
        if (!databaseSetupService.isValidConnection(dbTester)){
            controller.showStatus("La conexión de bases de datos usuario no se puso realizar ¡no es válida!");
        }
    }



    public DatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public DatabaseFactory getAppCacheFactory() {
        return appCacheFactory;
    }

}
