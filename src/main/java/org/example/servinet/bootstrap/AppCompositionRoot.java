package org.example.servinet.bootstrap;

import org.example.servinet.application.port.out.*;
import org.example.servinet.application.port.out.sql.*;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.application.service.*;
import org.example.servinet.application.usecase.GetLogUseCase;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.TechnicalUseCase;
import org.example.servinet.application.usecase.announce.CreateAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EditAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EventsAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.application.usecase.antenna.*;
import org.example.servinet.application.usecase.api.ConsultDniUseCase;
import org.example.servinet.application.usecase.appsetting.AppGeneralUseCase;
import org.example.servinet.application.usecase.clients.*;
import org.example.servinet.application.usecase.inventory.*;
import org.example.servinet.application.usecase.payments.CreatePaymentUseCase;
import org.example.servinet.application.usecase.payments.GetPaymentUseCase;
import org.example.servinet.application.usecase.payments.PaidPaymentUseCase;
import org.example.servinet.application.usecase.role.CreateRoleUseCase;
import org.example.servinet.application.usecase.role.DeleteRoleUseCase;
import org.example.servinet.application.usecase.role.GetRoleUseCase;
import org.example.servinet.application.usecase.role.UpdateRoleUseCase;
import org.example.servinet.application.usecase.staff.*;
import org.example.servinet.domain.entities.*;
import org.example.servinet.application.port.out.WhatsAppSendProvider;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.domain.repository.CacheRepository;
import org.example.servinet.infrastructure.api.ApisPeruAdapter;
import org.example.servinet.infrastructure.api.GmailAdapter;
import org.example.servinet.infrastructure.api.LocationPicker;
import org.example.servinet.infrastructure.api.UltraMsgWhatsAppAdapter;
import org.example.servinet.infrastructure.cache.YamlCacheLoader;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.*;
import org.example.servinet.infrastructure.database.sqlserver.*;
import org.example.servinet.infrastructure.hardware.WindowsHardwareIdProvider;
import org.example.servinet.infrastructure.pdf.ClientContractPdfGenerator;
import org.example.servinet.infrastructure.security.SecurePasswordGenerator;

import java.net.http.HttpClient;
import java.util.List;

public class AppCompositionRoot {
    /**
     * ===============================================================
     **/
    private final AppExecutor appExecutor;
    /**
     *  Announce UseCase
     **/
    private final CreateAnnounceUseCase createAnnounceUseCase;
    private final EditAnnounceUseCase editAnnounceUseCase;
    private final EventsAnnounceUseCase eventsAnnounceUseCase;
    private final GetAnnounceUseCase getAnnounceUseCase;
    /**
     *  Antenna UseCase
     **/
    private final CreateAntennaUseCase createAntennaUseCase;
    private final GetAntennaUseCase getAntennaUseCase;
    private final DeleteAntennaUseCase deleteAntennaUseCase;
    private final MaintenanceAntennaUseCase maintenanceAntennaUseCase;
    private final EditAntennaUseCase editAntennaUseCase;
    /**
     *  APIs UseCase
     **/
    private final ConsultDniUseCase consultDniUseCase;
    private final WhatsAppService whatsAppService;
    /**
     *  AppGeneral UseCase
     **/
    private final AppGeneralUseCase appGeneralUseCase;
    /**
     *  Client UseCase
     **/
    private final CreateClientUseCase createClientUseCase;
    private final EditClientUseCase editClientUseCase;
    private final GetClientUseCase getClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final CallClientUseCase callClientUseCase;
    /**
     *  Payment UseCase
     **/
    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final PaidPaymentUseCase paidPaymentUseCase;
    /**
     *  Role UseCase
     **/
    private final CreateRoleUseCase createRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    /**
     *  Staff UseCase
     **/
    private final LoginStaffUseCase loginStaffUseCase;
    private final DeleteStaffUseCase deleteStaffUseCase;
    private final EditStaffUseCase editStaffUseCase;
    private final GetStaffUseCase getStaffUseCase;
    private final RegisterStaffUseCase registerStaffUseCase;
    /**
     *  log UseCase
     **/
    private final GetLogUseCase getLogUseCase;
    /**
     *  Plan UseCase
     **/
    private final PlanUseCase planUseCase;
    /**
     *  Technical UseCase
     **/
    private final TechnicalUseCase technicalUseCase;

    /**
     *  Inventory UseCase
     **/

    private final InventoryRepository inventoryRepository;
    private final CreateInventoryUseCase createInventoryUseCase;
    private final UpdateInventoryUseCase updateInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final ManageInventoryStockUseCase manageInventoryStockUseCase;
    private final ManageInventoryEquipmentUseCase manageInventoryEquipmentUseCase;
    /**
     * ============================ Services =================================
     * Permission Security
     **/
    private final AuthorizationSecurity authorizationSecurity;
    /**
     * ===============================================================
     **/

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
        GmailAdapter gmailProvider = new GmailAdapter(cache.getGmailConfig());

        /*------ LOAD MEMORY CACHE -------------*/
        InMemoryComplexRepository<Staff> staffCache = new InMemoryStaffAuthRepository();

