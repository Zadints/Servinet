package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.dto.inventory.*;
import org.example.servinet.application.port.out.sql.SqlInventoryRepository;
import org.example.servinet.domain.enums.InventoryCategory;
import org.example.servinet.domain.enums.StockStatus;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SqliteInventory implements SqlInventoryRepository {

    private static final BigDecimal MAX_STOCK = new BigDecimal("9999999999.99");
    private static final Set<String> UNITS = Set.of("UNIDAD", "METRO", "ROLLO", "CAJA", "PAQUETE");

    private static final String STOCK_STATUS_SQL =
            "CASE WHEN item_stock = 0 THEN 'OUT_OF_STOCK' "
                    + "WHEN item_stock <= item_min_stock THEN 'LOW_STOCK' "
                    + "ELSE 'AVAILABLE' END AS stock_status";

    private final DatabaseFactory databaseFactory;

    public SqliteInventory(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public UUID createItem(InventoryItem item) {

        String name = item.name() == null ? "" : item.name().trim();
        String category = item.category() == null ? "" : item.category().name().trim();
        String unit = item.unit() == null ? "UNIDAD" : item.unit().trim().toUpperCase();

        String sql = "INSERT INTO inventory_items (item_uuid, item_code, item_name, item_description, "
                + "item_category, item_brand, item_model, item_recoverable, item_unit, item_stock, "
                + "item_min_stock, item_purchase_price, item_active, item_created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 1, ?)";

        try (Connection conn = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(conn, c -> {

                if (name.isEmpty()) {
                    throw new DatabaseException("El nombre del artículo es obligatorio.");
                }

                if (category.isEmpty()) {
                    throw new DatabaseException("La categoría es obligatoria.");
                }

                if (!UNITS.contains(unit)) {
                    throw new DatabaseException("Unidad de medida inválida.");
                }

                if (item.minimumStock() == null || item.minimumStock().signum() < 0) {
                    throw new DatabaseException("El stock mínimo no puede ser negativo.");
                }

                if (item.purchasePrice() == null || item.purchasePrice().signum() < 0) {
                    throw new DatabaseException("El precio no puede ser negativo.");
                }

                UUID uuid = UUID.randomUUID();
                String code = nextCode(c);

                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, code);
                    stmt.setString(3, name);
                    SqliteUtil.setNullableString(stmt, 4, item.description());
                    stmt.setString(5, category);
                    SqliteUtil.setNullableString(stmt, 6, item.brand());
                    SqliteUtil.setNullableString(stmt, 7, item.model());
                    stmt.setBoolean(8, item.recoverable());
                    stmt.setString(9, unit);
                    SqliteUtil.setDecimal(stmt, 10, item.minimumStock());
                    SqliteUtil.setDecimal(stmt, 11, item.purchasePrice());
                    stmt.setString(12, SqliteUtil.nowUtc());
                    stmt.executeUpdate();
                }

                return uuid;
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al crear el artículo: " + e.getMessage()
            );
        }
    }

    private String nextCode(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTR(item_code, 5) AS INTEGER)), 0) + 1 "
                + "FROM inventory_items WHERE item_code LIKE 'INV-%'";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int next = rs.next() ? rs.getInt(1) : 1;
            return "INV-" + String.format("%06d", next);
        }
    }

    public void updateItem(InventoryItem item) {

        String sql = "UPDATE inventory_items SET item_name = ?, item_description = ?, item_category = ?, "
                + "item_brand = ?, item_model = ?, item_recoverable = ?, item_min_stock = ?, "
                + "item_purchase_price = ?, item_updated_at = ? WHERE item_uuid = ? AND item_active = 1";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (item.name() == null || item.name().isBlank()) {
                throw new DatabaseException("El nombre es obligatorio.");
            }

            if (item.minimumStock() == null || item.minimumStock().signum() < 0) {
                throw new DatabaseException("Stock mínimo inválido.");
            }

            if (item.purchasePrice() == null || item.purchasePrice().signum() < 0) {
                throw new DatabaseException("Precio inválido.");
            }

            stmt.setString(1, item.name().trim());
            SqliteUtil.setNullableString(stmt, 2, item.description());
            stmt.setString(3, item.category().name());
            SqliteUtil.setNullableString(stmt, 4, item.brand());
            SqliteUtil.setNullableString(stmt, 5, item.model());
            stmt.setBoolean(6, item.recoverable());
            SqliteUtil.setDecimal(stmt, 7, item.minimumStock());
            SqliteUtil.setDecimal(stmt, 8, item.purchasePrice());
            stmt.setString(9, SqliteUtil.nowUtc());
            stmt.setString(10, item.uuid().toString());

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("Artículo no encontrado o inactivo.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al actualizar el artículo: " + e.getMessage()
            );
        }
    }

    public List<InventoryItem> getAllItems() {

        String sql = "SELECT item_uuid, item_code, item_name, item_description, item_category, item_brand, "
                + "item_model, item_recoverable, item_unit, item_stock, item_min_stock, "
                + "item_purchase_price, item_active, item_created_at, " + STOCK_STATUS_SQL + " "
                + "FROM inventory_items WHERE item_active = 1 ORDER BY item_name";

        List<InventoryItem> items = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
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

    public List<InventoryAlert> getStockAlerts() {

        String sql = "SELECT item_uuid, item_code, item_name, item_category, item_stock, "
                + "item_min_stock, item_unit, "
                + "CASE WHEN item_stock = 0 THEN 'OUT_OF_STOCK' ELSE 'LOW_STOCK' END AS stock_status "
                + "FROM inventory_items WHERE item_active = 1 AND item_stock <= item_min_stock "
                + "ORDER BY item_stock ASC, item_name";

        List<InventoryAlert> alerts = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                alerts.add(new InventoryAlert(
                        UUID.fromString(rs.getString("item_uuid")),
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        InventoryCategory.valueOf(
                                rs.getString("item_category")
                        ),
                        SqliteUtil.getDecimal(rs, "item_stock"),
                        SqliteUtil.getDecimal(rs, "item_min_stock"),
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

    public InventoryMovementResult registerMovement(
            UUID itemUuid,
            String movementType,
            BigDecimal quantity,
            UUID staffUuid,
            UUID orderUuid,
            String description
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(conn, c -> {

                if (itemUuid == null) {
                    throw new DatabaseException("El artículo es obligatorio.");
                }

                if (quantity == null || quantity.signum() <= 0) {
                    throw new DatabaseException("La cantidad debe ser mayor a cero.");
                }

                String type = movementType == null ? "" : movementType.trim().toUpperCase();

                if (!type.equals("ENTRADA") && !type.equals("SALIDA") && !type.equals("AJUSTE")) {
                    throw new DatabaseException("Tipo de movimiento inválido.");
                }

                BigDecimal previous = null;
                boolean active = false;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT item_stock, item_active FROM inventory_items WHERE item_uuid = ?")) {
                    stmt.setString(1, itemUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            previous = SqliteUtil.getDecimal(rs, "item_stock");
                            active = rs.getBoolean("item_active");
                        }
                    }
                }

                if (previous == null) {
                    throw new DatabaseException("El artículo no existe.");
                }

                if (!active) {
                    throw new DatabaseException("El artículo está desactivado.");
                }

                BigDecimal updated;

                if (type.equals("SALIDA")) {
                    if (previous.compareTo(quantity) < 0) {
                        throw new DatabaseException("Stock insuficiente.");
                    }
                    updated = previous.subtract(quantity);
                } else {
                    updated = previous.add(quantity);
                }

                if (updated.compareTo(MAX_STOCK) > 0) {
                    throw new DatabaseException("El stock supera el límite permitido.");
                }

                UUID movementUuid = UUID.randomUUID();

                setStock(c, itemUuid, updated);
                addMovement(c, movementUuid, itemUuid, type, quantity, previous, updated,
                        description, staffUuid, orderUuid);

                return new InventoryMovementResult(movementUuid, previous, updated);
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al registrar movimiento: " + e.getMessage()
            );
        }
    }

    public List<InventoryMovement> getMovements(UUID itemUuid) {

        String sql = "SELECT m.movement_uuid, m.item_uuid, i.item_name, i.item_code, m.movement_type, "
                + "m.movement_quantity, m.movement_previous_stock, m.movement_new_stock, "
                + "m.movement_description, m.staff_uuid, m.order_uuid, m.movement_created_at "
                + "FROM inventory_movements m INNER JOIN inventory_items i ON i.item_uuid = m.item_uuid "
                + "WHERE ? IS NULL OR m.item_uuid = ? "
                + "ORDER BY m.movement_created_at DESC, m.movement_uuid DESC";

        List<InventoryMovement> movements = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setNullableUuid(stmt, 1, itemUuid);
            setNullableUuid(stmt, 2, itemUuid);

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
                            SqliteUtil.getDecimal(rs, "movement_quantity"),
                            SqliteUtil.getDecimal(rs, "movement_previous_stock"),
                            SqliteUtil.getDecimal(rs, "movement_new_stock"),
                            rs.getString("movement_description"),
                            getNullableUuid(rs, "staff_uuid"),
                            getNullableUuid(rs, "order_uuid"),
                            SqliteUtil.getDateTime(rs, "movement_created_at")
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

    public void deactivateItem(UUID itemUuid) {

        String sql = "UPDATE inventory_items SET item_active = 0, item_updated_at = ? "
                + "WHERE item_uuid = ? AND item_active = 1";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SqliteUtil.nowUtc());
            stmt.setString(2, itemUuid.toString());

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("Artículo no encontrado o inactivo.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al desactivar el artículo: " + e.getMessage()
            );
        }
    }

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
                SqliteUtil.getDecimal(rs, "item_stock"),
                SqliteUtil.getDecimal(rs, "item_min_stock"),
                SqliteUtil.getDecimal(rs, "item_purchase_price"),
                rs.getBoolean("item_active"),
                StockStatus.valueOf(
                        rs.getString("stock_status")
                )
        );
    }

    private void setNullableUuid(
            PreparedStatement stmt,
            int index,
            UUID uuid
    ) throws SQLException {
        SqliteUtil.setNullableString(stmt, index, uuid == null ? null : uuid.toString());
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

    private String uuidOrNull(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    private BigDecimal lockRecoverableStock(
            Connection conn,
            UUID itemUuid
    ) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT item_stock FROM inventory_items "
                        + "WHERE item_uuid = ? AND item_active = 1 AND item_recoverable = 1")) {
            stmt.setString(1, itemUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? SqliteUtil.getDecimal(rs, "item_stock") : null;
            }
        }
    }

    private void setStock(
            Connection conn,
            UUID itemUuid,
            BigDecimal stock
    ) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE inventory_items SET item_stock = ?, item_updated_at = ? WHERE item_uuid = ?")) {
            SqliteUtil.setDecimal(stmt, 1, stock);
            stmt.setString(2, SqliteUtil.nowUtc());
            stmt.setString(3, itemUuid.toString());
            stmt.executeUpdate();
        }
    }

    private void addMovement(
            Connection conn,
            UUID movementUuid,
            UUID itemUuid,
            String type,
            BigDecimal quantity,
            BigDecimal previous,
            BigDecimal updated,
            String description,
            UUID staffUuid,
            UUID orderUuid
    ) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO inventory_movements (movement_uuid, item_uuid, movement_type, "
                        + "movement_quantity, movement_previous_stock, movement_new_stock, "
                        + "movement_description, staff_uuid, order_uuid, movement_created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, movementUuid.toString());
            stmt.setString(2, itemUuid.toString());
            stmt.setString(3, type);
            SqliteUtil.setDecimal(stmt, 4, quantity);
            SqliteUtil.setDecimal(stmt, 5, previous);
            SqliteUtil.setDecimal(stmt, 6, updated);
            SqliteUtil.setNullableString(stmt, 7, description);
            setNullableUuid(stmt, 8, staffUuid);
            setNullableUuid(stmt, 9, orderUuid);
            stmt.setString(10, SqliteUtil.nowUtc());
            stmt.executeUpdate();
        }
    }

    private void addHistory(
            Connection conn,
            UUID equipmentUuid,
            String previousStatus,
            String newStatus,
            String clientDni,
            UUID staffUuid,
            UUID orderUuid,
            String description
    ) throws SQLException {

        SqliteUtil.update(
                conn,
                "INSERT INTO inventory_equipment_history (history_uuid, equipment_uuid, previous_status, "
                        + "new_status, client_dni, staff_uuid, order_uuid, description, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID().toString(), equipmentUuid.toString(), previousStatus, newStatus,
                clientDni, uuidOrNull(staffUuid), uuidOrNull(orderUuid), description, SqliteUtil.nowUtc()
        );
    }

    public UUID registerEquipment(
            UUID itemUuid,
            String serialNumber,
            UUID staffUuid
    ) {

        String serial = serialNumber == null ? "" : serialNumber.trim();

        try (Connection conn = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(conn, c -> {

                if (itemUuid == null || serial.isEmpty()) {
                    throw new DatabaseException("Artículo y número de serie obligatorios.");
                }

                BigDecimal previous = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT item_stock FROM inventory_items WHERE item_uuid = ? AND item_active = 1 "
                                + "AND item_recoverable = 1 AND item_unit = 'UNIDAD'")) {
                    stmt.setString(1, itemUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            previous = SqliteUtil.getDecimal(rs, "item_stock");
                        }
                    }
                }

                if (previous == null) {
                    throw new DatabaseException("Artículo recuperable inválido.");
                }

                if (previous.compareTo(MAX_STOCK) >= 0) {
                    throw new DatabaseException("Límite de stock alcanzado.");
                }

                BigDecimal updated = previous.add(BigDecimal.ONE);
                UUID equipmentUuid = UUID.randomUUID();

                SqliteUtil.update(
                        c,
                        "INSERT INTO inventory_equipment_units (equipment_uuid, item_uuid, serial_number, "
                                + "equipment_status) VALUES (?, ?, ?, 'AVAILABLE')",
                        equipmentUuid.toString(), itemUuid.toString(), serial
                );

                setStock(c, itemUuid, updated);

                addMovement(c, UUID.randomUUID(), itemUuid, "ENTRADA", BigDecimal.ONE, previous, updated,
                        "Registro de equipo: " + serial, staffUuid, null);

                addHistory(c, equipmentUuid, null, "AVAILABLE", null, staffUuid, null,
                        "Equipo registrado en almacén");

                return equipmentUuid;
            });

        } catch (SQLException e) {

            String message = e.getMessage();

            if (message != null && message.contains("UNIQUE")) {
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

    public void assignEquipment(
            UUID equipmentUuid,
            UUID staffUuid
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (equipmentUuid == null || staffUuid == null) {
                    throw new DatabaseException("Equipo y técnico obligatorios.");
                }

                String itemId = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT item_uuid FROM inventory_equipment_units "
                                + "WHERE equipment_uuid = ? AND equipment_status = 'AVAILABLE'")) {
                    stmt.setString(1, equipmentUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemId = rs.getString("item_uuid");
                        }
                    }
                }

                if (itemId == null) {
                    throw new DatabaseException("El equipo no está disponible.");
                }

                UUID itemUuid = UUID.fromString(itemId);
                BigDecimal previous = lockRecoverableStock(c, itemUuid);

                if (previous == null || previous.compareTo(BigDecimal.ONE) < 0) {
                    throw new DatabaseException("Stock insuficiente o artículo inactivo.");
                }

                SqliteUtil.update(
                        c,
                        "UPDATE inventory_equipment_units SET equipment_status = 'ASSIGNED', "
                                + "assigned_staff_uuid = ?, client_dni = NULL, installed_order_uuid = NULL, "
                                + "installed_at = NULL, recovered_at = NULL WHERE equipment_uuid = ?",
                        staffUuid.toString(), equipmentUuid.toString()
                );

                BigDecimal updated = previous.subtract(BigDecimal.ONE);

                setStock(c, itemUuid, updated);

                addMovement(c, UUID.randomUUID(), itemUuid, "SALIDA", BigDecimal.ONE, previous, updated,
                        "Equipo entregado a técnico", staffUuid, null);

                addHistory(c, equipmentUuid, "AVAILABLE", "ASSIGNED", null, staffUuid, null,
                        "Equipo asignado al técnico");
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al asignar equipo al técnico: " + e.getMessage()
            );
        }
    }

    public void installEquipment(
            UUID equipmentUuid,
            String clientDni,
            UUID orderUuid,
            UUID staffUuid
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (equipmentUuid == null || clientDni == null || orderUuid == null || staffUuid == null) {
                    throw new DatabaseException("Faltan datos de instalación.");
                }

                String itemId = null;
                String status = null;
                String assignedStaff = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT item_uuid, equipment_status, assigned_staff_uuid "
                                + "FROM inventory_equipment_units WHERE equipment_uuid = ?")) {
                    stmt.setString(1, equipmentUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemId = rs.getString("item_uuid");
                            status = rs.getString("equipment_status");
                            assignedStaff = rs.getString("assigned_staff_uuid");
                        }
                    }
                }

                if (itemId == null) {
                    throw new DatabaseException("Equipo no encontrado.");
                }

                if (!status.equals("AVAILABLE") && !status.equals("ASSIGNED")) {
                    throw new DatabaseException("El equipo no puede instalarse.");
                }

                if (status.equals("ASSIGNED")
                        && (assignedStaff == null || !assignedStaff.equalsIgnoreCase(staffUuid.toString()))) {
                    throw new DatabaseException("El equipo pertenece a otro técnico.");
                }

                if (!SqliteUtil.exists(c, "SELECT 1 FROM clients WHERE client_dni = ?", clientDni)) {
                    throw new DatabaseException("Cliente no encontrado.");
                }

                if (!SqliteUtil.exists(c, "SELECT 1 FROM orders WHERE order_id = ?", orderUuid.toString())) {
                    throw new DatabaseException("Orden no encontrada.");
                }

                UUID itemUuid = UUID.fromString(itemId);

                if (status.equals("AVAILABLE")) {
                    BigDecimal previous = lockRecoverableStock(c, itemUuid);

                    if (previous == null || previous.compareTo(BigDecimal.ONE) < 0) {
                        throw new DatabaseException("Stock insuficiente.");
                    }

                    BigDecimal updated = previous.subtract(BigDecimal.ONE);

                    setStock(c, itemUuid, updated);

                    addMovement(c, UUID.randomUUID(), itemUuid, "SALIDA", BigDecimal.ONE, previous, updated,
                            "Instalación directa desde almacén", staffUuid, orderUuid);
                }

                SqliteUtil.update(
                        c,
                        "UPDATE inventory_equipment_units SET equipment_status = 'INSTALLED', client_dni = ?, "
                                + "assigned_staff_uuid = NULL, installed_order_uuid = ?, installed_at = ?, "
                                + "recovered_at = NULL WHERE equipment_uuid = ?",
                        clientDni, orderUuid.toString(), SqliteUtil.nowUtc(), equipmentUuid.toString()
                );

                addHistory(c, equipmentUuid, status, "INSTALLED", clientDni, staffUuid, orderUuid,
                        "Equipo instalado en domicilio");
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al instalar equipo: " + e.getMessage()
            );
        }
    }

    public void recoverEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (equipmentUuid == null || staffUuid == null) {
                    throw new DatabaseException("Equipo y técnico obligatorios.");
                }

                String clientDni = null;
                String orderId = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT client_dni, installed_order_uuid FROM inventory_equipment_units "
                                + "WHERE equipment_uuid = ? AND equipment_status = 'INSTALLED'")) {
                    stmt.setString(1, equipmentUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            clientDni = rs.getString("client_dni");
                            orderId = rs.getString("installed_order_uuid");
                        }
                    }
                }

                if (clientDni == null) {
                    throw new DatabaseException("El equipo no está instalado.");
                }

                SqliteUtil.update(
                        c,
                        "UPDATE inventory_equipment_units SET equipment_status = 'MAINTENANCE', "
                                + "client_dni = NULL, assigned_staff_uuid = ?, installed_order_uuid = NULL, "
                                + "installed_at = NULL, recovered_at = ? WHERE equipment_uuid = ?",
                        staffUuid.toString(), SqliteUtil.nowUtc(), equipmentUuid.toString()
                );

                addHistory(c, equipmentUuid, "INSTALLED", "MAINTENANCE", clientDni, staffUuid,
                        orderId == null ? null : UUID.fromString(orderId),
                        description != null ? description : "Equipo recuperado del cliente");
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al recuperar equipo: " + e.getMessage()
            );
        }
    }

    public void releaseEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {

                if (equipmentUuid == null || staffUuid == null) {
                    throw new DatabaseException("Equipo y responsable obligatorios.");
                }

                String itemId = null;
                String previousStatus = null;

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT item_uuid, equipment_status FROM inventory_equipment_units "
                                + "WHERE equipment_uuid = ?")) {
                    stmt.setString(1, equipmentUuid.toString());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemId = rs.getString("item_uuid");
                            previousStatus = rs.getString("equipment_status");
                        }
                    }
                }

                if (itemId == null) {
                    throw new DatabaseException("Equipo no encontrado.");
                }

                if (!previousStatus.equals("ASSIGNED") && !previousStatus.equals("MAINTENANCE")) {
                    throw new DatabaseException("El equipo no puede devolverse.");
                }

                UUID itemUuid = UUID.fromString(itemId);
                BigDecimal previous = lockRecoverableStock(c, itemUuid);

                if (previous == null) {
                    throw new DatabaseException("Artículo inactivo o inválido.");
                }

                if (previous.compareTo(MAX_STOCK) >= 0) {
                    throw new DatabaseException("Límite de stock alcanzado.");
                }

                SqliteUtil.update(
                        c,
                        "UPDATE inventory_equipment_units SET equipment_status = 'AVAILABLE', "
                                + "assigned_staff_uuid = NULL, client_dni = NULL, installed_order_uuid = NULL, "
                                + "installed_at = NULL WHERE equipment_uuid = ?",
                        equipmentUuid.toString()
                );

                BigDecimal updated = previous.add(BigDecimal.ONE);

                setStock(c, itemUuid, updated);

                addMovement(c, UUID.randomUUID(), itemUuid, "ENTRADA", BigDecimal.ONE, previous, updated,
                        "Equipo devuelto al almacén", staffUuid, null);

                addHistory(c, equipmentUuid, previousStatus, "AVAILABLE", null, staffUuid, null,
                        description != null ? description : "Equipo disponible nuevamente");
            });

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

        try (Connection conn = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(conn, c -> {

                if (itemUuid == null) {
                    throw new DatabaseException("El artículo es obligatorio.");
                }

                if (quantity <= 0) {
                    throw new DatabaseException("La cantidad debe ser mayor a cero.");
                }

                BigDecimal previous = lockRecoverableStock(c, itemUuid);

                if (previous == null) {
                    throw new DatabaseException("Artículo recuperable no encontrado o inactivo.");
                }

                BigDecimal amount = BigDecimal.valueOf(quantity);

                if (previous.compareTo(amount) < 0) {
                    throw new DatabaseException("Stock insuficiente.");
                }

                List<UUID> withdrawn = new ArrayList<>();

                try (PreparedStatement stmt = c.prepareStatement(
                        "SELECT equipment_uuid FROM inventory_equipment_units "
                                + "WHERE item_uuid = ? AND equipment_status = 'AVAILABLE' "
                                + "ORDER BY equipment_uuid LIMIT ?")) {
                    stmt.setString(1, itemUuid.toString());
                    stmt.setInt(2, quantity);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            withdrawn.add(UUID.fromString(rs.getString("equipment_uuid")));
                        }
                    }
                }

                if (withdrawn.size() != quantity) {
                    throw new DatabaseException("No hay suficientes equipos disponibles.");
                }

                for (UUID equipmentUuid : withdrawn) {
                    SqliteUtil.update(
                            c,
                            "UPDATE inventory_equipment_units SET equipment_status = 'RETIRED' "
                                    + "WHERE equipment_uuid = ?",
                            equipmentUuid.toString()
                    );
                }

                BigDecimal updated = previous.subtract(amount);
                UUID movementUuid = UUID.randomUUID();
                String text = description != null ? description : "Salida automática de equipos";

                setStock(c, itemUuid, updated);

                addMovement(c, movementUuid, itemUuid, "SALIDA", amount, previous, updated,
                        text, staffUuid, null);

                for (UUID equipmentUuid : withdrawn) {
                    addHistory(c, equipmentUuid, "AVAILABLE", "RETIRED", null, staffUuid, null,
                            description != null ? description : "Salida automática de inventario");
                }

                return new InventoryMovementResult(movementUuid, previous, updated);
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al retirar equipos: " + e.getMessage()
            );
        }
    }

    public List<InventoryEquipment> getTechnicianEquipment(UUID staffUuid) {

        String sql = "SELECT e.equipment_uuid, e.serial_number, e.equipment_status, "
                + "i.item_uuid, i.item_name, i.item_code "
                + "FROM inventory_equipment_units e INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid "
                + "WHERE e.assigned_staff_uuid = ? AND e.equipment_status IN ('ASSIGNED', 'MAINTENANCE') "
                + "ORDER BY i.item_name";

        List<InventoryEquipment> equipment = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        String sql = "SELECT e.equipment_uuid, e.serial_number, e.equipment_status, e.installed_at, "
                + "e.installed_order_uuid, i.item_uuid, i.item_name, i.item_code "
                + "FROM inventory_equipment_units e INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid "
                + "WHERE e.client_dni = ? AND e.equipment_status = 'INSTALLED' "
                + "ORDER BY e.installed_at DESC";

        List<InventoryEquipment> equipment = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
            installedAt = SqliteUtil.getDateTime(rs, "installed_at");

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
}
