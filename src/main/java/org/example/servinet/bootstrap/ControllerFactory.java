package org.example.servinet.bootstrap;
import javafx.util.Callback;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.LoginController;
import org.example.servinet.ui.controllers.center.AdministrationController;
import org.example.servinet.ui.controllers.center.DashboardController;
import org.example.servinet.ui.controllers.components.UserRowController;
import org.example.servinet.ui.navigator.Navigator;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppCompositionRoot app;
    private Navigator navigator;

    public ControllerFactory(AppCompositionRoot appCompositionRoot) {
        this.app = appCompositionRoot;
    }

    @Override
    public Object call(Class<?> type) {


        if (type == LoginController.class){
            return new LoginController(
                app.getAppExecutor(),
                app.getLoginStaffUseCase(),
                navigator

            );
        }
        if (type == IndexController.class){
            return new IndexController(
                    app.getAppExecutor(),
                    navigator,
                    app.getGetStaffUseCase(),
                    app.getAuthorizationSecurity()

            );
        }
        if (type == DashboardController.class){
            return new DashboardController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity()

            );
        }

        if (type == UserRowController.class){
            return new UserRowController(
                    app.getGetStaffUseCase(),
                    app.getAuthorizationSecurity()

            );
        }

        if (type == AdministrationController.class){
            return new AdministrationController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetStaffUseCase(),
                    app.getDeleteStaffUseCase(),
                    app.getGetRoleUseCase(),
                    app.getGetLogUseCase()

            );
        }



        throw new IllegalArgumentException("Controlador desconocido" + type);
    }

    public void setNavigator(Navigator navigator){
        this.navigator =navigator;
    }
}
