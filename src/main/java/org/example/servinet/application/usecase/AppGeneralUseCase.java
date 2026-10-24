package org.example.servinet.application.usecase;

import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.AppGeneral;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.infrastructure.database.sqlserver.AppGeneralModel;

public class AppGeneralUseCase {
    private static AppGeneral appGeneral = new AppGeneral();

    public static void loadAppConfig(){
        appGeneral.setAppName(AppGeneralModel.getAppGeneralDatabase());
    }
    public static boolean appRename(String newName, String password){

        if (newName == null ||  password == null || newName.isBlank() || password.isBlank()){
            throw new InvalidValueException("Debes rellenar todos los campos para poder cambiar de nombre");
        }
        if (!LoginStaffUseCase.isEqualsPasswordUser(password)){
            return false;
        }
        if (newName.length() > 12){
            return false;
        }
        appGeneral.setAppName(newName);
        AppGeneralModel.updateAppDatabase(newName);
        ActivityLogsService.addLog(LogType.APP_RENAME, "Nuevo nombre: " + newName);
        return true;
    }


    public static String getAppName() {
        return appGeneral.getAppName();
    }
}
