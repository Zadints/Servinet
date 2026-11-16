package org.example.servinet.infrastructure.database.sqlserver;
import org.example.servinet.application.dto.*;
import org.example.servinet.application.port.out.sql.InventoryRepository;
import org.example.servinet.domain.enums.InventoryCategory;
import org.example.servinet.domain.enums.StockStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class SqlInventoryRepository implements InventoryRepository {
    private final DatabaseFactory databaseFactory;

    public SqlInventoryRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    // ==========================================
    // CREAR ARTÍCULO
    // ==========================================
    @Override
    public UUID createItem(InventoryItem item) {

        String sql = "{call dbo.sp_CreateInventoryItem(?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, item.name());
            stmt.setString(2, item.description());
            stmt.setString(3, item.category().name());
            stmt.setString(4, item.brand());
            stmt.setString(5, item.model());
            stmt.setBoolean(6, item.recoverable());
            stmt.setString(7, item.unit());
            stmt.setBigDecimal(8, item.minimumStock());
            stmt.setBigDecimal(9, item.purchasePrice());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(
                            rs.getString("item_uuid")
                    );
                }
            }

            throw new DatabaseException(
                    "No se pudo obtener el UUID del artículo creado"
            );

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al crear el artículo: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // ACTUALIZAR ARTÍCULO
    // ==========================================

    public void updateItem(InventoryItem item) {

        String sql = "{call dbo.sp_UpdateInventoryItem(?,?,?,?,?,?,?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, item.uuid().toString());
            stmt.setString(2, item.name());
            stmt.setString(3, item.description());
            stmt.setString(4, item.category().name());
            stmt.setString(5, item.brand());
            stmt.setString(6, item.model());
            stmt.setBoolean(7, item.recoverable());
            stmt.setBigDecimal(8, item.minimumStock());
            stmt.setBigDecimal(9, item.purchasePrice());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al actualizar el artículo: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // OBTENER TODOS LOS ARTÍCULOS
    // ==========================================

    public List<InventoryItem> getAllItems() {

        String sql = "{call dbo.sp_GetInventoryItems()}";

        List<InventoryItem> items = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapItem(rs));
            }

            return items;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener el inventario: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // OBTENER ALERTAS DE STOCK
    // ==========================================

    public List<InventoryAlert> getStockAlerts() {

        String sql = "{call dbo.sp_GetInventoryAlerts()}";

        List<InventoryAlert> alerts = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                alerts.add(new InventoryAlert(
                        UUID.fromString(rs.getString("item_uuid")),
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        InventoryCategory.valueOf(
                                rs.getString("item_category")
                        ),
                        rs.getBigDecimal("item_stock"),
                        rs.getBigDecimal("item_min_stock"),
                        rs.getString("item_unit"),
                        StockStatus.valueOf(
                                rs.getString("stock_status")
                        )
                ));
            }

            return alerts;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener las alertas de inventario: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // REGISTRAR MOVIMIENTO
    // ==========================================

    public InventoryMovementResult registerMovement(
            UUID itemUuid,
            String movementType,
            BigDecimal quantity,
            UUID staffUuid,
            UUID orderUuid,
            String description
    ) {

        String sql = "{call dbo.sp_RegisterInventoryMovement(?,?,?,?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, itemUuid.toString());
            stmt.setString(2, movementType);
            stmt.setBigDecimal(3, quantity);

            setNullableUuid(stmt, 4, staffUuid);
            setNullableUuid(stmt, 5, orderUuid);

            stmt.setString(6, description);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return new InventoryMovementResult(
                            UUID.fromString(
                                    rs.getString("movement_uuid")
                            ),
                            rs.getBigDecimal("previous_stock"),
                            rs.getBigDecimal("new_stock")
                    );
                }
            }

            throw new DatabaseException(
                    "No se pudo registrar el movimiento"
            );

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al registrar movimiento: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // OBTENER HISTORIAL DE MOVIMIENTOS
    // ==========================================

    public List<InventoryMovement> getMovements(UUID itemUuid) {

        String sql = "{call dbo.sp_GetInventoryMovements(?)}";

        List<InventoryMovement> movements = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            setNullableUuid(stmt, 1, itemUuid);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    movements.add(new InventoryMovement(
                            UUID.fromString(
                                    rs.getString("movement_uuid")
                            ),
                            UUID.fromString(
                                    rs.getString("item_uuid")
                            ),
                            rs.getString("item_name"),
                            rs.getString("item_code"),
                            rs.getString("movement_type"),
                            rs.getBigDecimal("movement_quantity"),
                            rs.getBigDecimal("movement_previous_stock"),
                            rs.getBigDecimal("movement_new_stock"),
                            rs.getString("movement_description"),
                            getNullableUuid(rs, "staff_uuid"),
                            getNullableUuid(rs, "order_uuid"),
                            rs.getTimestamp("movement_created_at")
                                    .toLocalDateTime()
                    ));
                }
            }

            return movements;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener movimientos: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // DESACTIVAR ARTÍCULO
    // ==========================================

    public void deactivateItem(UUID itemUuid) {

        String sql = "{call dbo.sp_DeactivateInventoryItem(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, itemUuid.toString());
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al desactivar el artículo: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // MAPEO DE ARTÍCULOS
    // ==========================================

    private InventoryItem mapItem(ResultSet rs) throws SQLException {

        return new InventoryItem(
                UUID.fromString(rs.getString("item_uuid")),
                rs.getString("item_code"),
                rs.getString("item_name"),
                rs.getString("item_description"),
                InventoryCategory.valueOf(
                        rs.getString("item_category")
                ),
                rs.getString("item_brand"),
                rs.getString("item_model"),
                rs.getBoolean("item_recoverable"),
                rs.getString("item_unit"),
                rs.getBigDecimal("item_stock"),
                rs.getBigDecimal("item_min_stock"),
                rs.getBigDecimal("item_purchase_price"),
                rs.getBoolean("item_active"),
                StockStatus.valueOf(
                        rs.getString("stock_status")
                )
        );
    }

    // ==========================================
    // UTILIDADES JDBC
    // ==========================================

    private void setNullableUuid(
            CallableStatement stmt,
            int index,
            UUID uuid
    ) throws SQLException {

        if (uuid == null) {
            stmt.setNull(index, Types.CHAR);
        } else {
            stmt.setString(index, uuid.toString());
        }
    }

    private UUID getNullableUuid(
            ResultSet rs,
            String column
    ) throws SQLException {

        String value = rs.getString(column);

        return value == null
                ? null
                : UUID.fromString(value);
    }

    // ==========================================
// REGISTRAR EQUIPO RECUPERABLE
// ==========================================

    public UUID registerEquipment(
            UUID itemUuid,
            String serialNumber,
            UUID staffUuid
    ) {
        String sql = "{call dbo.sp_RegisterInventoryEquipment(?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, itemUuid.toString());
            stmt.setString(2, serialNumber);
            setNullableUuid(stmt, 3, staffUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(
                            rs.getString("equipment_uuid")
                    );
                }
            }

            throw new DatabaseException(
                    "No se pudo obtener el UUID del equipo registrado"
            );

        }  catch (SQLException e) {

        if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
            throw new DatabaseException(
                    "El número de serie '" + serialNumber +
                            "' ya está registrado en el inventario."
            );
        }

        throw new DatabaseException(
                "Error al registrar equipo: " + e.getMessage()
        );
    }
    }


