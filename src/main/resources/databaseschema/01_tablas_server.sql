/* =============================================================================
   SERVINET - 01_tablas_server.sql
   EJECUTAR PRIMERO (luego 02_procedimientos_server.sql).

   - Se puede correr varias veces: cada objeto se crea solo si no existe.
   - Ya viene en orden de dependencias (FK), no hay que reordenar nada.
   - Contiene el estado FINAL de cada tabla (los ALTER del script original
     ya están integrados en el CREATE TABLE).
   - Las secciones marcadas "RECONSTRUIDA" no estaban en tu script original:
     las deduje de los procedimientos que las usan. Revísalas.
   ============================================================================= */

IF DB_ID(N'Servinet') IS NULL
    CREATE DATABASE Servinet;
GO

USE Servinet;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO


/* ============================ 1. ROLES Y PERMISOS ========================= */

IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.roles (
        uuid     UNIQUEIDENTIFIER NOT NULL,
        name     VARCHAR(50)      NOT NULL,
        hexColor VARCHAR(20)      NOT NULL,
        CONSTRAINT PK_roles      PRIMARY KEY (uuid),
        CONSTRAINT UQ_roles_name UNIQUE (name)
    );
END;
GO

IF OBJECT_ID(N'dbo.role_permissions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.role_permissions (
        role_uuid  UNIQUEIDENTIFIER NOT NULL,
        permission VARCHAR(50)      NOT NULL,
        CONSTRAINT PK_role_permissions PRIMARY KEY (role_uuid, permission),
        CONSTRAINT FK_role_permissions FOREIGN KEY (role_uuid) REFERENCES dbo.roles(uuid)
    );
END;
GO

/* RECONSTRUIDA: tipo de tabla que usa sp_UpdateRole (@permissions ... READONLY) */
IF TYPE_ID(N'dbo.PermissionListType') IS NULL
    CREATE TYPE dbo.PermissionListType AS TABLE (
        permission VARCHAR(50) NOT NULL
    );
GO


/* ============================ 2. USUARIOS (STAFF) ========================= */

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        uuid          UNIQUEIDENTIFIER NOT NULL,
        display       VARCHAR(50)      NOT NULL,
        email         VARCHAR(50)      NOT NULL,
        role          UNIQUEIDENTIFIER NOT NULL,
        password_hash VARCHAR(255)     NOT NULL,
        create_at     DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        perfil_img    VARBINARY(MAX)   NULL,
        phone         VARCHAR(9)       NOT NULL DEFAULT '',
        CONSTRAINT PK_uuid               PRIMARY KEY (uuid),
        CONSTRAINT UQ_usuarios_display   UNIQUE (display),
        CONSTRAINT UQ_usuarios_email     UNIQUE (email),
        CONSTRAINT FK_users              FOREIGN KEY (role) REFERENCES dbo.roles(uuid)
    );
END;
GO

