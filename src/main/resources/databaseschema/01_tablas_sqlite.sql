/* =============================================================================
   SERVINET - 01_tablas_sqlite.sql   (versión SQLite de 01_tablas_server.sql)
   EJECUTAR PRIMERO (luego 02_procedimientos_sqlite.sql).

   - Se puede correr varias veces: cada objeto usa IF NOT EXISTS.
   - Ya viene en orden de dependencias (FK).
   - No hay CREATE DATABASE / USE: en SQLite la base ES el archivo .db.

   EQUIVALENCIAS USADAS
     UNIQUEIDENTIFIER      -> TEXT (UUID en minúsculas, 36 caracteres)
     VARCHAR/NVARCHAR(n)   -> TEXT + CHECK (length(col) <= n)
     VARCHAR(MAX)          -> TEXT
     VARBINARY(MAX)        -> BLOB
     BIT                   -> INTEGER + CHECK (col IN (0,1))
     INT / BIGINT          -> INTEGER
     DECIMAL(p,s)          -> DECIMAL(p,s)  (SQLite lo trata como NUMERIC)
     DATETIME2             -> TEXT en formato 'YYYY-MM-DD HH:MM:SS.SSS'
     SYSDATETIME()         -> strftime(... 'now','localtime')
     SYSUTCDATETIME()      -> strftime(... 'now')
     NEWID/NEWSEQUENTIALID -> DEFAULT con UUID v4 generado con randomblob()
     SEQUENCE              -> tabla con AUTOINCREMENT (seq_inventory_code)
     Collation de SQL Server (no distingue mayúsculas) -> COLLATE NOCASE en
     las columnas con UNIQUE o que se buscan por texto (email, display, ...).

   NOTAS IMPORTANTES
     * SQLite solo respeta las FOREIGN KEY si cada conexión ejecuta
       PRAGMA foreign_keys = ON;  (en JDBC: jdbc:sqlite:servinet.db?foreign_keys=on)
     * El tipo PermissionListType (tabla como parámetro) no existe en SQLite;
       sp_UpdateRole recibe los permisos como texto separado por comas.
     * Los ALTER TABLE de compatibilidad de announce no se portan: aquí la
       tabla ya nace con author_uuid, image y update_at.
     * Las secciones marcadas "RECONSTRUIDA" tampoco estaban en tu script
       original de SQL Server (se deducen de los procedimientos). Revísalas.
     * En tecnicos_logs y atencion_logs se agregó DEFAULT al UUID, porque en
       SQL Server lo generaba el procedimiento con NEWID().
   ============================================================================= */

PRAGMA foreign_keys = ON;


/* ============================ 1. ROLES Y PERMISOS ========================= */

CREATE TABLE IF NOT EXISTS roles (
    uuid     TEXT NOT NULL,
    name     TEXT NOT NULL COLLATE NOCASE CHECK (length(name) <= 50),
    hexColor TEXT NOT NULL CHECK (length(hexColor) <= 20),
    CONSTRAINT PK_roles      PRIMARY KEY (uuid),
    CONSTRAINT UQ_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_uuid  TEXT NOT NULL,
    permission TEXT NOT NULL CHECK (length(permission) <= 50),
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_uuid, permission),
    CONSTRAINT FK_role_permissions FOREIGN KEY (role_uuid) REFERENCES roles(uuid)
);

/* PermissionListType: no existe en SQLite (ver notas arriba). */


/* ============================ 2. USUARIOS (STAFF) ========================= */

CREATE TABLE IF NOT EXISTS users (
    uuid          TEXT NOT NULL,
    display       TEXT NOT NULL COLLATE NOCASE CHECK (length(display) <= 50),
    email         TEXT NOT NULL COLLATE NOCASE CHECK (length(email) <= 50),
    role          TEXT NOT NULL,
    password_hash TEXT NOT NULL CHECK (length(password_hash) <= 255),
    create_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    perfil_img    BLOB,
    phone         TEXT NOT NULL DEFAULT '' CHECK (length(phone) <= 9),
    CONSTRAINT PK_uuid             PRIMARY KEY (uuid),
    CONSTRAINT UQ_usuarios_display UNIQUE (display),
    CONSTRAINT UQ_usuarios_email   UNIQUE (email),
    CONSTRAINT FK_users            FOREIGN KEY (role) REFERENCES roles(uuid)
);

