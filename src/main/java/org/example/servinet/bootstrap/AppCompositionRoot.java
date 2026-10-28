package org.example.servinet.bootstrap;

import org.example.servinet.application.port.out.*;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.usecase.GetLogUseCase;
import org.example.servinet.application.usecase.announce.CreateAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EditAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EventsAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.application.usecase.role.CreateRoleUseCase;
import org.example.servinet.application.usecase.role.DeleteRoleUseCase;
import org.example.servinet.application.usecase.role.GetRoleUseCase;
import org.example.servinet.application.usecase.role.UpdateRoleUseCase;
import org.example.servinet.application.usecase.staff.*;
import org.example.servinet.domain.entities.*;
import org.example.servinet.application.port.out.WhatsAppSendProvider;
import org.example.servinet.domain.repository.CacheRepository;
import org.example.servinet.infrastructure.api.ApisPeruAdapter;
import org.example.servinet.infrastructure.api.UltraMsgWhatsAppAdapter;
import org.example.servinet.infrastructure.cache.YamlCacheLoader;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.*;
import org.example.servinet.infrastructure.database.sqlserver.*;
import org.example.servinet.infrastructure.hardware.WindowsHardwareIdProvider;
import org.example.servinet.infrastructure.security.SecurePasswordGenerator;

import java.net.http.HttpClient;

public class AppCompositionRoot {
    private final AppExecutor appExecutor;

    private final LoginStaffUseCase loginStaffUseCase;
    private final DeleteStaffUseCase deleteStaffUseCase;
    private final EditStaffUseCase editStaffUseCase;
    private final GetStaffUseCase getStaffUseCase;
    private final RegisterStaffUseCase registerStaffUseCase;

    private final CreateAnnounceUseCase createAnnounceUseCase;
    private final EditAnnounceUseCase editAnnounceUseCase;
    private final EventsAnnounceUseCase eventsAnnounceUseCase;
    private final GetAnnounceUseCase getAnnounceUseCase;

    private final CreateRoleUseCase createRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;

    private final GetLogUseCase getLogUseCase;

