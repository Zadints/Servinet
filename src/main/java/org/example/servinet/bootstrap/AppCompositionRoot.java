package org.example.servinet.bootstrap;

import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.application.port.out.HardwareIdProvider;
import org.example.servinet.domain.repository.CacheRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.infrastructure.cache.YamlCacheLoader;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.InMemoryStaffAuthRepository;
import org.example.servinet.infrastructure.database.sqlserver.SqlStaffAuthRepository;
import org.example.servinet.infrastructure.hardware.WindowsHardwareIdProvider;

public class AppCompositionRoot {
    private final AppExecutor appExecutor;
    private final HardwareIdProvider hardwareIdProvider;

    private final SqlStaffAuthRepository sqlStaffAuthRepository;


    private final InMemoryRepository<Staff> inMemoryStaffAuthRepository;
    private final LoginStaffUseCase loginStaffUseCase;

    public AppCompositionRoot() {
        this.appExecutor = new AppExecutor();

        YamlCacheLoader cacheLoader = new YamlCacheLoader();
        CacheRepository cache = cacheLoader.load("config/config.yml");
        DatabaseFactory dbConnection = new DatabaseFactory(cache.getDatabaseConfig());

        hardwareIdProvider = new WindowsHardwareIdProvider();
        inMemoryStaffAuthRepository = new InMemoryStaffAuthRepository();


        loginStaffUseCase = new LoginStaffUseCase();

    }

    public AppExecutor getAppExecutor() {
        return appExecutor;
    }

}
