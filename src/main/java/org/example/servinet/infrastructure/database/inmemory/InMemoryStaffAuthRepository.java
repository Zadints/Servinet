package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.domain.entities.LogEntry;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.application.port.out.InMemoryRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStaffAuthRepository implements InMemoryRepository<Staff> {

    private Staff currentStaff = null;
    private List<Staff> allStaffExist = new ArrayList<>();

    @Override
    public Staff getCurrent() {
        return this.currentStaff;
    }

    @Override
    public List<Staff> getAll() {
        return this.allStaffExist;
    }

    @Override
    public void setCurrent(Staff s) {
        this.currentStaff = s;
    }

    @Override
    public void setAll(List<Staff> staff) {
        this.allStaffExist = staff;
    }

    @Override
    public void addEntity(Staff entity) {
        allStaffExist.add(entity);
    }

    @Override
    public void editCurrent(Staff entity) throws IllegalArgumentException {
        if (!this.currentStaff.getUuid().equals(entity.getUuid())){
            throw new IllegalArgumentException("El staff actual no es el mismo a editar");
        }

    }

    @Override
    public void removeList(String id) {
        allStaffExist.removeIf(s -> s.getUuid().equals(id));
    }

    @Override
    public void resetCurrent() {
        currentStaff = null;
    }


}
