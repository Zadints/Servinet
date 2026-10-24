package org.example.servinet.bootstrap;
import javafx.util.Callback;
import net.bytebuddy.implementation.bytecode.Throw;
import org.example.servinet.ui.navigator.Navigator;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppCompositionRoot appCompositionRoot;
    private Navigator navigator;

    public ControllerFactory(AppCompositionRoot appCompositionRoot) {
        this.appCompositionRoot = appCompositionRoot;
    }

    @Override
    public Object call(Class<?> type) {


        throw new IllegalArgumentException("Controlador desconocido" + type);
    }

    public void setNavigator(Navigator navigator){
        this.navigator =navigator;
    }
}