// ==========================================
// ASIGNAR EQUIPO A TÉCNICO
// ==========================================

    public void assignEquipment(
            UUID equipmentUuid,
            UUID staffUuid
    ) {
        String sql = "{call dbo.sp_AssignInventoryEquipment(?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, equipmentUuid.toString());
            stmt.setString(2, staffUuid.toString());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al asignar equipo al técnico: " + e.getMessage()
            );
        }
    }


// ==========================================
// INSTALAR EQUIPO EN CLIENTE
// ==========================================

    public void installEquipment(
            UUID equipmentUuid,
            String clientDni,
            UUID orderUuid,
            UUID staffUuid
    ) {
        String sql = "{call dbo.sp_InstallInventoryEquipment(?,?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, equipmentUuid.toString());
            stmt.setString(2, clientDni);
            stmt.setString(3, orderUuid.toString());
            stmt.setString(4, staffUuid.toString());

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al instalar equipo: " + e.getMessage()
            );
        }
    }


// ==========================================
// RECUPERAR EQUIPO DEL CLIENTE
// ==========================================

    public void recoverEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {
        String sql = "{call dbo.sp_RecoverInventoryEquipment(?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, equipmentUuid.toString());
            stmt.setString(2, staffUuid.toString());
            stmt.setString(3, description);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al recuperar equipo: " + e.getMessage()
            );
        }
    }


