package org.example.servinet.application.usecase.role;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LimitedDataRepository;
import org.example.servinet.domain.entities.Role;

import java.util.List;

public class GetRoleUseCase {
    private final LimitedDataRepository<Role, InMemoryRepository<Role>> roleSqlRepository;
    private final InMemoryRepository<Role> roleInMemoryRepository;

    public GetRoleUseCase(LimitedDataRepository<Role, InMemoryRepository<Role>> roleSqlRepository, InMemoryRepository<Role> roleInMemoryRepository) {
        this.roleSqlRepository = roleSqlRepository;
        this.roleInMemoryRepository = roleInMemoryRepository;
    }

    public List<Role> getRoles() {
        return roleInMemoryRepository.getAll();
    }

    public Role getDbRole(String id){
        return roleSqlRepository.get(id);
    }

    public Role getRol(String name) {

        if (name == null) {
            return null;
        }

        for (Role rol : roleInMemoryRepository.getAll()) {

            if (rol.getName() != null
                    && rol.getName().equalsIgnoreCase(name.trim())) {

                return rol;
            }
        }

        return null;
    }
}