IF OBJECT_ID(N'dbo.users_session', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users_session (
        hardware_id VARCHAR(50)      NOT NULL,
        user_uuid   UNIQUEIDENTIFIER NOT NULL,
        expires_at  DATETIME2        NOT NULL
            CONSTRAINT DF_users_session_expires_at DEFAULT DATEADD(DAY, 2, SYSDATETIME()),
        CONSTRAINT PK_uuidhardware PRIMARY KEY (hardware_id),
        CONSTRAINT FK_user_uuid    FOREIGN KEY (user_uuid) REFERENCES dbo.users(uuid)
    );
END;
GO

IF OBJECT_ID(N'dbo.api_sessions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.api_sessions (
        sessions_id      UNIQUEIDENTIFIER NOT NULL,
        user_uuid        UNIQUEIDENTIFIER NOT NULL,
        access_token_id  UNIQUEIDENTIFIER NOT NULL,
        refresh_token_id UNIQUEIDENTIFIER NOT NULL,
        user_agent       VARCHAR(500)     NULL,
        user_ip          VARCHAR(45)      NULL,
        created_at       DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        expires_at       DATETIME2        NULL
            CONSTRAINT DF_api_sessions_expires_at DEFAULT DATEADD(MINUTE, 60, SYSDATETIME()),
        revoked_at       DATETIME2        NULL,
        CONSTRAINT PK_api_sessions      PRIMARY KEY (sessions_id),
        CONSTRAINT FK_api_sessions_user FOREIGN KEY (user_uuid) REFERENCES dbo.users(uuid)
    );
END;
GO


/* ============================ 3. CONFIGURACIÓN APP ======================== */

IF OBJECT_ID(N'dbo.appGeneral', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.appGeneral (
        app_name  VARCHAR(12)    NOT NULL,
        app_image VARBINARY(MAX) NULL
    );
END;
GO


/* ============================ 4. PLANES =================================== */

IF OBJECT_ID(N'dbo.plans', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.plans (
        plan_id       UNIQUEIDENTIFIER NOT NULL,
        plan_name     VARCHAR(8)       NOT NULL,
        plan_created  DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        plan_speed    INT              NOT NULL,
        plan_price    DECIMAL(10, 2)   NOT NULL,
        plan_ispromo  BIT              NOT NULL DEFAULT 0,
        plan_isactive BIT              NOT NULL DEFAULT 0,
        CONSTRAINT PK_plan_id PRIMARY KEY (plan_id)
    );
END;
GO


/* ============================ 5. ANTENAS ================================== */

IF OBJECT_ID(N'dbo.antennas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.antennas (
        uuid                  UNIQUEIDENTIFIER NOT NULL,
        priority              INT              NOT NULL DEFAULT 0,
        name                  VARCHAR(100)     NOT NULL,
        for_repair            BIT              NOT NULL,
        for_maintenance       BIT              NOT NULL,
        date_create           DATETIME2        NOT NULL,
        date_last_maintenance DATETIME2        NULL,
        count_days_on         INT              NOT NULL DEFAULT 0,
        count_days_off        INT              NOT NULL DEFAULT 0,
        image                 VARBINARY(MAX)   NULL,
        status                VARCHAR(30)      NOT NULL,
        latitude              DECIMAL(10, 7)   NULL,
        longitude             DECIMAL(10, 7)   NULL,
        CONSTRAINT PK_antennas PRIMARY KEY (uuid),
        CONSTRAINT UQ_name     UNIQUE (name)
    );
END;
GO

IF OBJECT_ID(N'dbo.maintenance', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.maintenance (
        uuid           UNIQUEIDENTIFIER NOT NULL,
        antenna_uuid   UNIQUEIDENTIFIER NOT NULL,
        technical_uuid UNIQUEIDENTIFIER NOT NULL,
        description    VARCHAR(200)     NOT NULL,
        date_start     DATETIME2        NOT NULL,
        date_ended     DATETIME2        NULL,
        image          VARBINARY(MAX)   NULL,
        CONSTRAINT PK_maintenance   PRIMARY KEY (uuid),
        CONSTRAINT FK_antenna_uuid  FOREIGN KEY (antenna_uuid)   REFERENCES dbo.antennas(uuid),
        CONSTRAINT FK_technical_uuid FOREIGN KEY (technical_uuid) REFERENCES dbo.users(uuid)
    );
END;
GO


/* ============================ 6. CLIENTES ================================= */
/* client_status: ACTIVO, EN_INSTALACION, SUSPENDIDO, RETIRADO, EN_DESINSTALACION */

IF OBJECT_ID(N'dbo.clients', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.clients (
        client_dni       VARCHAR(8)   NOT NULL,
        client_name      VARCHAR(100) NOT NULL,
        client_last_name VARCHAR(100) NOT NULL,
        client_phone     VARCHAR(9)   NOT NULL,
        client_address   VARCHAR(100) NOT NULL,
        client_email     VARCHAR(50)  NOT NULL,
        client_whatsapp  VARCHAR(9)   NOT NULL,
        client_register  DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
        client_status    VARCHAR(20)  NOT NULL
            CONSTRAINT DF_clients_status DEFAULT 'ACTIVO',
        longitud         VARCHAR(100) NOT NULL,
        latitud          VARCHAR(100) NOT NULL,
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
END;
GO

IF OBJECT_ID(N'dbo.antennasClient', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.antennasClient (
        antenna_uuid UNIQUEIDENTIFIER NOT NULL,
        client_dni   VARCHAR(8)       NOT NULL,
        CONSTRAINT PK_antennasClient PRIMARY KEY (antenna_uuid, client_dni),
        CONSTRAINT FK_antennasClient_Antennas FOREIGN KEY (antenna_uuid) REFERENCES dbo.antennas(uuid),
        CONSTRAINT FK_antennasClient_Clients  FOREIGN KEY (client_dni)   REFERENCES dbo.clients(client_dni)
    );
END;
GO


/* ============================ 7. PEDIDOS E INSTALACIONES ================== */

IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders (
        order_id              UNIQUEIDENTIFIER NOT NULL,
        order_antenna_connect UNIQUEIDENTIFIER NULL,
        order_plan_connect    UNIQUEIDENTIFIER NOT NULL,
        order_created         DATETIME2        NULL,
        order_status          VARCHAR(30)      NOT NULL DEFAULT 'PENDIENTE',
        order_client_dni      VARCHAR(8)       NOT NULL,
        CONSTRAINT PK_order                 PRIMARY KEY (order_id),
        CONSTRAINT FK_order_plan_connect    FOREIGN KEY (order_plan_connect)    REFERENCES dbo.plans(plan_id),
        CONSTRAINT FK_order_antenna_connect FOREIGN KEY (order_antenna_connect) REFERENCES dbo.antennas(uuid),
        CONSTRAINT FK_orders_clients        FOREIGN KEY (order_client_dni)      REFERENCES dbo.clients(client_dni)
    );
END;
GO

IF OBJECT_ID(N'dbo.orders_report', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders_report (
        order_id           UNIQUEIDENTIFIER NOT NULL,
        report_client_dni  VARCHAR(8)       NOT NULL,
        report_observation VARCHAR(MAX)     NULL,
        img_router         VARBINARY(MAX)   NULL,
        img_conexion       VARBINARY(MAX)   NULL,
        img_mini_antenna   VARBINARY(MAX)   NULL,
        router_password    VARCHAR(255)     NOT NULL,
        report_create      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        report_status      VARCHAR(30)      NOT NULL DEFAULT 'PENDIENTE',
        report_ended       DATETIME2        NULL,
        CONSTRAINT PK_order_id_report       PRIMARY KEY (order_id),
        CONSTRAINT FK_order_id_report       FOREIGN KEY (order_id)          REFERENCES dbo.orders(order_id),
        CONSTRAINT FK_report_client_dni     FOREIGN KEY (report_client_dni) REFERENCES dbo.clients(client_dni)
    );
END;
GO

IF OBJECT_ID(N'dbo.tecnicos_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tecnicos_logs (
        servicio_uuid    UNIQUEIDENTIFIER NOT NULL,
        staff_uuid       UNIQUEIDENTIFIER NOT NULL,
        client_dni       VARCHAR(8)       NULL,
        accion_tipo      VARCHAR(30)      NOT NULL,
        order_id         UNIQUEIDENTIFIER NOT NULL,
        description_tipo VARCHAR(200)     NULL,
        current_order    BIT              NOT NULL DEFAULT 1,
        CONSTRAINT CK_accion_tipo         CHECK (accion_tipo IN ('INTSTALACION')),
        CONSTRAINT PK_atencion_uuid       PRIMARY KEY (servicio_uuid),
        CONSTRAINT FK_staff_uuid_TECNICO  FOREIGN KEY (staff_uuid) REFERENCES dbo.users(uuid),
        CONSTRAINT FK_order_id_TECNICO    FOREIGN KEY (order_id)   REFERENCES dbo.orders(order_id)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = N'UX_Tecnico_CurrentOrder'
                 AND object_id = OBJECT_ID(N'dbo.tecnicos_logs'))
    CREATE UNIQUE INDEX UX_Tecnico_CurrentOrder
    ON dbo.tecnicos_logs(staff_uuid)
    WHERE current_order = 1;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = N'UX_Order_CurrentTechnician'
                 AND object_id = OBJECT_ID(N'dbo.tecnicos_logs'))
    CREATE UNIQUE INDEX UX_Order_CurrentTechnician
    ON dbo.tecnicos_logs(order_id)
    WHERE current_order = 1;
GO

IF OBJECT_ID(N'dbo.atencion_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.atencion_logs (
        atencion_uuid    UNIQUEIDENTIFIER NOT NULL,
        staff_uuid       UNIQUEIDENTIFIER NOT NULL,
        client_dni       VARCHAR(8)       NULL,
        client_name      VARCHAR(100)     NOT NULL,
        client_last_name VARCHAR(100)     NOT NULL,
        atencion_tipo    VARCHAR(30)      NOT NULL,
        valoracion       INT              NOT NULL,
        description_tipo VARCHAR(200)     NULL,
        CONSTRAINT CK_atencion_tipo CHECK (atencion_tipo IN (
            'VENTA',
            'INFORME',
            'SOPORTE',
            'SUSPENCION',
            'COBRO',
            'ATENCION_CLIENTE'
        )),
        CONSTRAINT PK_atencion_uuid_atencion PRIMARY KEY (atencion_uuid),
        CONSTRAINT FK_staff_uuid_ATENCION    FOREIGN KEY (staff_uuid) REFERENCES dbo.users(uuid)
    );
END;
GO


/* ============================ 8. PAGOS ==================================== */
/* RECONSTRUIDA: no estaba en tu script. Columnas y tipos deducidos de los
   procedimientos sp_*Payment*. Si ya existe en tu base, esto no hace nada. */

IF OBJECT_ID(N'dbo.payments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payments (
        payment_id          UNIQUEIDENTIFIER NOT NULL,
        client_dni          VARCHAR(8)       NOT NULL,
        payment_amount      DECIMAL(10, 2)   NOT NULL,
        payment_date_paid   DATETIME2        NULL,
        payment_method      VARCHAR(30)      NULL,
        payment_ispaid      BIT              NOT NULL DEFAULT 0,
        payment_image       VARBINARY(MAX)   NULL,
        payment_description VARCHAR(255)     NULL,
        payment_reference   VARCHAR(100)     NULL,
        payment_created_at  DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_payments PRIMARY KEY (payment_id)
    );
END;
GO


/* ============================ 9. ANUNCIOS ================================= */
/* La tabla announce sí estaba en tu script, pero sin las columnas
   author_uuid, image y update_at que usan los procedimientos
   (RECONSTRUIDAS). announce_likes y announce_comments también son
   RECONSTRUIDAS. */

IF OBJECT_ID(N'dbo.announce', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.announce (
        uuid        UNIQUEIDENTIFIER NOT NULL,
        priority    VARCHAR(50)      NOT NULL,
        title       VARCHAR(20)      NOT NULL,
        description VARCHAR(150)     NOT NULL,
        author      VARCHAR(20)      NOT NULL,
        author_uuid UNIQUEIDENTIFIER NULL,
        image       VARBINARY(MAX)   NULL,
        sendAt      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        update_at   DATETIME2        NULL,
        CONSTRAINT PK_announce PRIMARY KEY (uuid)
    );
END;
GO

/* Si announce ya existía con el formato viejo, le agrega lo que falte */
IF COL_LENGTH(N'dbo.announce', N'author_uuid') IS NULL
    ALTER TABLE dbo.announce ADD author_uuid UNIQUEIDENTIFIER NULL;
GO
IF COL_LENGTH(N'dbo.announce', N'image') IS NULL
    ALTER TABLE dbo.announce ADD image VARBINARY(MAX) NULL;
GO
IF COL_LENGTH(N'dbo.announce', N'update_at') IS NULL
    ALTER TABLE dbo.announce ADD update_at DATETIME2 NULL;
GO

IF OBJECT_ID(N'dbo.announce_likes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.announce_likes (
        announce_uuid UNIQUEIDENTIFIER NOT NULL,
        user_uuid     UNIQUEIDENTIFIER NOT NULL,
        CONSTRAINT PK_announce_likes PRIMARY KEY (announce_uuid, user_uuid)
    );
END;
GO

IF OBJECT_ID(N'dbo.announce_comments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.announce_comments (
        uuid          UNIQUEIDENTIFIER NOT NULL,
        announce_uuid UNIQUEIDENTIFIER NOT NULL,
        user_uuid     UNIQUEIDENTIFIER NOT NULL,
        content       NVARCHAR(500)    NOT NULL,
        createAt      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_announce_comments PRIMARY KEY (uuid)
    );
END;
GO


/* ============================ 10. LOGS DE USUARIOS ======================== */
/* RECONSTRUIDA: deducida de sp_InsertLog, sp_GetLogs, sp_GetActivityByDay y
   sp_GetMeTecStats. */

IF OBJECT_ID(N'dbo.users_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users_logs (
        user_uuid   UNIQUEIDENTIFIER NOT NULL,
        user_name   NVARCHAR(255)    NOT NULL,
        log_type    NVARCHAR(100)    NOT NULL,
        information NVARCHAR(MAX)    NULL,
        create_at   DATETIME2        NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO


/* ============================ 11. INVENTARIO ============================== */

IF OBJECT_ID(N'dbo.seq_inventory_code', N'SO') IS NULL
    CREATE SEQUENCE dbo.seq_inventory_code
        AS BIGINT
        START WITH 1
        INCREMENT BY 1;
GO

IF OBJECT_ID(N'dbo.inventory_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventory_items (
        item_uuid           UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_inventory_items PRIMARY KEY
            DEFAULT NEWSEQUENTIALID(),
        item_code           VARCHAR(40)      NOT NULL,
        item_name           NVARCHAR(150)    NOT NULL,
        item_description    NVARCHAR(500)    NULL,
        item_category       VARCHAR(60)      NOT NULL,
        item_brand          NVARCHAR(100)    NULL,
        item_model          NVARCHAR(100)    NULL,
        item_recoverable    BIT              NOT NULL DEFAULT 0,
        item_unit           VARCHAR(20)      NOT NULL DEFAULT 'UNIDAD',
        item_stock          DECIMAL(12, 2)   NOT NULL DEFAULT 0,
        item_min_stock      DECIMAL(12, 2)   NOT NULL DEFAULT 5,
        item_purchase_price DECIMAL(12, 2)   NOT NULL DEFAULT 0,
        item_active         BIT              NOT NULL DEFAULT 1,
        item_created_at     DATETIME2(3)     NOT NULL DEFAULT SYSUTCDATETIME(),
        item_updated_at     DATETIME2(3)     NULL,
        CONSTRAINT UQ_inventory_item_code UNIQUE (item_code),
        CONSTRAINT CK_inventory_stock     CHECK (item_stock >= 0),
        CONSTRAINT CK_inventory_min_stock CHECK (item_min_stock >= 0),
        CONSTRAINT CK_inventory_price     CHECK (item_purchase_price >= 0),
        CONSTRAINT CK_inventory_unit      CHECK (item_unit IN ('UNIDAD', 'METRO', 'ROLLO', 'CAJA', 'PAQUETE')),
        CONSTRAINT CK_inventory_name      CHECK (LEN(TRIM(item_name)) > 0),
        CONSTRAINT CK_inventory_code      CHECK (LEN(TRIM(item_code)) > 0),
        CONSTRAINT CK_inventory_category  CHECK (LEN(TRIM(item_category)) > 0)
    );
END;
GO

IF OBJECT_ID(N'dbo.inventory_movements', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventory_movements (
        movement_uuid           UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_inventory_movements PRIMARY KEY
            DEFAULT NEWSEQUENTIALID(),
        item_uuid               UNIQUEIDENTIFIER NOT NULL,
        movement_type           VARCHAR(20)      NOT NULL,
        movement_quantity       DECIMAL(12, 2)   NOT NULL,
        movement_previous_stock DECIMAL(12, 2)   NOT NULL,
        movement_new_stock      DECIMAL(12, 2)   NOT NULL,
        movement_description    NVARCHAR(500)    NULL,
        staff_uuid              UNIQUEIDENTIFIER NULL,
        order_uuid              UNIQUEIDENTIFIER NULL,
        movement_created_at     DATETIME2(3)     NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_inventory_movement_item  FOREIGN KEY (item_uuid)  REFERENCES dbo.inventory_items(item_uuid),
        CONSTRAINT FK_inventory_movement_staff FOREIGN KEY (staff_uuid) REFERENCES dbo.users(uuid),
        CONSTRAINT FK_inventory_movement_order FOREIGN KEY (order_uuid) REFERENCES dbo.orders(order_id),
        CONSTRAINT CK_inventory_movement_type  CHECK (movement_type IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
        CONSTRAINT CK_inventory_movement_quantity CHECK (movement_quantity > 0),
        CONSTRAINT CK_inventory_movement_stocks CHECK (
            movement_previous_stock >= 0
            AND movement_new_stock >= 0
        ),
        CONSTRAINT CK_inventory_movement_balance CHECK (
            (movement_type = 'ENTRADA'
                AND movement_new_stock = movement_previous_stock + movement_quantity)
            OR
            (movement_type = 'SALIDA'
                AND movement_new_stock = movement_previous_stock - movement_quantity)
            OR
            (movement_type = 'AJUSTE'
                AND ABS(movement_new_stock - movement_previous_stock) = movement_quantity)
        )
    );
END;
GO

IF OBJECT_ID(N'dbo.inventory_equipment_units', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventory_equipment_units (
        equipment_uuid       UNIQUEIDENTIFIER NOT NULL
            DEFAULT NEWSEQUENTIALID()
            PRIMARY KEY,
        item_uuid            UNIQUEIDENTIFIER NOT NULL,
        serial_number        VARCHAR(100)     NOT NULL UNIQUE,
        equipment_status     VARCHAR(20)      NOT NULL DEFAULT 'AVAILABLE',
        client_dni           VARCHAR(8)       NULL,
        assigned_staff_uuid  UNIQUEIDENTIFIER NULL,
        installed_at         DATETIME2(3)     NULL,
        installed_order_uuid UNIQUEIDENTIFIER NULL,
        recovered_at         DATETIME2(3)     NULL,
        CONSTRAINT FK_equipment_item            FOREIGN KEY (item_uuid)            REFERENCES dbo.inventory_items(item_uuid),
        CONSTRAINT FK_equipment_client          FOREIGN KEY (client_dni)           REFERENCES dbo.clients(client_dni),
        CONSTRAINT FK_equipment_staff           FOREIGN KEY (assigned_staff_uuid)  REFERENCES dbo.users(uuid),
        CONSTRAINT FK_equipment_installed_order FOREIGN KEY (installed_order_uuid) REFERENCES dbo.orders(order_id),
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
END;
GO

IF OBJECT_ID(N'dbo.inventory_equipment_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventory_equipment_history (
        history_uuid    UNIQUEIDENTIFIER NOT NULL
            DEFAULT NEWSEQUENTIALID()
            PRIMARY KEY,
        equipment_uuid  UNIQUEIDENTIFIER NOT NULL,
        previous_status VARCHAR(20)      NULL,
        new_status      VARCHAR(20)      NOT NULL,
        client_dni      VARCHAR(8)       NULL,
        staff_uuid      UNIQUEIDENTIFIER NULL,
        order_uuid      UNIQUEIDENTIFIER NULL,
        description     NVARCHAR(500)    NULL,
        created_at      DATETIME2(3)     NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_equipment_history_unit   FOREIGN KEY (equipment_uuid) REFERENCES dbo.inventory_equipment_units(equipment_uuid),
        CONSTRAINT FK_equipment_history_client FOREIGN KEY (client_dni)     REFERENCES dbo.clients(client_dni),
        CONSTRAINT FK_equipment_history_staff  FOREIGN KEY (staff_uuid)     REFERENCES dbo.users(uuid),
        CONSTRAINT FK_equipment_history_order  FOREIGN KEY (order_uuid)     REFERENCES dbo.orders(order_id)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = N'IX_equipment_status_item'
                 AND object_id = OBJECT_ID(N'dbo.inventory_equipment_units'))
    CREATE INDEX IX_equipment_status_item
    ON dbo.inventory_equipment_units(equipment_status, item_uuid);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = N'IX_equipment_client'
                 AND object_id = OBJECT_ID(N'dbo.inventory_equipment_units'))
    CREATE INDEX IX_equipment_client
    ON dbo.inventory_equipment_units(client_dni)
    WHERE client_dni IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = N'IX_equipment_history_unit'
                 AND object_id = OBJECT_ID(N'dbo.inventory_equipment_history'))
    CREATE INDEX IX_equipment_history_unit
    ON dbo.inventory_equipment_history(equipment_uuid, created_at DESC);
GO
