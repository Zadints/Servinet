package org.example.servinet.application.usecase.role;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LimitedDataRepository;
import org.example.servinet.domain.entities.Role;

import java.util.List;

public class GetRoleUseCase {
    private final LimitedDataRepository<Role, List<Role>> roleSqlRepository;
    private final InMemoryRepository<Role> roleInMemoryRepository;

    public GetRoleUseCase(LimitedDataRepository<Role, List<Role>> roleSqlRepository, InMemoryRepository<Role> roleInMemoryRepository) {
        this.roleSqlRepository = roleSqlRepository;
        this.roleInMemoryRepository = roleInMemoryRepository;
    }

    public List<Role> getRoles() {

        List<Role> roles =  roleInMemoryRepository.getAll();

        if (!roles.isEmpty()) return roles;

        roles = roleSqlRepository.getAll();

        if (roles.isEmpty()){
            return null;
        }

        roleInMemoryRepository.setAll(roles);

        return roles;

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
