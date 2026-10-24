package org.example.servinet.application.service;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.domain.entities.Staff;

import java.util.List;

public class StaffCacheService {

    private final StaffRepository staffRepository;
    private final InMemoryRepository<Staff> inMemoryRepository;

    public StaffCacheService(
            StaffRepository staffRepository,
            InMemoryRepository<Staff> inMemoryRepository
    ) {
        this.staffRepository = staffRepository;
        this.inMemoryRepository = inMemoryRepository;
    }

    public void reload() {
        List<Staff> staff = staffRepository.getAllStaff();
        inMemoryRepository.setAll(staff);
    }


}
