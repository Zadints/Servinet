package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.utils.GetHadware;

public class StartUseCase {

    public static boolean checkAndConfigureDatabase(){
        return true;
    }

    public static boolean checkSessionActive() {
        return SessionUseCase.automaticUserLogin(GetHadware.id());
    }

    public static boolean checkAndCreateBasicRol() {
        return true;
    }

    public static boolean checkAndCreateBasicUser() {
        return true;
    }
    public static boolean loadAllConfigApp() {
        //aqui agregar para cargar todas las antenas, etc etc.
        return true;
    }
}
