package org.example.servinet.application.service;

import org.example.servinet.application.port.out.inmemory.InMemoryComplexRepository;
import org.example.servinet.application.port.out.sql.SqlStaffRepository;
import org.example.servinet.domain.entities.Staff;

import java.util.List;

public class StaffCacheService {

    private final SqlStaffRepository sqlStaffRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;

    public StaffCacheService(
            SqlStaffRepository sqlStaffRepository,
            InMemoryComplexRepository<Staff> inMemoryRepository
    ) {
        this.sqlStaffRepository = sqlStaffRepository;
        this.inMemoryRepository = inMemoryRepository;
    }

    public void reload() {
        System.out.println("Cargando staff...");
        List<Staff> staff = sqlStaffRepository.getAllStaff();
        inMemoryRepository.setAll(staff);

    }
    public List<Staff> getAllStaff() {
        return inMemoryRepository.getAll();
    }
    public Staff getCurrent(){
        return inMemoryRepository.getCurrent();
    }
}
