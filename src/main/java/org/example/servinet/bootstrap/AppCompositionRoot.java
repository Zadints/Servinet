package org.example.servinet.bootstrap;

import org.example.servinet.application.port.out.LogsRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.application.port.out.HardwareIdProvider;
import org.example.servinet.domain.repository.CacheRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.infrastructure.cache.YamlCacheLoader;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.InMemoryLogsRepository;
import org.example.servinet.infrastructure.database.inmemory.InMemoryStaffAuthRepository;
import org.example.servinet.infrastructure.database.sqlserver.SqlLogsRepository;
import org.example.servinet.infrastructure.database.sqlserver.SqlStaffAuthRepository;
import org.example.servinet.infrastructure.hardware.WindowsHardwareIdProvider;

public class AppCompositionRoot {
    private final AppExecutor appExecutor;

    private final LoginStaffUseCase loginStaffUseCase;

    public AppCompositionRoot() {
        this.appExecutor = new AppExecutor();

        YamlCacheLoader cacheLoader = new YamlCacheLoader();
        CacheRepository cache = cacheLoader.load("config/config.yml");
        DatabaseFactory dbFactory = new DatabaseFactory(cache.getDatabaseConfig());


        InMemoryRepository<Staff> staffCacheRepository = new InMemoryStaffAuthRepository();
        InMemoryRepository<LogEntry> logsCacheRepository = new InMemoryLogsRepository();
        HardwareIdProvider hardwareIdProvider = new WindowsHardwareIdProvider();

        StaffRepository sqlStaffAuthRepository = new SqlStaffAuthRepository(dbFactory);
        LogsRepository sqlLogsRepository = new SqlLogsRepository(dbFactory);

        ActivityLogsService activityLogsService = new ActivityLogsService(
                staffCacheRepository, logsCacheRepository, sqlLogsRepository);

        loginStaffUseCase = new LoginStaffUseCase(
                hardwareIdProvider,sqlStaffAuthRepository, staffCacheRepository, activityLogsService);

    }

    public AppExecutor getAppExecutor() {
        return appExecutor;
    }

    public LoginStaffUseCase getLoginStaffUseCase() {
        return loginStaffUseCase;
    }
}
