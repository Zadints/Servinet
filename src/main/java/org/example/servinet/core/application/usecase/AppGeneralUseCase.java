package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.entities.AppGeneral;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.models.AppGeneralModel;

public class AppGeneralUseCase {
    private static AppGeneral appGeneral = new AppGeneral();

    public static void loadAppConfig(){
        appGeneral.setAppName(AppGeneralModel.getAppGeneralDatabase());
    }
    public static boolean appRename(String newName, String password){

        if (newName == null ||  password == null || newName.isBlank() || password.isBlank()){
            throw new InvalidValueException("Debes rellenar todos los campos para poder cambiar de nombre");
        }
        if (!SessionUseCase.isEqualsPasswordUser(password)){
            return false;
        }
        if (newName.length() > 12){
            return false;
        }
        appGeneral.setAppName(newName);
        AppGeneralModel.updateAppDatabase(newName);
        LogsUseCase.addLog(LogType.APP_RENAME, "Nuevo nombre: " + newName);
        return true;
    }


    public static String getAppName() {
        return appGeneral.getAppName();
    }
}
