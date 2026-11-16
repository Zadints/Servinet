package org.example.servinet.bootstrap;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.servinet.application.dto.database.DatabaseSetup;
import org.example.servinet.application.enums.DeploymentMode;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.ServinetStoragePaths;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqlServerDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqliteDatabaseFactory;
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

    public void startBootStrap(Stage stage){
        /*Chicos acuerdense primero se crea el caché
         automaticamentesi no existe o se valida coenexion
         */
        try {
            System.out.println("Cargadon esto..");
            servinetStoragePaths.createDirectories();
            System.out.println("Cargadon esto.. 2");
            this.appCacheFactory = new SqliteDatabaseFactory(servinetStoragePaths.getCacheDatabaseFile());
            System.out.println("Cargadon esto.. 3");
            if (!databaseSetupService.isValidConnection(new JdbcConnectionTester(appCacheFactory))){
                System.out.println("No se pudo realizar la coenxion");
                return;
            }
            System.out.println("Cargadon esto.. 4");

            if (!servinetStoragePaths.userDatabaseExists()){
                renderConfigDatabases(stage);
            }
            System.out.println("terminò");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
        }
        /*
        * de aqui parte la creacion de estructustura interna de la db (tablas)
        * luego arreglar para que cree su cuenta inicial el usuario mediante el login y nd más
        * */



    }

    public void renderConfigDatabases(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/org/example/servinet/setup/config-setupt-app.fxml"
                )
        );

        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        scene.getStylesheets().add(
                getClass().getResource("/styles/setup.css").toExternalForm()
        );
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        double width = screen.getWidth() * 0.7;
        double height = screen.getHeight() * 0.8;


        DatabaseSetupController controller = loader.getController();

        Stage modal = new Stage();
        modal.setWidth(width);
        modal.setHeight(height);
        modal.initStyle(StageStyle.TRANSPARENT);
        modal.initOwner(stage);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Configuración de Servinet");
        modal.setScene(scene);
        modal.setResizable(true);
        modal.centerOnScreen();

        controller.setSaveHandler(setup -> {

            if (setDatabaseFactory(setup, controller)) {
                modal.close();
            }
        });

        controller.setCancelHandler(setup -> {
            modal.close();
            Platform.exit();
        });

        modal.showAndWait();
    }




    private boolean setDatabaseFactory(DatabaseSetup setup, DatabaseSetupController controller){
        controller.showStatus("Starting ...");
        try {
            controller.showStatus("Verify device...");
            DatabaseFactory candidate = switch (setup.mode()) {

                case MULTI_DEVICE ->
                        new SqlServerDatabaseFactory(
                                databaseSetupService.toDatabaseConfig(setup)
                        );

                case SINGLE_DEVICE ->
                        new SqliteDatabaseFactory(
                                servinetStoragePaths.getUserDatabaseFile(
                        ));
            };
            controller.showStatus("Validating connection...");
            if (!databaseSetupService.isValidConnection(
                    new JdbcConnectionTester(candidate))) {

                controller.showStatus("Invalid connection database-user");
                return false;
            }
            this.databaseFactory = candidate;

            controller.showStatus("Success connection");
            return true;
        } catch (Exception e) {
            controller.showStatus(e.getMessage());
            return false;
        }
    }



    public DatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public DatabaseFactory getAppCacheFactory() {
        return appCacheFactory;
    }

}
