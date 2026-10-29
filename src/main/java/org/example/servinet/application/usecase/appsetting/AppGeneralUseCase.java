package org.example.servinet.application.usecase.appsetting;

import org.example.servinet.application.port.out.InMemoryAppRepository;
import org.example.servinet.application.port.out.sql.AppRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.AppGeneral;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

public class AppGeneralUseCase {

    private final InMemoryAppRepository inMemoryAppRepository;
    private final AppRepository sqlAppRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ActivityLogsService activityLogsService;

    public AppGeneralUseCase(InMemoryAppRepository inMemoryAppRepository, AppRepository sqlAppRepository, AuthorizationSecurity authorizationSecurity, ActivityLogsService activityLogsService) {
        this.inMemoryAppRepository = inMemoryAppRepository;
        this.sqlAppRepository = sqlAppRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.activityLogsService = activityLogsService;
    }

    public void appRename(String newName){

        if (!authorizationSecurity.hasPermission(Permission.APP_CHANGE_NAME)){
            throw new PermissionException("No tienes permiso para cambiar el nombre de la app");
        }

        if (newName.length() > 12){
            throw new IllegalArgumentException("El nombre ingresado excede los 12 caracteres");
        }
        inMemoryAppRepository.updateName(newName);
        sqlAppRepository.renameApp(newName);
        activityLogsService.addLog(LogType.APP_RENAME, "Nuevo nombre: " + newName);
    }

    public void appChangueImage(byte[] image){

        if (!authorizationSecurity.hasPermission(Permission.APP_CHANGE_LOGO)){
            throw new PermissionException("No tienes permiso para cambiar el logo de la app");
        }

        if (image == null){
            throw new IllegalArgumentException("La imagen ingresada no es válida");
        }
        inMemoryAppRepository.updateImage(image);
        sqlAppRepository.changueImage(image);
        activityLogsService.addLog(LogType.APP_CHANGUEIMAGE, "Nueva imagen para la app");
    }



    public AppGeneral getData() {

        AppGeneral app = inMemoryAppRepository.get();
        if (app != null){
            return app;
        }

        app = sqlAppRepository.get();

        if (app == null || app.getAppName() == null){
            app = new AppGeneral("Servinet", null);
        }

        inMemoryAppRepository.set(app);

        return app;
    }
}