CREATE TABLE IF NOT EXISTS users_session (
    hardware_id TEXT NOT NULL CHECK (length(hardware_id) <= 50),
    user_uuid   TEXT NOT NULL,
    expires_at  TEXT NOT NULL
        DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime', '+2 days')),
    CONSTRAINT PK_uuidhardware PRIMARY KEY (hardware_id),
    CONSTRAINT FK_user_uuid    FOREIGN KEY (user_uuid) REFERENCES users(uuid)
);

CREATE TABLE IF NOT EXISTS api_sessions (
    sessions_id      TEXT NOT NULL,
    user_uuid        TEXT NOT NULL,
    access_token_id  TEXT NOT NULL,
    refresh_token_id TEXT NOT NULL,
    user_agent       TEXT CHECK (length(user_agent) <= 500),
    user_ip          TEXT CHECK (length(user_ip) <= 45),
    created_at       TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    expires_at       TEXT
        DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime', '+60 minutes')),
    revoked_at       TEXT,
    CONSTRAINT PK_api_sessions      PRIMARY KEY (sessions_id),
    CONSTRAINT FK_api_sessions_user FOREIGN KEY (user_uuid) REFERENCES users(uuid)
);


/* ============================ 3. CONFIGURACIÓN APP ======================== */

CREATE TABLE IF NOT EXISTS appGeneral (
    app_name  TEXT NOT NULL CHECK (length(app_name) <= 12),
    app_image BLOB
);


/* ============================ 4. PLANES =================================== */

CREATE TABLE IF NOT EXISTS plans (
    plan_id       TEXT NOT NULL,
    plan_name     TEXT NOT NULL CHECK (length(plan_name) <= 8),
    plan_created  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    plan_speed    INTEGER NOT NULL,
    plan_price    DECIMAL(10, 2) NOT NULL,
    plan_ispromo  INTEGER NOT NULL DEFAULT 0 CHECK (plan_ispromo  IN (0, 1)),
    plan_isactive INTEGER NOT NULL DEFAULT 0 CHECK (plan_isactive IN (0, 1)),
    CONSTRAINT PK_plan_id PRIMARY KEY (plan_id)
);


/* ============================ 5. ANTENAS ================================== */