        LogsRepository sqlLogs = new SqlLogsRepository(dbFactory);
        InMemoryRepository<LogEntry> logsCache = sqlLogs.getLogs(50);


        InMemoryRepository<Role> roleCache = new InMemoryRolesRepository();
        InMemoryRepository<Announcement> announceCache = new InMemoryAnnounceRepository();
        InMemoryRepository<Antenna> antennaCache = new InMemoryAntennasRepository();
        HardwareIdProvider hardwareIdProvider = new WindowsHardwareIdProvider();
        InMemoryAppRepository appGeneralCache = new InMemoryAppSettingRepository();
        InMemoryClientRepository clientCache = new InMemoryClientsRepository();
        InMemoryRepository<ClientPlan> planCache = new InMemoryPlanRepository();
        InMemoryTechnicalRepository technicalCache = new  InMemoryCacheTechnicalRepository();
        InMemoryRelationAntClinRepository relationAntCliCache = new InMemoryRelationAntClinRepository();

        /*------ LOAD SQL CLASS -------------*/
        StaffRepository sqlStaff = new SqlStaffAuthRepository(dbFactory);

        AnnounceRepository sqlAnnounce = new SqlAnnouncementRepository(dbFactory);
        LimitedDataRepository<Role,List<Role>> sqlRole = new SqlRoleRepository(dbFactory);
        ComplexRepository<Antenna> sqlAntenna = new SqlAntennaRepository(dbFactory);
        AppRepository sqlAppGeneral = new SqlAppGeneralRepository(dbFactory);
        ClientRepository sqlClient = new SqlClientRepository(dbFactory);
        LongDataRepository<Payment, List<Payment>, List<Payment>> sqlPayment = new SqlPaymentsRepository(dbFactory);
        TechnicalRepository sqlTechnical = new SqlTechnicalRepository(dbFactory);
        Sellerrepository sqlSeller = new SqlSellerRepository(dbFactory);
        inventoryRepository = new SqlInventoryRepository(dbFactory);
        /*------ LOAD SERVICES -------------*/

        ActivityLogsService logsService = new ActivityLogsService(staffCache, logsCache, sqlLogs);
        StaffCacheService staffCacheService = new StaffCacheService(sqlStaff,staffCache);
        authorizationSecurity = new AuthorizationSecurity(staffCache,new PermissionPolicy());
        //AuthorizationService tendrà un metodo que se llamarà cuando el usuario iniciò ya sesion para asignar permisos.
        PaymentsService paymentsService = new PaymentsService(sqlPayment);
        ClientService clientService = new ClientService(clientCache, appExecutor, sqlClient, paymentsService);
        PlanService planService = new PlanService(planCache,sqlClient);
        GmailService gmailService = new GmailService(gmailProvider);
        AntennaService antennaService = new AntennaService(sqlAntenna, antennaCache, relationAntCliCache);
        whatsAppService = new WhatsAppService(whatsAppSendProvider);
        ClientContractService clientContractService = new ClientContractService(new ClientContractPdfGenerator(), whatsAppService, gmailService);
        SellerService sellerService= new SellerService(sqlSeller);
        /*------ LOAD ADDS-------------*/

        SecurePasswordGenerator securePassword = new SecurePasswordGenerator();

        /*------ LOAD USECASE -------------*/



        createInventoryUseCase = new CreateInventoryUseCase(inventoryRepository);
        updateInventoryUseCase = new UpdateInventoryUseCase(inventoryRepository);
        getInventoryUseCase = new GetInventoryUseCase(inventoryRepository);
        manageInventoryStockUseCase = new ManageInventoryStockUseCase(inventoryRepository);
        manageInventoryEquipmentUseCase = new ManageInventoryEquipmentUseCase(inventoryRepository);

        createAnnounceUseCase = new CreateAnnounceUseCase(
                authorizationSecurity, logsService,staffCache,announceCache,sqlAnnounce);
        editAnnounceUseCase = new EditAnnounceUseCase(
                authorizationSecurity,logsService,staffCache, announceCache,sqlAnnounce);
        eventsAnnounceUseCase = new EventsAnnounceUseCase(
                authorizationSecurity, logsService, staffCache,announceCache, sqlAnnounce);
        getAnnounceUseCase = new GetAnnounceUseCase(
                authorizationSecurity,staffCache,announceCache,sqlAnnounce);

        createAntennaUseCase = new CreateAntennaUseCase(antennaCache, sqlAntenna, logsService);
        getAntennaUseCase = new GetAntennaUseCase(antennaCache, sqlAntenna);
        deleteAntennaUseCase = new DeleteAntennaUseCase(antennaCache, sqlAntenna, logsService);
        maintenanceAntennaUseCase = new MaintenanceAntennaUseCase(antennaCache,sqlAntenna);
        editAntennaUseCase = new EditAntennaUseCase( sqlAntenna, logsService);


        consultDniUseCase = new ConsultDniUseCase(dniGetProvider);

        appGeneralUseCase = new AppGeneralUseCase(appGeneralCache, sqlAppGeneral,authorizationSecurity, logsService);