    private final AuthorizationSecurity authorizationSecurity;
    public AppCompositionRoot() {
        this.appExecutor = new AppExecutor();

        YamlCacheLoader cacheLoader = new YamlCacheLoader();
        CacheRepository cache = cacheLoader.load("config/config.yml");
        DatabaseFactory dbFactory = new DatabaseFactory(cache.getDatabaseConfig());
        HttpClient httpClient = HttpClient.newHttpClient();

        /*------ API LOADER -------------*/
        WhatsAppSendProvider whatsAppSendProvider= new UltraMsgWhatsAppAdapter(
                httpClient, cache.getWhatsAppConfig());
        DniGetProvider dniGetProvider = new ApisPeruAdapter(httpClient, cache.getApiPeruConfig());
        /*------ LOAD MEMORY CACHE -------------*/
        InMemoryComplexRepository<Staff> staffCache = new InMemoryStaffAuthRepository();
        InMemoryRepository<LogEntry> logsCache = new InMemoryLogsRepository();
        InMemoryRepository<Role> roleCache = new InMemoryRolesRepository();
        InMemoryRepository<Announcement> announceCache = new InMemoryAnnounceRepository();
        InMemoryRepository<Antenna> antennaCache = new InMemoryAntennasRepository();
        HardwareIdProvider hardwareIdProvider = new WindowsHardwareIdProvider();

        /*------ LOAD SQL CLASS -------------*/
        StaffRepository sqlStaff = new SqlStaffAuthRepository(dbFactory);
        LogsRepository sqlLogs = new SqlLogsRepository(dbFactory);
        SqlAnnounceRepository sqlAnnounce = new SqlAnnouncementRepository(dbFactory);
        SqlRepository<Role> sqlRole = new SqlRoleRepository(dbFactory);
        SqlComplexRepository<Antenna> sqlAntenna = new SqlAntennaRepository(dbFactory);
        /*------ LOAD SERVICES -------------*/

        ActivityLogsService logsService = new ActivityLogsService(staffCache, logsCache, sqlLogs);
        StaffCacheService staffCacheService = new StaffCacheService(sqlStaff,staffCache);
        authorizationSecurity = new AuthorizationSecurity(staffCache,new PermissionPolicy());
        //AuthorizationService tendrà un metodo que se llamarà cuando el usuario iniciò ya sesion para asignar permisos.

        /*------ LOAD ADDS-------------*/

        SecurePasswordGenerator securePassword = new SecurePasswordGenerator();

        /*------ LOAD USECASE -------------*/

        loginStaffUseCase = new LoginStaffUseCase(
                hardwareIdProvider,sqlStaff, staffCache, logsService, staffCacheService, appExecutor, authorizationSecurity);
        deleteStaffUseCase = new DeleteStaffUseCase(
                sqlStaff,staffCache,staffCacheService, authorizationSecurity,logsService);
        editStaffUseCase = new EditStaffUseCase(
                sqlStaff,staffCache, staffCacheService,securePassword, logsService, authorizationSecurity);
        getStaffUseCase = new GetStaffUseCase(
                staffCache);
        registerStaffUseCase = new RegisterStaffUseCase(
                sqlStaff,staffCache, securePassword);


        createAnnounceUseCase = new CreateAnnounceUseCase(
                authorizationSecurity, logsService,staffCache,announceCache,sqlAnnounce);
        editAnnounceUseCase = new EditAnnounceUseCase(
                authorizationSecurity,logsService,staffCache, announceCache,sqlAnnounce);
        eventsAnnounceUseCase = new EventsAnnounceUseCase(
                authorizationSecurity, logsService, staffCache,announceCache, sqlAnnounce);
        getAnnounceUseCase = new GetAnnounceUseCase(
                authorizationSecurity,staffCache,announceCache,sqlAnnounce);


        createRoleUseCase = new CreateRoleUseCase(
                sqlRole, roleCache, authorizationSecurity, logsService);
        deleteRoleUseCase = new DeleteRoleUseCase(
                sqlRole, roleCache,sqlStaff, logsService, authorizationSecurity);
        getRoleUseCase = new GetRoleUseCase(
                sqlRole,roleCache);
        updateRoleUseCase = new UpdateRoleUseCase(
                sqlRole,roleCache, logsService, authorizationSecurity);

        getLogUseCase = new GetLogUseCase(logsService);
    }

    public AppExecutor getAppExecutor() {
        return appExecutor;
    }

    public LoginStaffUseCase getLoginStaffUseCase() {
        return loginStaffUseCase;
    }

    public DeleteStaffUseCase getDeleteStaffUseCase() {
        return deleteStaffUseCase;
    }

    public EditStaffUseCase getEditStaffUseCase() {
        return editStaffUseCase;
    }

    public GetStaffUseCase getGetStaffUseCase() {
        return getStaffUseCase;
    }

    public RegisterStaffUseCase getRegisterStaffUseCase() {
        return registerStaffUseCase;
    }

    public CreateAnnounceUseCase getCreateAnnounceUseCase() {
        return createAnnounceUseCase;
    }

    public EditAnnounceUseCase getEditAnnounceUseCase() {
        return editAnnounceUseCase;
    }

    public EventsAnnounceUseCase getEventsAnnounceUseCase() {
        return eventsAnnounceUseCase;
    }

    public GetAnnounceUseCase getGetAnnounceUseCase() {
        return getAnnounceUseCase;
    }

    public CreateRoleUseCase getCreateRoleUseCase() {
        return createRoleUseCase;
    }

    public DeleteRoleUseCase getDeleteRoleUseCase() {
        return deleteRoleUseCase;
    }

    public GetRoleUseCase getGetRoleUseCase() {
        return getRoleUseCase;
    }

    public UpdateRoleUseCase getUpdateRoleUseCase() {
        return updateRoleUseCase;
    }

    public AuthorizationSecurity getAuthorizationSecurity() {
        return authorizationSecurity;
    }

    public GetLogUseCase getGetLogUseCase() {
        return getLogUseCase;
    }
}