CREATE TABLE IF NOT EXISTS antennas (
    uuid                  TEXT NOT NULL,
    priority              INTEGER NOT NULL DEFAULT 0,
    name                  TEXT NOT NULL COLLATE NOCASE CHECK (length(name) <= 100),
    for_repair            INTEGER NOT NULL CHECK (for_repair      IN (0, 1)),
    for_maintenance       INTEGER NOT NULL CHECK (for_maintenance IN (0, 1)),
    date_create           TEXT NOT NULL,
    date_last_maintenance TEXT,
    count_days_on         INTEGER NOT NULL DEFAULT 0,
    count_days_off        INTEGER NOT NULL DEFAULT 0,
    image                 BLOB,
    status                TEXT NOT NULL CHECK (length(status) <= 30),
    latitude              DECIMAL(10, 7),
    longitude             DECIMAL(10, 7),
    CONSTRAINT PK_antennas PRIMARY KEY (uuid),
    CONSTRAINT UQ_name     UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS maintenance (
    uuid           TEXT NOT NULL,
    antenna_uuid   TEXT NOT NULL,
    technical_uuid TEXT NOT NULL,
    description    TEXT NOT NULL CHECK (length(description) <= 200),
    date_start     TEXT NOT NULL,
    date_ended     TEXT,
    image          BLOB,
    CONSTRAINT PK_maintenance    PRIMARY KEY (uuid),
    CONSTRAINT FK_antenna_uuid   FOREIGN KEY (antenna_uuid)   REFERENCES antennas(uuid),
    CONSTRAINT FK_technical_uuid FOREIGN KEY (technical_uuid) REFERENCES users(uuid)
);


/* ============================ 6. CLIENTES ================================= */
/* client_status: ACTIVO, EN_INSTALACION, SUSPENDIDO, RETIRADO, EN_DESINSTALACION */

CREATE TABLE IF NOT EXISTS clients (
    client_dni       TEXT NOT NULL CHECK (length(client_dni) <= 8),
    client_name      TEXT NOT NULL COLLATE NOCASE CHECK (length(client_name) <= 100),
    client_last_name TEXT NOT NULL COLLATE NOCASE CHECK (length(client_last_name) <= 100),
    client_phone     TEXT NOT NULL CHECK (length(client_phone) <= 9),
    client_address   TEXT NOT NULL CHECK (length(client_address) <= 100),
    client_email     TEXT NOT NULL CHECK (length(client_email) <= 50),
    client_whatsapp  TEXT NOT NULL CHECK (length(client_whatsapp) <= 9),
    client_register  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    client_status    TEXT NOT NULL DEFAULT 'ACTIVO',
    longitud         TEXT NOT NULL CHECK (length(longitud) <= 100),
    latitud          TEXT NOT NULL CHECK (length(latitud) <= 100),
    CONSTRAINT PK_clients PRIMARY KEY (client_dni),
    CONSTRAINT UQ_client_name_last_name UNIQUE (client_name, client_last_name),
    CONSTRAINT CK_clients_status CHECK (
        client_status IN (
            'ACTIVO',
            'EN_INSTALACION',
            'SUSPENDIDO',
            'RETIRADO',
            'EN_DESINSTALACION'
        )
    )
);

CREATE TABLE IF NOT EXISTS antennasClient (
    antenna_uuid TEXT NOT NULL,
    client_dni   TEXT NOT NULL,
    CONSTRAINT PK_antennasClient PRIMARY KEY (antenna_uuid, client_dni),
    CONSTRAINT FK_antennasClient_Antennas FOREIGN KEY (antenna_uuid) REFERENCES antennas(uuid),
    CONSTRAINT FK_antennasClient_Clients  FOREIGN KEY (client_dni)   REFERENCES clients(client_dni)
);


/* ============================ 7. PEDIDOS E INSTALACIONES ================== */

CREATE TABLE IF NOT EXISTS orders (
    order_id              TEXT NOT NULL,
    order_antenna_connect TEXT,
    order_plan_connect    TEXT NOT NULL,
    order_created         TEXT,
    order_status          TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (length(order_status) <= 30),
    order_client_dni      TEXT NOT NULL,
    CONSTRAINT PK_order                 PRIMARY KEY (order_id),
    CONSTRAINT FK_order_plan_connect    FOREIGN KEY (order_plan_connect)    REFERENCES plans(plan_id),
    CONSTRAINT FK_order_antenna_connect FOREIGN KEY (order_antenna_connect) REFERENCES antennas(uuid),
    CONSTRAINT FK_orders_clients        FOREIGN KEY (order_client_dni)      REFERENCES clients(client_dni)
);

CREATE TABLE IF NOT EXISTS orders_report (
    order_id           TEXT NOT NULL,
    report_client_dni  TEXT NOT NULL,
    report_observation TEXT,
    img_router         BLOB,
    img_conexion       BLOB,
    img_mini_antenna   BLOB,
    router_password    TEXT NOT NULL CHECK (length(router_password) <= 255),
    report_create      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    report_status      TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (length(report_status) <= 30),
    report_ended       TEXT,
    CONSTRAINT PK_order_id_report   PRIMARY KEY (order_id),
    CONSTRAINT FK_order_id_report   FOREIGN KEY (order_id)          REFERENCES orders(order_id),
    CONSTRAINT FK_report_client_dni FOREIGN KEY (report_client_dni) REFERENCES clients(client_dni)
);

CREATE TABLE IF NOT EXISTS tecnicos_logs (
    servicio_uuid    TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    staff_uuid       TEXT NOT NULL,
    client_dni       TEXT CHECK (length(client_dni) <= 8),
    accion_tipo      TEXT NOT NULL,
    order_id         TEXT NOT NULL,
    description_tipo TEXT CHECK (length(description_tipo) <= 200),
    current_order    INTEGER NOT NULL DEFAULT 1 CHECK (current_order IN (0, 1)),
    CONSTRAINT CK_accion_tipo        CHECK (accion_tipo IN ('INTSTALACION')),
    CONSTRAINT PK_atencion_uuid      PRIMARY KEY (servicio_uuid),
    CONSTRAINT FK_staff_uuid_TECNICO FOREIGN KEY (staff_uuid) REFERENCES users(uuid),
    CONSTRAINT FK_order_id_TECNICO   FOREIGN KEY (order_id)   REFERENCES orders(order_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS UX_Tecnico_CurrentOrder
    ON tecnicos_logs(staff_uuid)
    WHERE current_order = 1;

CREATE UNIQUE INDEX IF NOT EXISTS UX_Order_CurrentTechnician
    ON tecnicos_logs(order_id)
    WHERE current_order = 1;

CREATE TABLE IF NOT EXISTS atencion_logs (
    atencion_uuid    TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    staff_uuid       TEXT NOT NULL,
    client_dni       TEXT CHECK (length(client_dni) <= 8),
    client_name      TEXT NOT NULL CHECK (length(client_name) <= 100),
    client_last_name TEXT NOT NULL CHECK (length(client_last_name) <= 100),
    atencion_tipo    TEXT NOT NULL,
    valoracion       INTEGER NOT NULL,
    description_tipo TEXT CHECK (length(description_tipo) <= 200),
    CONSTRAINT CK_atencion_tipo CHECK (atencion_tipo IN (
        'VENTA',
        'INFORME',
        'SOPORTE',
        'SUSPENCION',
        'COBRO',
        'ATENCION_CLIENTE'
    )),
    CONSTRAINT PK_atencion_uuid_atencion PRIMARY KEY (atencion_uuid),
    CONSTRAINT FK_staff_uuid_ATENCION    FOREIGN KEY (staff_uuid) REFERENCES users(uuid)
);


/* ============================ 8. PAGOS ==================================== */
/* RECONSTRUIDA: no estaba en tu script. Columnas y tipos deducidos de los
   procedimientos sp_*Payment*. Si ya existe, no hace nada. */

CREATE TABLE IF NOT EXISTS payments (
    payment_id          TEXT NOT NULL,
    client_dni          TEXT NOT NULL CHECK (length(client_dni) <= 8),
    payment_amount      DECIMAL(10, 2) NOT NULL,
    payment_date_paid   TEXT,
    payment_method      TEXT CHECK (length(payment_method) <= 30),
    payment_ispaid      INTEGER NOT NULL DEFAULT 0 CHECK (payment_ispaid IN (0, 1)),
    payment_image       BLOB,
    payment_description TEXT CHECK (length(payment_description) <= 255),
    payment_reference   TEXT CHECK (length(payment_reference) <= 100),
    payment_created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    CONSTRAINT PK_payments PRIMARY KEY (payment_id)
);


/* ============================ 9. ANUNCIOS ================================= */
/* announce_likes y announce_comments son RECONSTRUIDAS (igual que en el
   original no llevan FOREIGN KEY). */

CREATE TABLE IF NOT EXISTS announce (
    uuid        TEXT NOT NULL,
    priority    TEXT NOT NULL CHECK (length(priority) <= 50),
    title       TEXT NOT NULL CHECK (length(title) <= 20),
    description TEXT NOT NULL CHECK (length(description) <= 150),
    author      TEXT NOT NULL CHECK (length(author) <= 20),
    author_uuid TEXT,
    image       BLOB,
    sendAt      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    update_at   TEXT,
    CONSTRAINT PK_announce PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS announce_likes (
    announce_uuid TEXT NOT NULL,
    user_uuid     TEXT NOT NULL,
    CONSTRAINT PK_announce_likes PRIMARY KEY (announce_uuid, user_uuid)
);

CREATE TABLE IF NOT EXISTS announce_comments (
    uuid          TEXT NOT NULL,
    announce_uuid TEXT NOT NULL,
    user_uuid     TEXT NOT NULL,
    content       TEXT NOT NULL CHECK (length(content) <= 500),
    createAt      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
    CONSTRAINT PK_announce_comments PRIMARY KEY (uuid)
);


/* ============================ 10. LOGS DE USUARIOS ======================== */
/* RECONSTRUIDA: deducida de sp_InsertLog, sp_GetLogs, sp_GetActivityByDay y
   sp_GetMeTecStats. (Sin PK, igual que el original; SQLite usa rowid.) */

CREATE TABLE IF NOT EXISTS users_logs (
    user_uuid   TEXT NOT NULL,
    user_name   TEXT NOT NULL CHECK (length(user_name) <= 255),
    log_type    TEXT NOT NULL CHECK (length(log_type) <= 100),
    information TEXT,
    create_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'))
);


/* ============================ 11. INVENTARIO ============================== */

/* Reemplazo de dbo.seq_inventory_code (SEQUENCE): cada INSERT genera el
   siguiente número (1, 2, 3...). */
CREATE TABLE IF NOT EXISTS seq_inventory_code (
    id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE IF NOT EXISTS inventory_items (
    item_uuid           TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    item_code           TEXT NOT NULL COLLATE NOCASE CHECK (length(item_code) <= 40),
    item_name           TEXT NOT NULL CHECK (length(item_name) <= 150),
    item_description    TEXT CHECK (length(item_description) <= 500),
    item_category       TEXT NOT NULL CHECK (length(item_category) <= 60),
    item_brand          TEXT CHECK (length(item_brand) <= 100),
    item_model          TEXT CHECK (length(item_model) <= 100),
    item_recoverable    INTEGER NOT NULL DEFAULT 0 CHECK (item_recoverable IN (0, 1)),
    item_unit           TEXT NOT NULL DEFAULT 'UNIDAD',
    item_stock          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    item_min_stock      DECIMAL(12, 2) NOT NULL DEFAULT 5,
    item_purchase_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    item_active         INTEGER NOT NULL DEFAULT 1 CHECK (item_active IN (0, 1)),
    item_created_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now')),
    item_updated_at     TEXT,
    CONSTRAINT PK_inventory_items     PRIMARY KEY (item_uuid),
    CONSTRAINT UQ_inventory_item_code UNIQUE (item_code),
    CONSTRAINT CK_inventory_stock     CHECK (item_stock >= 0),
    CONSTRAINT CK_inventory_min_stock CHECK (item_min_stock >= 0),
    CONSTRAINT CK_inventory_price     CHECK (item_purchase_price >= 0),
    CONSTRAINT CK_inventory_unit      CHECK (item_unit IN ('UNIDAD', 'METRO', 'ROLLO', 'CAJA', 'PAQUETE')),
    CONSTRAINT CK_inventory_name      CHECK (length(trim(item_name)) > 0),
    CONSTRAINT CK_inventory_code      CHECK (length(trim(item_code)) > 0),
    CONSTRAINT CK_inventory_category  CHECK (length(trim(item_category)) > 0)
);

/* En los CHECK de saldo se redondea a 2 decimales porque SQLite guarda los
   DECIMAL como números de punto flotante (evita fallos tipo 0.1 + 0.2). */
CREATE TABLE IF NOT EXISTS inventory_movements (
    movement_uuid           TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    item_uuid               TEXT NOT NULL,
    movement_type           TEXT NOT NULL,
    movement_quantity       DECIMAL(12, 2) NOT NULL,
    movement_previous_stock DECIMAL(12, 2) NOT NULL,
    movement_new_stock      DECIMAL(12, 2) NOT NULL,
    movement_description    TEXT CHECK (length(movement_description) <= 500),
    staff_uuid              TEXT,
    order_uuid              TEXT,
    movement_created_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now')),
    CONSTRAINT PK_inventory_movements      PRIMARY KEY (movement_uuid),
    CONSTRAINT FK_inventory_movement_item  FOREIGN KEY (item_uuid)  REFERENCES inventory_items(item_uuid),
    CONSTRAINT FK_inventory_movement_staff FOREIGN KEY (staff_uuid) REFERENCES users(uuid),
    CONSTRAINT FK_inventory_movement_order FOREIGN KEY (order_uuid) REFERENCES orders(order_id),
    CONSTRAINT CK_inventory_movement_type  CHECK (movement_type IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    CONSTRAINT CK_inventory_movement_quantity CHECK (movement_quantity > 0),
    CONSTRAINT CK_inventory_movement_stocks CHECK (
        movement_previous_stock >= 0
        AND movement_new_stock >= 0
    ),
    CONSTRAINT CK_inventory_movement_balance CHECK (
        (movement_type = 'ENTRADA'
            AND round(movement_new_stock, 2) = round(movement_previous_stock + movement_quantity, 2))
        OR
        (movement_type = 'SALIDA'
            AND round(movement_new_stock, 2) = round(movement_previous_stock - movement_quantity, 2))
        OR
        (movement_type = 'AJUSTE'
            AND round(abs(movement_new_stock - movement_previous_stock), 2) = round(movement_quantity, 2))
    )
);

CREATE TABLE IF NOT EXISTS inventory_equipment_units (
    equipment_uuid       TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    item_uuid            TEXT NOT NULL,
    serial_number        TEXT NOT NULL COLLATE NOCASE CHECK (length(serial_number) <= 100),
    equipment_status     TEXT NOT NULL DEFAULT 'AVAILABLE',
    client_dni           TEXT CHECK (length(client_dni) <= 8),
    assigned_staff_uuid  TEXT,
    installed_at         TEXT,
    installed_order_uuid TEXT,
    recovered_at         TEXT,
    CONSTRAINT PK_inventory_equipment_units PRIMARY KEY (equipment_uuid),
    CONSTRAINT UQ_equipment_serial          UNIQUE (serial_number),
    CONSTRAINT FK_equipment_item            FOREIGN KEY (item_uuid)            REFERENCES inventory_items(item_uuid),
    CONSTRAINT FK_equipment_client          FOREIGN KEY (client_dni)           REFERENCES clients(client_dni),
    CONSTRAINT FK_equipment_staff           FOREIGN KEY (assigned_staff_uuid)  REFERENCES users(uuid),
    CONSTRAINT FK_equipment_installed_order FOREIGN KEY (installed_order_uuid) REFERENCES orders(order_id),
    CONSTRAINT CK_equipment_status CHECK (equipment_status IN (
        'AVAILABLE',
        'ASSIGNED',
        'INSTALLED',
        'MAINTENANCE',
        'DAMAGED',
        'LOST',
        'RETIRED'
    ))
);

CREATE TABLE IF NOT EXISTS inventory_equipment_history (
    history_uuid    TEXT NOT NULL
        DEFAULT (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
              || substr(lower(hex(randomblob(2))), 2) || '-'
              || substr('89ab', abs(random()) % 4 + 1, 1)
              || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6)))),
    equipment_uuid  TEXT NOT NULL,
    previous_status TEXT,
    new_status      TEXT NOT NULL,
    client_dni      TEXT CHECK (length(client_dni) <= 8),
    staff_uuid      TEXT,
    order_uuid      TEXT,
    description     TEXT CHECK (length(description) <= 500),
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f', 'now')),
    CONSTRAINT PK_inventory_equipment_history PRIMARY KEY (history_uuid),
    CONSTRAINT FK_equipment_history_unit   FOREIGN KEY (equipment_uuid) REFERENCES inventory_equipment_units(equipment_uuid),
    CONSTRAINT FK_equipment_history_client FOREIGN KEY (client_dni)     REFERENCES clients(client_dni),
    CONSTRAINT FK_equipment_history_staff  FOREIGN KEY (staff_uuid)     REFERENCES users(uuid),
    CONSTRAINT FK_equipment_history_order  FOREIGN KEY (order_uuid)     REFERENCES orders(order_id)
);

CREATE INDEX IF NOT EXISTS IX_equipment_status_item
    ON inventory_equipment_units(equipment_status, item_uuid);

CREATE INDEX IF NOT EXISTS IX_equipment_client
    ON inventory_equipment_units(client_dni)
    WHERE client_dni IS NOT NULL;

CREATE INDEX IF NOT EXISTS IX_equipment_history_unit
    ON inventory_equipment_history(equipment_uuid, created_at DESC);
