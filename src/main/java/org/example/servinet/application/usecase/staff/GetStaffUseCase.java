package org.example.servinet.application.usecase.staff;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;


import java.util.List;

public class GetStaffUseCase {

    private final InMemoryRepository<Staff> inMemoryRepository;

    public GetStaffUseCase(InMemoryRepository<Staff> inMemoryRepository) {
        this.inMemoryRepository = inMemoryRepository;
    }

    public List<Staff> getAllStaff() {
        return inMemoryRepository.getAll();
    }

    public Staff getActualSessionStaff() {
        if (inMemoryRepository.getCurrent() == null)
            throw new NullPointerException("No puedes solicitar datos del usuario si no existen");

        return inMemoryRepository.getCurrent();
    }

    public Role getUserRol() {
        return inMemoryRepository.getCurrent().getRol();
    }
}
