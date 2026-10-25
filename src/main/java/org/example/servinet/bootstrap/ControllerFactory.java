package org.example.servinet.bootstrap;
import javafx.util.Callback;
import org.example.servinet.ui.controllers.LoginController;
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


        throw new IllegalArgumentException("Controlador desconocido" + type);
    }

    public void setNavigator(Navigator navigator){
        this.navigator =navigator;
    }
}