// ==========================================
// DEVOLVER EQUIPO AL ALMACÉN
// ==========================================

    public void releaseEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {
        String sql = "{call dbo.sp_ReleaseInventoryEquipment(?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, equipmentUuid.toString());
            stmt.setString(2, staffUuid.toString());
            stmt.setString(3, description);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al devolver equipo al almacén: " + e.getMessage()
            );
        }
    }

    @Override
    public InventoryMovementResult withdrawEquipment(
            UUID itemUuid,
            int quantity,
            UUID staffUuid,
            String description
    ) {
        String sql = "{call dbo.sp_WithdrawInventoryEquipment(?,?,?,?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, itemUuid.toString());
            stmt.setInt(2, quantity);
            setNullableUuid(stmt, 3, staffUuid);
            stmt.setString(4, description);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return new InventoryMovementResult(
                            UUID.fromString(rs.getString("movement_uuid")),
                            rs.getBigDecimal("previous_stock"),
                            rs.getBigDecimal("new_stock")
                    );
                }
            }

            throw new DatabaseException(
                    "No se pudo registrar la salida de equipos."
            );

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al retirar equipos: " + e.getMessage()
            );
        }
    }

    public List<InventoryEquipment> getTechnicianEquipment(UUID staffUuid) {

        String sql = "{call dbo.sp_GetTechnicianInventoryEquipment(?)}";

        List<InventoryEquipment> equipment = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, staffUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    equipment.add(mapEquipment(rs, false));
                }
            }

            return equipment;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener equipos del técnico: " + e.getMessage()
            );
        }
    }


    public List<InventoryEquipment> getClientEquipment(String clientDni) {

        String sql = "{call dbo.sp_GetClientInventoryEquipment(?)}";

        List<InventoryEquipment> equipment = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, clientDni);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    equipment.add(mapEquipment(rs, true));
                }
            }

            return equipment;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener equipos del cliente: " + e.getMessage()
            );
        }
    }

    private InventoryEquipment mapEquipment(
            ResultSet rs,
            boolean includeInstallation
    ) throws SQLException {

        LocalDateTime installedAt = null;
        UUID installedOrderUuid = null;

        if (includeInstallation) {

            Timestamp timestamp = rs.getTimestamp("installed_at");

            if (timestamp != null) {
                installedAt = timestamp.toLocalDateTime();
            }

            installedOrderUuid = getNullableUuid(
                    rs,
                    "installed_order_uuid"
            );
        }

        return new InventoryEquipment(
                UUID.fromString(rs.getString("equipment_uuid")),
                UUID.fromString(rs.getString("item_uuid")),
                rs.getString("item_code"),
                rs.getString("item_name"),
                rs.getString("serial_number"),
                rs.getString("equipment_status"),
                installedAt,
                installedOrderUuid
        );
    }

    //pendientes los procedimientos específicos para equipos dañados,
    // perdidos o dados de baja
}
