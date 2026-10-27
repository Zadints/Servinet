package org.example.servinet.infrastructure.database.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlRepository;
import org.example.servinet.domain.entities.Role;

import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;
import org.example.servinet.infrastructure.database.inmemory.InMemoryRolesRepository;

import java.sql.*;

import java.util.*;
import java.util.stream.Collectors;

public class SqlRoleRepository implements SqlRepository<Role> {

    private final DatabaseFactory databaseFactory;

    public SqlRoleRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void add(Role rol) {

        try {
            String permissions = rol.getPermissions().stream().map(Enum::name).collect(Collectors.joining(","));
            String sql = "{call dbo.sp_CreateRole(?,?,?,?)}";

            try (Connection conn = databaseFactory.getConnection();
                CallableStatement stmt = conn.prepareCall(sql)) {
                    stmt.setString(1, rol.getUuid());
                    stmt.setString(2, rol.getName());
                    stmt.setString(3, rol.getHexColor());
                    stmt.setString(4, permissions);
                    stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al crear el rol porque el rol ya existe", e
            );
        }
    }

    @Override
    public Role get(String id) {
        return null;
    }

    @Override
    public InMemoryRepository<Role> getAll() {
        try {

            String sql = "{CALL sp_GetRoles()}";

            Map<String, Role> roles = new LinkedHashMap<>();

            try (Connection conn = databaseFactory.getConnection();
                    CallableStatement stmt = conn.prepareCall(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    String uuid = rs.getString("uuid");

                    Role role = roles.get(uuid);

                    if (role == null) {

                        role = new Role(
                                uuid,
                                new HashSet<>(),
                                rs.getString("hexColor"),
                                rs.getString("name")
                        );

                        roles.put(uuid, role);
                    }

                    String permissionName = rs.getString("permission");

                    if (permissionName != null) {
                        role.getPermissions().add(
                                Permission.valueOf(permissionName)
                        );
                    }
                }
            }
            InMemoryRepository<Role> memory = new InMemoryRolesRepository();
            memory.setAll(new ArrayList<>(roles.values()));

            return memory;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener los roles de la base de datos.",
                    e
            );
        }


    }

    @Override
    public void delete(String id) {
        try {
            String sql = "{call dbo.sp_DeleteRole(?)}";

            try (Connection conn = databaseFactory.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el rol", e);
        }
    }

    @Override
    public void update(Role role) {
        String sql = "{call dbo.sp_UpdateRole(?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection()) {

            SQLServerDataTable permissionTable = new SQLServerDataTable();

            permissionTable.addColumnMetadata(
                    "permission",
                    Types.VARCHAR
            );

            for (Permission permission : role.getPermissions()) {
                permissionTable.addRow(permission.name());
            }

            try (SQLServerCallableStatement stmt =
                         (SQLServerCallableStatement) conn.prepareCall(sql)) {

                stmt.setString(1, role.getUuid());
                stmt.setString(2, role.getName());
                stmt.setString(3, role.getHexColor());

                stmt.setStructured(
                        4,
                        "dbo.PermissionListType",
                        permissionTable
                );

                stmt.execute();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el rol",
                    e
            );
        }
    }


}