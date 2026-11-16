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
import org.example.servinet.application.port.out.sql.SqlCacheManagerRepository;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.infrastructure.database.deploy.ServinetStoragePaths;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqlServerDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.factory.SqliteDatabaseFactory;
import org.example.servinet.infrastructure.database.deploy.scripts.DatabaseScriptExecutor;
import org.example.servinet.infrastructure.database.deploy.scripts.SqlServerScriptExecutor;
import org.example.servinet.infrastructure.database.deploy.scripts.SqliteScriptExecutor;
import org.example.servinet.infrastructure.database.deploy.test.JdbcConnectionTester;
import org.example.servinet.infrastructure.database.sqlite.SqlCacheManager;
import org.example.servinet.infrastructure.dto.DatabaseConfig;
import org.example.servinet.ui.controllers.setup.DatabaseSetupController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Esta clase solo carga las bases de datos:
 * - valida su existencia
 * - dependiendo de eso recupera o crea las db junto con sus 2 DatabaseFactory
 * - retorna mediante sus getters a los 2 DatabaseFactory
 **/

public class StartBootStrap {

    private DatabaseFactory databaseUserFactory;
    private DatabaseFactory appCacheFactory;
    private final DatabaseSetupService databaseSetupService;
    private final ServinetStoragePaths servinetStoragePaths;

    public StartBootStrap(DatabaseSetupService databaseSetupService) {
        this.databaseSetupService = databaseSetupService;
        this.servinetStoragePaths = new ServinetStoragePaths();
    }

    public void startBootStrap(Stage stage){

        System.out.println("Inciiando validaciones");
        try {
            servinetStoragePaths.createDirectories();
            this.appCacheFactory = new SqliteDatabaseFactory(servinetStoragePaths.getCacheDatabaseFile());

            if (!servinetStoragePaths.userDatabaseExists()){
                System.out.println("No existe db user, creandolo");
                renderConfigDatabases(stage);
                return;
            }
            System.out.println("Si existe la db users, pasar a ver como leerla");
            //aqui va ´para sacar del cache el tipo de
            // db que se usará + cargar el databaseFactory
            SqlCacheManagerRepository sqlCache= new SqlCacheManager(appCacheFactory);
            Optional<DeploymentMode> optionalMode = sqlCache.getDeployModeType();
            System.out.println("Obtiene datos de lcache");
            if (optionalMode.isEmpty()){
                System.out.println("No hay datos en cache, o la tabla no existe");
                renderConfigDatabases(stage);
                return;
            }
            System.out.println("Obteniendo la forma de despliegue para leer db de usaser");
            DeploymentMode mode = optionalMode.get();

            switch (mode) {

                case MULTI_DEVICE ->{
                    Optional<DatabaseConfig> optinalCredent = sqlCache.getDatabaseConfig();

                    if (optinalCredent.isEmpty()){
                        renderConfigDatabases(stage);
                        return;
                    }
                    this.databaseUserFactory = new SqlServerDatabaseFactory(optinalCredent.get());
                }


                case SINGLE_DEVICE ->
                    this.databaseUserFactory = new SqliteDatabaseFactory(
                            servinetStoragePaths.getUserDatabaseFile());

            };


            System.out.println("terminò de iniciar");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
            return;
        }
        /*
        * luego arreglar para que cree su cuenta inicial el usuario mediante el login y nd más
        * */

    }
    private boolean createOrLoadCache(DatabaseSetupController controller){
        controller.showStatus("Creando el cache de la aplicación ");
        if (!databaseSetupService.isValidConnection(
                new JdbcConnectionTester(appCacheFactory))){
            return false;
        }
        controller.showStatus("Creando la estructura interna de el caché");
        DatabaseScriptExecutor cacheBuild = new SqliteScriptExecutor(appCacheFactory);
        try {
            cacheBuild.executeScript("/databaseschema/03_cache_database.sql");
        }catch (IOException | SQLException e){
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
            controller.showStatus(e.getMessage());
            return false;
        }
        controller.showStatus("Creación de caché finalizada");
        return true;
    }



    private boolean createOrLoadDatabaseUser(DatabaseSetup setup, DatabaseSetupController controller){
        try {
            DatabaseFactory candidate = null;
            DatabaseScriptExecutor cacheBuild = null;
            String[] paths = new String[0];

            System.out.println("Iniciando configuracion de db user");
            switch (setup.mode()) {

                case MULTI_DEVICE ->{
                    System.out.println("Es multi");
                    candidate =  new SqlServerDatabaseFactory(
                                databaseSetupService.toDatabaseConfig(setup)
                        );
                    cacheBuild = new SqlServerScriptExecutor(candidate);
                    paths = new String[]{
                            "/databaseschema/01_tablas_server.sql",
                            "/databaseschema/02_procedimientos_server.sql"
                    };
                }

                case SINGLE_DEVICE ->{
                    System.out.println("Estás solo y morirás solo");
                    candidate =  new SqliteDatabaseFactory(servinetStoragePaths.getUserDatabaseFile());

                    cacheBuild = new SqliteScriptExecutor(candidate);
                    paths = new String[]{
                            "/databaseschema/01_tablas_sqlite.sql"
                    };
                }
            };
            System.out.println("Validating connection...");
            controller.showStatus("Validating connection...");
            if (!databaseSetupService.isValidConnection(
                    new JdbcConnectionTester(candidate))) {

                controller.showStatus("Invalid connection database-user");
                return false;
            }
            System.out.println("aplicando al for");
            this.databaseUserFactory = candidate;

            for (String path: paths){
                cacheBuild.executeScript(path);
            }

            System.out.println("Success connection");
            controller.showStatus("Success connection");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage()+e.getCause());
            controller.showStatus(e.getMessage());
            return false;
        }
    }


    private boolean setDatabaseFactory(DatabaseSetup setup, DatabaseSetupController controller){
        System.out.println("Starting installatión ");
        controller.showStatus("Starting installatión ...");
        System.out.println("Starting cache loader  ");
        boolean status = createOrLoadCache(controller);
        if (!status) return false;
        System.out.println("validando o cargando db user");
        boolean status2 =  createOrLoadDatabaseUser(setup,controller);
        if (!status2) return false;
        System.out.println("todo cargado");
        //aqui guardar el tipo de databaseSetup en cache
        //aqui guardar conexion en cache credenciales en
        // cache dependiend tip ademas del tipo

        return true;
    }

    private void renderConfigDatabases(Stage stage) throws IOException {

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
                System.out.println("retrnó true cerrando");
                modal.close();
            }
        });

        controller.setCancelHandler(setup -> {
            modal.close();
            Platform.exit();
        });

        modal.showAndWait();
    }

    public DatabaseFactory getDatabaseUserFactory() {
        return databaseUserFactory;
    }

    public DatabaseFactory getAppCacheFactory() {
        return appCacheFactory;
    }

}
