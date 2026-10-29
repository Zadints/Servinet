package org.example.servinet.infrastructure.database.inmemory;

import com.sun.jdi.InvalidTypeException;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.Role;

import java.util.ArrayList;
import java.util.List;

public class InMemoryRolesRepository implements InMemoryRepository<Role> {

    private List<Role> rolesList = new ArrayList<>();

    @Override
    public List<Role> getAll() {
        return rolesList;
    }
    @Override
    public void setAll(List<Role> listEntity) {
        rolesList = listEntity;
    }

    @Override
    public void addEntity(Role entity) {
        rolesList.add(entity);
    }

    @Override
    public void remove(String id) {
        rolesList.removeIf(s -> s.getUuid().equals(id));
    }

    @Override
    public void edit(Role entity) {
        for (int i = 0; i < rolesList.size(); i++) {
            if (rolesList.get(i).getUuid().equals(entity.getUuid())) {
                rolesList.set(i, entity);
                break;
            }
        }
    }

}