        createClientUseCase = new CreateClientUseCase(
                clientCache,
                authorizationSecurity,
                sqlClient,
                paymentsService,
                clientService,
                antennaService,
                clientContractService,
                sellerService,
                staffCacheService,
                logsService
        );
        editClientUseCase = new EditClientUseCase(clientCache, authorizationSecurity, sqlClient, logsService);
        getClientUseCase = new GetClientUseCase(
                clientCache, sqlClient, clientService,planService,authorizationSecurity);
        deleteClientUseCase = new DeleteClientUseCase(clientCache, authorizationSecurity,sqlClient, logsService);
        callClientUseCase = new CallClientUseCase(whatsAppService, staffCacheService,appExecutor
        );


        createPaymentUseCase = new CreatePaymentUseCase(paymentsService);
        getPaymentUseCase = new GetPaymentUseCase(paymentsService);
        paidPaymentUseCase = new PaidPaymentUseCase(sqlPayment, logsService);

        createRoleUseCase = new CreateRoleUseCase(
                sqlRole, roleCache, authorizationSecurity, logsService);
        deleteRoleUseCase = new DeleteRoleUseCase(
                sqlRole, roleCache,sqlStaff, logsService, authorizationSecurity);
        getRoleUseCase = new GetRoleUseCase(
                sqlRole,roleCache);
        updateRoleUseCase = new UpdateRoleUseCase(
                sqlRole,roleCache, logsService, authorizationSecurity);

        loginStaffUseCase = new LoginStaffUseCase(
                hardwareIdProvider,sqlStaff, staffCache, logsService, staffCacheService, appExecutor, authorizationSecurity);
        deleteStaffUseCase = new DeleteStaffUseCase(
                sqlStaff,staffCache,staffCacheService, authorizationSecurity,logsService);
        editStaffUseCase = new EditStaffUseCase(
                sqlStaff,staffCache, staffCacheService,securePassword, logsService,
                appExecutor,whatsAppService);
        getStaffUseCase = new GetStaffUseCase(
                staffCache);
        registerStaffUseCase = new RegisterStaffUseCase(
                sqlStaff,staffCache, securePassword, whatsAppService, appExecutor);

        getLogUseCase = new GetLogUseCase(logsService);
        planUseCase = new PlanUseCase(sqlClient, planCache,planService);
        technicalUseCase = new TechnicalUseCase(
                technicalCache, staffCacheService, clientService,sqlTechnical
        );
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

    public CreateAntennaUseCase getCreateAntennaUseCase() {
        return createAntennaUseCase;
    }

    public GetAntennaUseCase getGetAntennaUseCase() {
        return getAntennaUseCase;
    }

    public ConsultDniUseCase getConsultDniUseCase() {
        return consultDniUseCase;
    }

    public WhatsAppService getWhatsAppService() {
        return whatsAppService;
    }

    public AppGeneralUseCase getAppGeneralUseCase() {
        return appGeneralUseCase;
    }

    public CreateClientUseCase getCreateClientUseCase() {
        return createClientUseCase;
    }

    public EditClientUseCase getEditClientUseCase() {
        return editClientUseCase;
    }

    public GetClientUseCase getGetClientUseCase() {
        return getClientUseCase;
    }

    public CreatePaymentUseCase getCreatePaymentUseCase() {
        return createPaymentUseCase;
    }

    public GetPaymentUseCase getGetPaymentUseCase() {
        return getPaymentUseCase;
    }

    public PaidPaymentUseCase getPaidPaymentUseCase() {
        return paidPaymentUseCase;
    }

    public PlanUseCase getPlanUseCase() {
        return planUseCase;
    }

    public TechnicalUseCase getTechnicalUseCase() {
        return technicalUseCase;
    }

    public DeleteAntennaUseCase getDeleteAntennaUseCase() {
        return deleteAntennaUseCase;
    }

    public MaintenanceAntennaUseCase getMaintenanceAntennaUseCase() {
        return maintenanceAntennaUseCase;
    }

    public DeleteClientUseCase getDeleteClientUseCase() {
        return deleteClientUseCase;
    }

    public EditAntennaUseCase getEditAntennaUseCase() {
        return editAntennaUseCase;
    }

    public CallClientUseCase getCallClientUseCase() {
        return callClientUseCase;
    }

    public LocationPicker getLocationPicker(){
        return new LocationPicker();
    }

    public InventoryRepository getInventoryRepository() {
        return inventoryRepository;
    }

    public CreateInventoryUseCase getCreateInventoryUseCase() {
        return createInventoryUseCase;
    }

    public UpdateInventoryUseCase getUpdateInventoryUseCase() {
        return updateInventoryUseCase;
    }

    public GetInventoryUseCase getGetInventoryUseCase() {
        return getInventoryUseCase;
    }

    public ManageInventoryStockUseCase getManageInventoryStockUseCase() {
        return manageInventoryStockUseCase;
    }

    public ManageInventoryEquipmentUseCase getManageInventoryEquipmentUseCase() {
        return manageInventoryEquipmentUseCase;
    }
}
