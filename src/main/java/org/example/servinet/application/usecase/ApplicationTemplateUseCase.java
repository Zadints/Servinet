package org.example.servinet.application.usecase;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.example.servinet.application.port.out.PasswordGenerator;
import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.sql.SqlLimitedDataRepository;
import org.example.servinet.application.port.out.sql.SqlStaffRepository;
import org.example.servinet.application.service.setup.DatabaseSetupService;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.infrastructure.database.sqlite.SqliteRole;

import java.time.LocalDateTime;
import java.util.*;

import static org.example.servinet.domain.enums.Permission.*;

public class ApplicationTemplateUseCase {

    private final SqlStaffRepository sqlStaffRepository;
    private final SqlLimitedDataRepository<Role, List<Role>> sqlRoleRepository;
    private final boolean isNewAccount;
    private final InMemoryRepository<Role> roleInMemoryRepository;
    private final Argon2 argon;
    private String uuid;

    public ApplicationTemplateUseCase(SqlStaffRepository sqlStaffRepository, SqlLimitedDataRepository<Role, List<Role>> sqlRoleRepository, boolean isNewAccount, InMemoryRepository<Role> roleInMemoryRepository) {
        this.sqlStaffRepository = sqlStaffRepository;
        this.sqlRoleRepository = sqlRoleRepository;
        this.isNewAccount = isNewAccount;
        this.roleInMemoryRepository = roleInMemoryRepository;
        this.argon = Argon2Factory.create();
    }


    public void executeTemplate() {
        try {
            runTemplateRoles();
            runTemplateOwner();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage() + e.getCause());
        }
    }

    private void runTemplateRoles() {
        System.out.println("iniciando template roles");
        if (!isNewAccount) return;

        List<Role> roles = new ArrayList<>();
        uuid = UUID.randomUUID().toString();
        roles.add(new Role(
                uuid,
                EnumSet.of(BYPASS),
                "#D64D5B",
                "Administrador"
        ));
        roles.add(new Role(
                UUID.randomUUID().toString(),
                EnumSet.of(DASH_BYPASS, ANNOUNCE_VIEW,
                        ANT_VIEW_ANTENNAS, ANT_VIEW_INFO, ANT_EDIT,
                        ANT_START_MAINTE, ANT_END_MAINT, ANT_GO_ACTIVE, ANT_GO_DESACTIVE),
                "#54A5F3",
                "Técnico"
        ));

        roles.add(new Role(
                UUID.randomUUID().toString(),
                EnumSet.of(DASH_BYPASS, ANNOUNCE_VIEW, SELL_BYPASS,
                        CLIENT_SEARCH, CLIENT_INFO, CLIENT_PAY_VIEW),
                "#4ED55D",
                "Vendedor"
        ));

        for (Role r : roles) {
            sqlRoleRepository.add(r);
            roleInMemoryRepository.addEntity(r);
        }
        System.out.println("Roles tempalte finalziado");
    }

    private void runTemplateOwner(){

        System.out.println("Template owner iniciado");
        if (!isNewAccount) return;

        Role role = (Role) roleInMemoryRepository.getAll().stream().filter(
                rol -> rol.getUuid().equals(uuid)).findFirst().orElse(null);

        if (role == null) throw new IllegalArgumentException("No existe el rol owner");

        sqlStaffRepository.setStaffDatabase(new Staff(
                UUID.randomUUID().toString(),
                "084273558",
                "email@owner.com",
                role,
                LocalDateTime.now(),
                "Owner",
                null
        ), hashPassword("admin123"));

        System.out.println("templateOwner finalizado");
    }

    private String hashPassword(String password){
        return argon.hash(3, 65536, 1, password);
    }
}
