/* =============================================================================
   SERVINET - 02_procedimientos_sqlite.sql   (versión SQLite de 02_procedimientos_server.sql)
   EJECUTAR DESPUÉS de 01_tablas_sqlite.sql.

   SQLite NO tiene procedimientos almacenados (ni CREATE PROCEDURE, ni EXEC,
   ni CallableStatement en JDBC). Cada sp_* del original se reemplazó así:

   [A] ESCRIBEN DATOS (INSERT/UPDATE/DELETE, con o sin validaciones/THROW)
       -> una VISTA con el mismo nombre del procedimiento + un TRIGGER
          INSTEAD OF INSERT que ejecuta la lógica. Se "llaman" con un INSERT:

              INSERT INTO sp_CreateRole (uuid, name, hexColor, permissions)
              VALUES (?, ?, ?, ?);

          * Las columnas de la vista son los parámetros del procedimiento.
          * Cada llamada es atómica: si una validación falla (RAISE), se
            deshace TODO lo que hizo esa llamada, como el ROLLBACK original.
          * Los THROW de SQL Server son RAISE(ABORT, '[codigo] mensaje');
            en JDBC llegan como SQLException con ese texto.
          * Parámetros opcionales (@x = NULL / valor por defecto): no los
            incluyas en el INSERT o pásalos NULL.
          * No hace falta UPDLOCK/HOLDLOCK: SQLite serializa las escrituras.

   [B] LEEN DATOS SIN PARÁMETROS
       -> una VISTA con el mismo nombre:   SELECT * FROM sp_GetRoles;

   [C] LEEN DATOS CON PARÁMETROS (o devuelven el resultado de un [A])
       -> consulta lista para copiar a tu código, escrita dentro de un
          comentario de bloque que empieza con el nombre [sp_Nombre], con
          parámetros :nombre. Una vista no puede recibir parámetros, por eso
          no se pueden crear como objetos.
          En JDBC cada :nombre distinto es un número de parámetro, en orden
          de primera aparición (si se repite, es el mismo número).

   DIFERENCIAS A TENER EN CUENTA
     * Los UUID van como TEXT en minúsculas (UUID.randomUUID().toString()).
       Donde el original generaba NEWID() dentro del procedimiento, aquí el
       UUID es un parámetro opcional: si lo pasas, lo puedes consultar luego;
       si lo dejas NULL se genera solo.
     * sp_updateUser devuelve "filas afectadas": va como [C] (UPDATE normal),
       así executeUpdate() devuelve ese número.
     * Los parámetros con valor por defecto (p. ej. @item_unit = 'UNIDAD')
       también toman el defecto cuando pasas NULL explícito.
     * Fechas: guárdalas como texto 'YYYY-MM-DD HH:MM:SS.SSS' (con xerial
       sqlite-jdbc: ?date_class=TEXT en la URL).
     * Si ejecutas este archivo desde un cliente que parte sentencias por ';'
       (DBeaver, etc.), usa "ejecutar script" completo: los triggers contienen
       varios ';' internos.
   ============================================================================= */

PRAGMA foreign_keys = ON;


/* ============================================================================
   AUXILIARES (no son procedimientos del original)
   ============================================================================ */

/* Reemplazo de NEWID(): SELECT id FROM _newid  ->  UUID v4 nuevo. */
DROP VIEW IF EXISTS _newid;
CREATE VIEW _newid AS
SELECT lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4'
    || substr(lower(hex(randomblob(2))), 2) || '-'
    || substr('89ab', abs(random()) % 4 + 1, 1)
    || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6))) AS id;

/* Tabla de trabajo usada solo por sp_ToggleAnnounceLike (queda vacía). */
CREATE TABLE IF NOT EXISTS _sp_scratch (
    announce_uuid TEXT,
    user_uuid     TEXT,
    existed       INTEGER
);


/* ============================================================================
   ROLES Y PERMISOS
   ============================================================================ */

/* permissions = texto separado por comas, ej: 'USERS_READ, USERS_WRITE' */
DROP VIEW IF EXISTS sp_CreateRole;
CREATE VIEW sp_CreateRole AS
SELECT NULL AS uuid, NULL AS name, NULL AS hexColor, NULL AS permissions;

CREATE TRIGGER trg_sp_CreateRole INSTEAD OF INSERT ON sp_CreateRole
BEGIN
    INSERT INTO roles (uuid, name, hexColor)
    VALUES (NEW.uuid, NEW.name, NEW.hexColor);

    INSERT INTO role_permissions (role_uuid, permission)
    SELECT NEW.uuid, trim(value)
    FROM json_each('["' || replace(replace(NEW.permissions, '"', ''), ',', '","') || '"]');
END;

DROP VIEW IF EXISTS sp_DeleteRole;
CREATE VIEW sp_DeleteRole AS
SELECT NULL AS role_uuid;

CREATE TRIGGER trg_sp_DeleteRole INSTEAD OF INSERT ON sp_DeleteRole
BEGIN
    DELETE FROM role_permissions WHERE role_uuid = NEW.role_uuid;
    DELETE FROM roles WHERE uuid = NEW.role_uuid;
END;

DROP VIEW IF EXISTS sp_GetRoles;
CREATE VIEW sp_GetRoles AS
SELECT
    r.uuid,
    r.name,
    r.hexColor,
    rp.permission
FROM roles r
LEFT JOIN role_permissions rp
       ON r.uuid = rp.role_uuid;

/* Original: @permissions era una tabla (PermissionListType). Aquí es texto
   separado por comas, igual que en sp_CreateRole. */
DROP VIEW IF EXISTS sp_UpdateRole;
CREATE VIEW sp_UpdateRole AS
SELECT NULL AS uuid, NULL AS name, NULL AS hexColor, NULL AS permissions;

CREATE TRIGGER trg_sp_UpdateRole INSTEAD OF INSERT ON sp_UpdateRole
BEGIN
    SELECT RAISE(ABORT, '[50001] No se encontró el rol que se intenta actualizar.')
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE uuid = NEW.uuid);

    UPDATE roles
    SET name = NEW.name,
        hexColor = NEW.hexColor
    WHERE uuid = NEW.uuid;

    DELETE FROM role_permissions WHERE role_uuid = NEW.uuid;

    INSERT INTO role_permissions (role_uuid, permission)
    SELECT NEW.uuid, trim(value)
    FROM json_each('["' || replace(replace(NEW.permissions, '"', ''), ',', '","') || '"]');
END;


/* ============================================================================
   USUARIOS / STAFF (PC)
   ============================================================================ */

/* [C] */
/* [sp_getStaffIsActiveHadwareId]
SELECT user_uuid, expires_at
FROM users_session
WHERE hardware_id = :hardware_id;
*/

/* [C] */
/* [sp_getUserPasswordByEmail]
SELECT password_hash, uuid
FROM users
WHERE email = :user_email;
*/

/* [C] OJO: en el original este procedimiento (nombre "remove") solo hace
   SELECT, no borra nada. Aquí se porta igual. Si lo que querías era cerrar
   la sesión del equipo, usa el DELETE de abajo. */
/* [sp_removeStaffActiveHadwareId]
SELECT user_uuid, expires_at
FROM users_session
WHERE hardware_id = :hardware_id;

-- (lo que probablemente se quería)
-- DELETE FROM users_session WHERE hardware_id = :hardware_id;
*/

/* [C] devuelve 2 resultados: primero el rol, luego sus permisos */
/* [sp_getRole]
SELECT uuid, name, hexColor
FROM roles
WHERE uuid = :role_uuid;

SELECT permission
FROM role_permissions
WHERE role_uuid = :role_uuid;
*/

/* [B] */
DROP VIEW IF EXISTS sp_getAllStaff;
CREATE VIEW sp_getAllStaff AS
SELECT * FROM users ORDER BY create_at;

/* [C] devuelve las filas afectadas: ejecútalo con executeUpdate() (0 si el
   usuario no existe; el original tampoco lanzaba error). */
/* [sp_updateUser]
UPDATE users
SET
    email         = COALESCE(:email, email),
    role          = COALESCE(:role_uuid, role),
    password_hash = COALESCE(:new_password_hash, password_hash),
    perfil_img    = COALESCE(:image_bytes, perfil_img)
WHERE uuid = :uuid;
*/

/* [A] + [C]: crea/renueva la sesión del equipo (2 días). Si el usuario no
   existe no hace nada (igual que el original). Después ejecuta el SELECT
   para obtener los datos del staff, en la misma transacción. */
DROP VIEW IF EXISTS sp_getStaffAndNewSession;
CREATE VIEW sp_getStaffAndNewSession AS
SELECT NULL AS hardware_id, NULL AS staff_uuid;

CREATE TRIGGER trg_sp_getStaffAndNewSession INSTEAD OF INSERT ON sp_getStaffAndNewSession
BEGIN
    INSERT INTO users_session (hardware_id, user_uuid)
    SELECT NEW.hardware_id, NEW.staff_uuid
    WHERE EXISTS (SELECT 1 FROM users WHERE uuid = NEW.staff_uuid)
    ON CONFLICT (hardware_id) DO UPDATE
    SET user_uuid  = excluded.user_uuid,
        expires_at = strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime', '+2 days');
END;

/* [sp_getStaffAndNewSession - resultado]
SELECT
    u.uuid,
    u.phone,
    u.display,
    u.email,
    u.role AS role_id,
    u.create_at,
    u.perfil_img
FROM users AS u
WHERE u.uuid = :staff_uuid;
*/

/* [C] */
/* [sp_isNameOrEmail]
SELECT 1 FROM users WHERE email = :email;
*/

/* [A] Si ya existe el email NO inserta (el original devolvía uuid NULL).
   Resultado: ejecuta el SELECT; si no devuelve filas, el email ya existía. */
DROP VIEW IF EXISTS sp_createUser;
CREATE VIEW sp_createUser AS
SELECT NULL AS uuid, NULL AS display, NULL AS email, NULL AS phone, NULL AS role,
       NULL AS password_hash, NULL AS create_at, NULL AS perfil_img;

CREATE TRIGGER trg_sp_createUser INSTEAD OF INSERT ON sp_createUser
BEGIN
    INSERT INTO users (uuid, display, email, phone, role, password_hash, create_at, perfil_img)
    SELECT NEW.uuid, NEW.display, NEW.email, NEW.phone, NEW.role, NEW.password_hash,
           COALESCE(NEW.create_at, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
           NEW.perfil_img
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = NEW.email);
END;

/* [sp_createUser - resultado]
SELECT uuid FROM users WHERE uuid = :uuid;
*/

DROP VIEW IF EXISTS sp_deleteUser;
CREATE VIEW sp_deleteUser AS
SELECT NULL AS uuid;

CREATE TRIGGER trg_sp_deleteUser INSTEAD OF INSERT ON sp_deleteUser
BEGIN
    SELECT RAISE(ABORT, '[50001] No se encontró el usuario que se desea eliminar')
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE uuid = NEW.uuid);

    DELETE FROM users_session WHERE user_uuid = NEW.uuid;
    DELETE FROM users WHERE uuid = NEW.uuid;
END;

/* [C] */
/* [sp_countUsersWithRole]
SELECT COUNT(*) AS total
FROM users
WHERE role = :role_uuid;
*/

DROP VIEW IF EXISTS sp_updatePassword;
CREATE VIEW sp_updatePassword AS
SELECT NULL AS uuid, NULL AS new_password_hash;

CREATE TRIGGER trg_sp_updatePassword INSTEAD OF INSERT ON sp_updatePassword
BEGIN
    SELECT RAISE(ABORT, '[50002] No se encontró el usuario cuya contraseña se desea actualizar')
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE uuid = NEW.uuid);

    UPDATE users
    SET password_hash = NEW.new_password_hash
    WHERE uuid = NEW.uuid;
END;

/* [C] */
/* [sp_getStaffByUuid]
SELECT
    uuid,
    email,
    phone,
    role AS role_id,
    create_at,
    display,
    perfil_img
FROM users
WHERE uuid = :staff_uuid;
*/


/* ============================================================================
   LOGIN MOVIL
   ============================================================================ */

DROP VIEW IF EXISTS sp_logoutSession;
CREATE VIEW sp_logoutSession AS
SELECT NULL AS sessions_id, NULL AS user_uuid;

CREATE TRIGGER trg_sp_logoutSession INSTEAD OF INSERT ON sp_logoutSession
BEGIN
    UPDATE api_sessions
    SET revoked_at = strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')
    WHERE sessions_id = NEW.sessions_id
      AND user_uuid = NEW.user_uuid;
END;

/* [C] */
/* [sp_GetSession]
SELECT
    sessions_id,
    user_uuid,
    access_token_id,
    refresh_token_id,
    user_agent,
    user_ip,
    created_at,
    expires_at,
    revoked_at
FROM api_sessions
WHERE sessions_id = :sessions_id
  AND access_token_id = :access_token_id;
*/

DROP VIEW IF EXISTS sp_SetSession;
CREATE VIEW sp_SetSession AS
SELECT NULL AS sessions_id, NULL AS user_uuid, NULL AS access_token_id,
       NULL AS refresh_token_id, NULL AS user_agent, NULL AS user_ip;

CREATE TRIGGER trg_sp_SetSession INSTEAD OF INSERT ON sp_SetSession
BEGIN
    INSERT INTO api_sessions (
        sessions_id, user_uuid, access_token_id, refresh_token_id, user_agent, user_ip
    )
    VALUES (
        NEW.sessions_id, NEW.user_uuid, NEW.access_token_id, NEW.refresh_token_id,
        NEW.user_agent, NEW.user_ip
    );
END;

/* [C] si :user_name es NULL busca por email; si no, por display (igual que el original) */
/* [sp_GetUserMovil]
SELECT
    u.uuid AS user_uuid,
    u.display,
    u.email,
    u.role,
    u.password_hash,
    u.create_at,
    u.perfil_img,
    r.uuid AS role_uuid,
    r.name,
    r.hexColor
FROM users AS u
INNER JOIN roles AS r
        ON u.role = r.uuid
WHERE (:user_name IS NULL     AND u.email   = :email)
   OR (:user_name IS NOT NULL AND u.display = :user_name);
*/

/* [C] */
/* [sp_GetMeMovil]
SELECT
    u.uuid AS user_uuid,
    u.display,
    u.email,
    u.role,
    u.create_at,
    u.perfil_img,
    r.uuid AS role_uuid,
    r.name,
    r.hexColor
FROM users AS u
INNER JOIN roles AS r
        ON u.role = r.uuid
WHERE u.uuid = :user_uuid;
*/


/* ============================================================================
   ANTENAS
   ============================================================================ */

/* [C] */
/* [sp_GetClientsByAntenna]
SELECT client_dni
FROM antennasClient
WHERE antenna_uuid = :antenna_uuid;
*/

DROP VIEW IF EXISTS sp_AddAntennaClient;
CREATE VIEW sp_AddAntennaClient AS
SELECT NULL AS antenna_uuid, NULL AS client_dni;

CREATE TRIGGER trg_sp_AddAntennaClient INSTEAD OF INSERT ON sp_AddAntennaClient
BEGIN
    SELECT RAISE(ABORT, '[50001] La antena no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM antennas WHERE uuid = NEW.antenna_uuid);

    SELECT RAISE(ABORT, '[50002] El cliente no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE client_dni = NEW.client_dni);

    SELECT RAISE(ABORT, '[50003] La relación entre antena y cliente ya existe.')
    WHERE EXISTS (
        SELECT 1 FROM antennasClient
        WHERE antenna_uuid = NEW.antenna_uuid
          AND client_dni = NEW.client_dni
    );

    INSERT INTO antennasClient (antenna_uuid, client_dni)
    VALUES (NEW.antenna_uuid, NEW.client_dni);
END;

/* [B] El original devolvía 2 resultados; aquí son 2 vistas:
       sp_GetAntennasWithMaintenance_antennas     (las antenas)
       sp_GetAntennasWithMaintenance_maintenance  (los mantenimientos)   */
DROP VIEW IF EXISTS sp_GetAntennasWithMaintenance_antennas;
CREATE VIEW sp_GetAntennasWithMaintenance_antennas AS
SELECT
    a.uuid,
    a.priority,
    a.name,
    a.for_repair,
    a.for_maintenance,
    a.date_create,
    a.date_last_maintenance,
    a.count_days_on,
    a.count_days_off,
    a.status,
    a.image,
    a.latitude,
    a.longitude
FROM antennas AS a
ORDER BY
    a.priority DESC,
    a.name ASC;

DROP VIEW IF EXISTS sp_GetAntennasWithMaintenance_maintenance;
CREATE VIEW sp_GetAntennasWithMaintenance_maintenance AS
SELECT
    m.uuid,
    m.antenna_uuid,
    m.technical_uuid,
    m.description,
    m.date_start,
    m.date_ended,
    m.image
FROM maintenance AS m
INNER JOIN antennas AS a
        ON a.uuid = m.antenna_uuid
ORDER BY
    m.date_start DESC,
    m.uuid ASC;

DROP VIEW IF EXISTS sp_DeleteAntenna;
CREATE VIEW sp_DeleteAntenna AS
SELECT NULL AS uuid;

CREATE TRIGGER trg_sp_DeleteAntenna INSTEAD OF INSERT ON sp_DeleteAntenna
BEGIN
    SELECT RAISE(ABORT, '[50001] La antena no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM antennas WHERE uuid = NEW.uuid);

    DELETE FROM maintenance WHERE antenna_uuid = NEW.uuid;
    DELETE FROM antennas WHERE uuid = NEW.uuid;
END;

DROP VIEW IF EXISTS sp_UpdateAntenna;
CREATE VIEW sp_UpdateAntenna AS
SELECT NULL AS uuid, NULL AS priority, NULL AS name, NULL AS for_repair,
       NULL AS for_maintenance, NULL AS date_last_maintenance, NULL AS count_days_on,
       NULL AS count_days_off, NULL AS status, NULL AS latitude, NULL AS longitude;

CREATE TRIGGER trg_sp_UpdateAntenna INSTEAD OF INSERT ON sp_UpdateAntenna
BEGIN
    SELECT RAISE(ABORT, '[50002] La antena no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM antennas WHERE uuid = NEW.uuid);

    UPDATE antennas
    SET priority = NEW.priority,
        name = NEW.name,
        for_repair = NEW.for_repair,
        for_maintenance = NEW.for_maintenance,
        date_last_maintenance = NEW.date_last_maintenance,
        count_days_on = NEW.count_days_on,
        count_days_off = NEW.count_days_off,
        status = NEW.status,
        latitude = NEW.latitude,
        longitude = NEW.longitude
    WHERE uuid = NEW.uuid;
END;

/* [A] + [C] */
DROP VIEW IF EXISTS sp_UpdateAntennaStatus;
CREATE VIEW sp_UpdateAntennaStatus AS
SELECT NULL AS uuid, NULL AS status;

CREATE TRIGGER trg_sp_UpdateAntennaStatus INSTEAD OF INSERT ON sp_UpdateAntennaStatus
BEGIN
    SELECT RAISE(ABORT, '[50002] La antena no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM antennas WHERE uuid = NEW.uuid);

    UPDATE antennas SET status = NEW.status WHERE uuid = NEW.uuid;
END;

/* [sp_UpdateAntennaStatus - resultado]
SELECT status FROM antennas WHERE uuid = :uuid;
*/

/* [A] + [C] */
DROP VIEW IF EXISTS sp_ToggleAntennaMaintenance;
CREATE VIEW sp_ToggleAntennaMaintenance AS
SELECT NULL AS uuid;

CREATE TRIGGER trg_sp_ToggleAntennaMaintenance INSTEAD OF INSERT ON sp_ToggleAntennaMaintenance
BEGIN
    SELECT RAISE(ABORT, '[50002] La antena no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM antennas WHERE uuid = NEW.uuid);

    UPDATE antennas
    SET for_maintenance = CASE WHEN for_maintenance = 1 THEN 0 ELSE 1 END
    WHERE uuid = NEW.uuid;
END;

/* [sp_ToggleAntennaMaintenance - resultado]
SELECT for_maintenance FROM antennas WHERE uuid = :uuid;
*/

/* El original devolvía SELECT @uuid AS uuid: aquí el uuid ya lo conoces porque
   es un parámetro. count_days_on / count_days_off toman 0 si van NULL. */
DROP VIEW IF EXISTS sp_InsertAntenna;
CREATE VIEW sp_InsertAntenna AS
SELECT NULL AS uuid, NULL AS priority, NULL AS name, NULL AS for_repair,
       NULL AS for_maintenance, NULL AS date_create, NULL AS date_last_maintenance,
       NULL AS count_days_on, NULL AS count_days_off, NULL AS status, NULL AS image,
       NULL AS latitude, NULL AS longitude;

CREATE TRIGGER trg_sp_InsertAntenna INSTEAD OF INSERT ON sp_InsertAntenna
BEGIN
    INSERT INTO antennas (
        uuid, priority, name, for_repair, for_maintenance,
        date_create, date_last_maintenance, count_days_on,
        count_days_off, image, status, latitude, longitude
    )
    VALUES (
        NEW.uuid, NEW.priority, NEW.name, NEW.for_repair, NEW.for_maintenance,
        NEW.date_create, NEW.date_last_maintenance, COALESCE(NEW.count_days_on, 0),
        COALESCE(NEW.count_days_off, 0), NEW.image, NEW.status, NEW.latitude, NEW.longitude
    );
END;

/* [C] */
/* [sp_GetAntennaImage]
SELECT image
FROM antennas
WHERE uuid = :uuid;
*/

/* [C] */
/* [sp_GetConnectedClientsByAntenna]
SELECT COUNT(DISTINCT c.client_dni) AS total_connections
FROM clients c
INNER JOIN orders o
        ON o.order_client_dni = c.client_dni
WHERE o.order_antenna_connect = :antenna_uuid
  AND c.client_status IN ('ACTIVO', 'SUSPENDIDO');
*/

/* [B] */
DROP VIEW IF EXISTS sp_GetAllConnectedClients;
CREATE VIEW sp_GetAllConnectedClients AS
SELECT COUNT(DISTINCT c.client_dni) AS total_connections
FROM clients c
INNER JOIN orders o
        ON o.order_client_dni = c.client_dni
WHERE c.client_status IN ('ACTIVO', 'SUSPENDIDO')
  AND o.order_antenna_connect IS NOT NULL;


/* ============================================================================
   CONFIGURACION DE LA APP
   ============================================================================ */

/* [B] app_image se devuelve siempre NULL (igual que el original) */
DROP VIEW IF EXISTS sp_GetAppGeneral;
CREATE VIEW sp_GetAppGeneral AS
SELECT
    app_name,
    NULL AS app_image
FROM appGeneral;

DROP VIEW IF EXISTS sp_RenameApp;
CREATE VIEW sp_RenameApp AS
SELECT NULL AS name;

CREATE TRIGGER trg_sp_RenameApp INSTEAD OF INSERT ON sp_RenameApp
BEGIN
    SELECT RAISE(ABORT, '[50010] No existe la configuración de la aplicación.')
    WHERE NOT EXISTS (SELECT 1 FROM appGeneral);

    UPDATE appGeneral SET app_name = NEW.name;
END;

DROP VIEW IF EXISTS sp_ChangeAppImage;
CREATE VIEW sp_ChangeAppImage AS
SELECT NULL AS image;

CREATE TRIGGER trg_sp_ChangeAppImage INSTEAD OF INSERT ON sp_ChangeAppImage
BEGIN
    SELECT RAISE(ABORT, '[50011] No existe la configuración de la aplicación.')
    WHERE NOT EXISTS (SELECT 1 FROM appGeneral);

    UPDATE appGeneral SET app_image = NEW.image;
END;


/* ============================================================================
   PLANES
   ============================================================================ */

DROP VIEW IF EXISTS sp_AddPlan;
CREATE VIEW sp_AddPlan AS
SELECT NULL AS plan_id, NULL AS plan_name, NULL AS plan_created, NULL AS plan_speed,
       NULL AS plan_price, NULL AS plan_ispromo, NULL AS plan_isactive;

CREATE TRIGGER trg_sp_AddPlan INSTEAD OF INSERT ON sp_AddPlan
BEGIN
    INSERT INTO plans (
        plan_id, plan_name, plan_created, plan_speed, plan_price, plan_ispromo, plan_isactive
    )
    VALUES (
        NEW.plan_id, NEW.plan_name,
        COALESCE(NEW.plan_created, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
        NEW.plan_speed, NEW.plan_price, NEW.plan_ispromo, NEW.plan_isactive
    );
END;

/* [B] */
DROP VIEW IF EXISTS sp_GetAllPlans;
CREATE VIEW sp_GetAllPlans AS
SELECT * FROM plans;

DROP VIEW IF EXISTS sp_DeactivatePlan;
CREATE VIEW sp_DeactivatePlan AS
SELECT NULL AS plan_id;

CREATE TRIGGER trg_sp_DeactivatePlan INSTEAD OF INSERT ON sp_DeactivatePlan
BEGIN
    SELECT RAISE(ABORT, '[50001] El plan no existe')
    WHERE NOT EXISTS (SELECT 1 FROM plans WHERE plan_id = NEW.plan_id);

    UPDATE plans SET plan_isactive = 0 WHERE plan_id = NEW.plan_id;
END;


/* ============================================================================
   CLIENTES Y PEDIDOS
   ============================================================================ */

/* [B] */
DROP VIEW IF EXISTS sp_GetClientsWithOrders;
CREATE VIEW sp_GetClientsWithOrders AS
SELECT
    c.client_dni,
    c.client_name,
    c.client_last_name,
    c.client_phone,
    c.client_address,
    c.client_email,
    c.client_whatsapp,
    c.client_register,
    c.client_status,
    c.longitud,
    c.latitud,
    o.order_id,
    o.order_antenna_connect,
    o.order_plan_connect,
    o.order_created,
    o.order_status
FROM clients c
LEFT JOIN orders o
       ON o.order_client_dni = c.client_dni
ORDER BY
    c.client_register DESC,
    o.order_created DESC;

/* Crea el cliente y su pedido en una sola llamada atómica. */
DROP VIEW IF EXISTS sp_CreateClientAndOrder;
CREATE VIEW sp_CreateClientAndOrder AS
SELECT NULL AS client_dni, NULL AS client_name, NULL AS client_last_name,
       NULL AS client_phone, NULL AS client_address, NULL AS client_email,
       NULL AS client_whatsapp, NULL AS client_register, NULL AS client_status,
       NULL AS longitud, NULL AS latitud,
       NULL AS order_id, NULL AS order_antenna_connect, NULL AS order_plan_connect,
       NULL AS order_created, NULL AS order_status;

CREATE TRIGGER trg_sp_CreateClientAndOrder INSTEAD OF INSERT ON sp_CreateClientAndOrder
BEGIN
    INSERT INTO clients (
        client_dni, client_name, client_last_name, client_phone, client_address,
        client_email, client_whatsapp, client_register, client_status, longitud, latitud
    )
    VALUES (
        NEW.client_dni, NEW.client_name, NEW.client_last_name, NEW.client_phone,
        NEW.client_address, NEW.client_email, NEW.client_whatsapp,
        COALESCE(NEW.client_register, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')),
        COALESCE(NEW.client_status, 'ACTIVO'), NEW.longitud, NEW.latitud
    );

    INSERT INTO orders (
        order_id, order_client_dni, order_antenna_connect, order_plan_connect,
        order_created, order_status
    )
    VALUES (
        NEW.order_id, NEW.client_dni, NEW.order_antenna_connect, NEW.order_plan_connect,
        NEW.order_created, COALESCE(NEW.order_status, 'PENDIENTE')
    );
END;

/* Solo cambia los campos que no vengan NULL. Si plan_id no es NULL, actualiza
   el plan de TODOS los pedidos del cliente (igual que el original).
   OJO: en el original @plan_id era VARCHAR(10) y no cabe un GUID de 36
   caracteres; aquí es TEXT completo. */
DROP VIEW IF EXISTS sp_updateClient;
CREATE VIEW sp_updateClient AS
SELECT NULL AS client_dni, NULL AS client_phone, NULL AS client_address,
       NULL AS client_email, NULL AS client_whatsapp, NULL AS client_status,
       NULL AS plan_id;

CREATE TRIGGER trg_sp_updateClient INSTEAD OF INSERT ON sp_updateClient
BEGIN
    SELECT RAISE(ABORT, '[50001] El cliente no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE client_dni = NEW.client_dni);

    SELECT RAISE(ABORT, '[50002] El plan seleccionado no existe.')
    WHERE NEW.plan_id IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM plans WHERE plan_id = NEW.plan_id);

    UPDATE clients
    SET
        client_phone    = COALESCE(NEW.client_phone, client_phone),
        client_address  = COALESCE(NEW.client_address, client_address),
        client_email    = COALESCE(NEW.client_email, client_email),
        client_whatsapp = COALESCE(NEW.client_whatsapp, client_whatsapp),
        client_status   = COALESCE(NEW.client_status, client_status)
    WHERE client_dni = NEW.client_dni;

    UPDATE orders
    SET order_plan_connect = NEW.plan_id
    WHERE order_client_dni = NEW.client_dni
      AND NEW.plan_id IS NOT NULL;
END;

DROP VIEW IF EXISTS sp_UpdateClientStatus;
CREATE VIEW sp_UpdateClientStatus AS
SELECT NULL AS client_dni, NULL AS client_status;

CREATE TRIGGER trg_sp_UpdateClientStatus INSTEAD OF INSERT ON sp_UpdateClientStatus
BEGIN
    SELECT RAISE(ABORT, '[50002] Estado inválido. Use ACTIVO, SUSPENDIDO o RETIRADO.')
    WHERE NEW.client_status IS NULL
       OR NEW.client_status NOT IN ('ACTIVO', 'SUSPENDIDO', 'RETIRADO');

    SELECT RAISE(ABORT, '[50001] No se encontró un cliente con ese DNI.')
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE client_dni = NEW.client_dni);

    UPDATE clients
    SET client_status = NEW.client_status
    WHERE client_dni = NEW.client_dni;
END;


/* ============================================================================
   INSTALACIONES Y TECNICOS
   ============================================================================ */

/* [C] */
/* [sp_GetActiveInstallation]
SELECT
    servicio_uuid,
    staff_uuid,
    client_dni,
    accion_tipo,
    order_id,
    description_tipo,
    current_order
FROM tecnicos_logs
WHERE staff_uuid = :staff_uuid
  AND current_order = 1;
*/

/* Además de las validaciones, los índices únicos parciales
   UX_Tecnico_CurrentOrder / UX_Order_CurrentTechnician evitan duplicados. */
DROP VIEW IF EXISTS sp_AcceptInstallation;
CREATE VIEW sp_AcceptInstallation AS
SELECT NULL AS staff_uuid, NULL AS order_id, NULL AS client_dni;

CREATE TRIGGER trg_sp_AcceptInstallation INSTEAD OF INSERT ON sp_AcceptInstallation
BEGIN
    SELECT RAISE(ABORT, '[50001] El técnico ya tiene una instalación activa.')
    WHERE EXISTS (
        SELECT 1 FROM tecnicos_logs
        WHERE staff_uuid = NEW.staff_uuid AND current_order = 1
    );

    SELECT RAISE(ABORT, '[50002] Este pedido ya fue aceptado por otro técnico.')
    WHERE EXISTS (
        SELECT 1 FROM tecnicos_logs
        WHERE order_id = NEW.order_id AND current_order = 1
    );

    INSERT INTO tecnicos_logs (
        staff_uuid, client_dni, accion_tipo, order_id, description_tipo, current_order
    )
    VALUES (
        NEW.staff_uuid, NEW.client_dni, 'INTSTALACION', NEW.order_id,
        'Instalación aceptada por el técnico', 1
    );
END;

DROP VIEW IF EXISTS sp_RejectInstallation;
CREATE VIEW sp_RejectInstallation AS
SELECT NULL AS staff_uuid, NULL AS order_id;

CREATE TRIGGER trg_sp_RejectInstallation INSTEAD OF INSERT ON sp_RejectInstallation
BEGIN
    SELECT RAISE(ABORT, '[50009] No tienes esta instalación asignada.')
    WHERE NOT EXISTS (
        SELECT 1 FROM tecnicos_logs
        WHERE staff_uuid = NEW.staff_uuid
          AND order_id = NEW.order_id
          AND accion_tipo = 'INTSTALACION'
          AND current_order = 1
    );

    DELETE FROM tecnicos_logs
    WHERE staff_uuid = NEW.staff_uuid
      AND order_id = NEW.order_id
      AND accion_tipo = 'INTSTALACION'
      AND current_order = 1;
END;

/* [C] */
/* [sp_IsInstallationInProgress]
SELECT CASE
           WHEN EXISTS (
               SELECT 1
               FROM tecnicos_logs
               WHERE order_id = :order_id
                 AND current_order = 1
                 AND accion_tipo = 'INTSTALACION'
           )
           THEN 1
           ELSE 0
       END AS in_progress;
*/

/* Registra el reporte, libera al técnico y activa al cliente (todo atómico).
   Errores: 50003 sin instalación asignada, 50004 pedido de otro cliente,
   50005 reporte duplicado, 50007 el cliente no estaba EN_INSTALACION.
   (El 50006 del original no puede ocurrir: el índice único parcial garantiza
   una sola fila activa por pedido.) */
DROP VIEW IF EXISTS sp_FinishInstallation;
CREATE VIEW sp_FinishInstallation AS
SELECT NULL AS staff_uuid, NULL AS order_id, NULL AS report_client_dni,
       NULL AS report_observation, NULL AS img_router, NULL AS img_conexion,
       NULL AS img_mini_antenna, NULL AS router_password;

CREATE TRIGGER trg_sp_FinishInstallation INSTEAD OF INSERT ON sp_FinishInstallation
BEGIN
    SELECT RAISE(ABORT, '[50003] No tienes esta instalación asignada.')
    WHERE NOT EXISTS (
        SELECT 1 FROM tecnicos_logs
        WHERE staff_uuid = NEW.staff_uuid
          AND order_id = NEW.order_id
          AND client_dni = NEW.report_client_dni
          AND accion_tipo = 'INTSTALACION'
          AND current_order = 1
    );

    SELECT RAISE(ABORT, '[50004] El pedido no corresponde al cliente.')
    WHERE NOT EXISTS (
        SELECT 1 FROM orders
        WHERE order_id = NEW.order_id
          AND order_client_dni = NEW.report_client_dni
    );

    SELECT RAISE(ABORT, '[50005] Este pedido ya tiene un reporte registrado.')
    WHERE EXISTS (SELECT 1 FROM orders_report WHERE order_id = NEW.order_id);

    SELECT RAISE(ABORT, '[50007] No se pudo activar al cliente.')
    WHERE NOT EXISTS (
        SELECT 1 FROM clients
        WHERE client_dni = NEW.report_client_dni
          AND client_status = 'EN_INSTALACION'
    );

    INSERT INTO orders_report (
        order_id, report_client_dni, report_observation, img_router, img_conexion,
        img_mini_antenna, router_password, report_status, report_ended
    )
    VALUES (
        NEW.order_id, NEW.report_client_dni, NEW.report_observation, NEW.img_router,
        NEW.img_conexion, NEW.img_mini_antenna, NEW.router_password, 'INSTALLED_ACTIVE',
        strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')
    );

    UPDATE tecnicos_logs
    SET current_order = 0,
        description_tipo = 'Instalación finalizada'
    WHERE staff_uuid = NEW.staff_uuid
      AND order_id = NEW.order_id
      AND current_order = 1;

    UPDATE clients
    SET client_status = 'ACTIVO'
    WHERE client_dni = NEW.report_client_dni
      AND client_status = 'EN_INSTALACION';
END;

/* [C] */
/* [sp_GetTechnicianLogs]
SELECT
    o.order_id,
    o.order_client_dni,
    o.order_antenna_connect,
    o.order_plan_connect,
    o.order_created,
    o.order_status,

    r.report_client_dni,
    r.report_observation,
    r.img_router,
    r.img_conexion,
    r.img_mini_antenna,
    r.router_password,
    r.report_create,
    r.report_status,
    r.report_ended

FROM tecnicos_logs tl
INNER JOIN orders o
        ON o.order_id = tl.order_id
LEFT JOIN orders_report r
       ON r.order_id = o.order_id

WHERE tl.staff_uuid = :staff_uuid
ORDER BY tl.servicio_uuid DESC;
*/

/* [C] */
/* [sp_GetTechnicianLogsByClient]
SELECT
    o.order_id,
    o.order_client_dni,
    o.order_antenna_connect,
    o.order_plan_connect,
    o.order_created,
    o.order_status,

    r.report_client_dni,
    r.report_observation,
    r.img_router,
    r.img_conexion,
    r.img_mini_antenna,
    r.router_password,
    r.report_create,
    r.report_status,
    r.report_ended

FROM orders o
LEFT JOIN orders_report r
       ON r.order_id = o.order_id

WHERE o.order_client_dni = :order_client_dni
ORDER BY o.order_created DESC;
*/

/* [B] */
DROP VIEW IF EXISTS sp_GetTechnicianStatistics;
CREATE VIEW sp_GetTechnicianStatistics AS
SELECT
    staff_uuid,
    COUNT(*) AS total_instalaciones
FROM tecnicos_logs
WHERE accion_tipo = 'INTSTALACION'
GROUP BY staff_uuid
ORDER BY total_instalaciones DESC;


/* ============================================================================
   ATENCION AL CLIENTE
   ============================================================================ */

DROP VIEW IF EXISTS sp_RegisterAttentionLog;
CREATE VIEW sp_RegisterAttentionLog AS
SELECT NULL AS staff_uuid, NULL AS client_dni, NULL AS client_name,
       NULL AS client_last_name, NULL AS atencion_tipo, NULL AS valoracion,
       NULL AS description_tipo;

CREATE TRIGGER trg_sp_RegisterAttentionLog INSTEAD OF INSERT ON sp_RegisterAttentionLog
BEGIN
    INSERT INTO atencion_logs (
        staff_uuid, client_dni, client_name, client_last_name,
        atencion_tipo, valoracion, description_tipo
    )
    VALUES (
        NEW.staff_uuid, NEW.client_dni, NEW.client_name, NEW.client_last_name,
        NEW.atencion_tipo, NEW.valoracion, NEW.description_tipo
    );
END;

/* [B] */
DROP VIEW IF EXISTS sp_GetAttentionStatistics;
CREATE VIEW sp_GetAttentionStatistics AS
SELECT
    staff_uuid,
    COUNT(*) AS total_atenciones,
    AVG(CAST(valoracion AS REAL)) AS promedio_valoracion,
    SUM(CASE WHEN atencion_tipo = 'VENTA' THEN 1 ELSE 0 END) AS total_ventas,
    SUM(CASE WHEN atencion_tipo = 'INFORME' THEN 1 ELSE 0 END) AS total_informes,
    SUM(CASE WHEN atencion_tipo = 'SOPORTE' THEN 1 ELSE 0 END) AS total_soportes,
    SUM(CASE WHEN atencion_tipo = 'SUSPENCION' THEN 1 ELSE 0 END) AS total_suspensiones,
    SUM(CASE WHEN atencion_tipo = 'COBRO' THEN 1 ELSE 0 END) AS total_cobros,
    SUM(CASE WHEN atencion_tipo = 'ATENCION_CLIENTE' THEN 1 ELSE 0 END) AS total_atenciones_cliente
FROM atencion_logs
GROUP BY staff_uuid
ORDER BY total_atenciones DESC;


/* ============================================================================
   PAGOS
   ============================================================================ */

/* [B] */
DROP VIEW IF EXISTS sp_GetAllMoney;
CREATE VIEW sp_GetAllMoney AS
SELECT IFNULL(SUM(payment_amount), 0) AS total_money
FROM payments;

/* [B] */
DROP VIEW IF EXISTS sp_GetAllPayments;
CREATE VIEW sp_GetAllPayments AS
SELECT
    payment_id,
    client_dni,
    payment_amount,
    payment_date_paid,
    payment_method,
    payment_ispaid,
    payment_image,
    payment_description,
    payment_reference,
    payment_created_at
FROM payments
ORDER BY payment_created_at DESC;

/* [C] */
/* [sp_GetPaymentsByClient]
SELECT
    payment_id,
    client_dni,
    payment_amount,
    payment_date_paid,
    payment_created_at,
    payment_method,
    payment_ispaid,
    payment_image,
    payment_description,
    payment_reference
FROM payments
WHERE client_dni = :client_dni
ORDER BY payment_created_at DESC;
*/

/* [C] El original lanzaba error 50001 si :offset < 0 o :page_size <= 0;
   en una consulta no se puede lanzar error, valida eso en tu código.
   payment_image se devuelve NULL (igual que el original). */
/* [sp_GetPaymentsByClientPage]
SELECT
    payment_id,
    client_dni,
    payment_amount,
    payment_created_at,
    payment_date_paid,
    payment_method,
    payment_ispaid,
    NULL AS payment_image,
    payment_description,
    payment_reference
FROM payments
WHERE client_dni = :client_dni
ORDER BY payment_created_at DESC, payment_id DESC
LIMIT :page_size OFFSET :offset;
*/

DROP VIEW IF EXISTS sp_CreatePendingPayment;
CREATE VIEW sp_CreatePendingPayment AS
SELECT NULL AS client_dni, NULL AS payment_amount, NULL AS payment_description,
       NULL AS payment_reference, NULL AS payment_id;

CREATE TRIGGER trg_sp_CreatePendingPayment INSTEAD OF INSERT ON sp_CreatePendingPayment
BEGIN
    INSERT INTO payments (
        payment_id, client_dni, payment_amount, payment_description, payment_reference
    )
    VALUES (
        NEW.payment_id, NEW.client_dni, NEW.payment_amount,
        NEW.payment_description, NEW.payment_reference
    );
END;

/* [A] + [C] */
DROP VIEW IF EXISTS sp_CompletePayment;
CREATE VIEW sp_CompletePayment AS
SELECT NULL AS payment_id, NULL AS payment_method, NULL AS payment_image;

CREATE TRIGGER trg_sp_CompletePayment INSTEAD OF INSERT ON sp_CompletePayment
BEGIN
    SELECT RAISE(ABORT, '[50001] El pago no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM payments WHERE payment_id = NEW.payment_id);

    SELECT RAISE(ABORT, '[50002] Este pago ya fue registrado como pagado.')
    WHERE EXISTS (
        SELECT 1 FROM payments
        WHERE payment_id = NEW.payment_id AND payment_ispaid = 1
    );

    UPDATE payments
    SET
        payment_date_paid = strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'),
        payment_method = NEW.payment_method,
        payment_image = NEW.payment_image,
        payment_ispaid = 1
    WHERE payment_id = NEW.payment_id;
END;

/* [sp_CompletePayment - resultado]
SELECT
    payment_id,
    client_dni,
    payment_amount,
    payment_date_paid,
    payment_method,
    payment_ispaid,
    payment_image,
    payment_description,
    payment_reference
FROM payments
WHERE payment_id = :payment_id;
*/

DROP VIEW IF EXISTS sp_DeletePayment;
CREATE VIEW sp_DeletePayment AS
SELECT NULL AS payment_id;

CREATE TRIGGER trg_sp_DeletePayment INSTEAD OF INSERT ON sp_DeletePayment
BEGIN
    SELECT RAISE(ABORT, '[50022] El pago no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM payments WHERE payment_id = NEW.payment_id);

    DELETE FROM payments WHERE payment_id = NEW.payment_id;
END;


/* ============================================================================
   ANUNCIOS
   ============================================================================ */

/* [B] */
DROP VIEW IF EXISTS sp_GetAnnounces;
CREATE VIEW sp_GetAnnounces AS
SELECT * FROM announce;

/* [C] Valores por defecto del original: :offset = 0, :pageSize = 5.
   Valida en tu código que :offset >= 0 y :pageSize >= 1 (el original lanzaba
   el error 50001). OJO: liked_by_me del original NO filtra por usuario
   (vale 1 si CUALQUIER usuario dio like). Se porta igual; para que sea "mío"
   agrega "AND l2.user_uuid = :user_uuid". */
/* [sp_GetAnnouncesPage]
SELECT
    a.uuid,
    a.priority,
    a.title,
    a.description,
    a.author,
    a.author_uuid,
    a.image,
    a.sendAt,
    a.update_at,

    (SELECT COUNT(*)
     FROM announce_likes l
     WHERE l.announce_uuid = a.uuid) AS like_count,

    (SELECT COUNT(*)
     FROM announce_comments c
     WHERE c.announce_uuid = a.uuid) AS comment_count,

    CASE WHEN EXISTS (
        SELECT 1
        FROM announce_likes l2
        WHERE l2.announce_uuid = a.uuid
    ) THEN 1 ELSE 0 END AS liked_by_me

FROM announce a
ORDER BY a.sendAt DESC, a.uuid DESC
LIMIT :pageSize OFFSET :offset;
*/

DROP VIEW IF EXISTS sp_AddAnnounces;
CREATE VIEW sp_AddAnnounces AS
SELECT NULL AS uuid, NULL AS priority, NULL AS title, NULL AS description,
       NULL AS author, NULL AS author_uuid, NULL AS image, NULL AS sendAt;

CREATE TRIGGER trg_sp_AddAnnounces INSTEAD OF INSERT ON sp_AddAnnounces
BEGIN
    INSERT INTO announce (uuid, priority, title, description, author, author_uuid, image, sendAt)
    VALUES (
        NEW.uuid, NEW.priority, NEW.title, NEW.description, NEW.author, NEW.author_uuid,
        NEW.image, COALESCE(NEW.sendAt, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'))
    );
END;

/* replaceImage = 1 -> reemplaza la imagen por newImage (puede ser NULL para
   quitarla); replaceImage = 0 -> deja la imagen como está. */
DROP VIEW IF EXISTS sp_UpdateAnnounce;
CREATE VIEW sp_UpdateAnnounce AS
SELECT NULL AS uuid, NULL AS title, NULL AS description, NULL AS priority,
       NULL AS replaceImage, NULL AS newImage;

CREATE TRIGGER trg_sp_UpdateAnnounce INSTEAD OF INSERT ON sp_UpdateAnnounce
BEGIN
    SELECT RAISE(ABORT, '[50002] No se encontró el anuncio que se desea actualizar.')
    WHERE NOT EXISTS (SELECT 1 FROM announce WHERE uuid = NEW.uuid);

    UPDATE announce
    SET title = NEW.title,
        description = NEW.description,
        priority = NEW.priority,
        image = CASE WHEN NEW.replaceImage = 1 THEN NEW.newImage ELSE image END,
        update_at = strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime')
    WHERE uuid = NEW.uuid;
END;

DROP VIEW IF EXISTS sp_DeleteAnnounce;
CREATE VIEW sp_DeleteAnnounce AS
SELECT NULL AS uuid;

CREATE TRIGGER trg_sp_DeleteAnnounce INSTEAD OF INSERT ON sp_DeleteAnnounce
BEGIN
    SELECT RAISE(ABORT, '[50001] No se encontró el anuncio que se desea eliminar.')
    WHERE NOT EXISTS (SELECT 1 FROM announce WHERE uuid = NEW.uuid);

    DELETE FROM announce WHERE uuid = NEW.uuid;
END;

/* [A] + [C] Alterna el like: si existía lo quita, si no lo agrega.
   El valor OUTPUT @liked del original se consulta después con el SELECT. */
DROP VIEW IF EXISTS sp_ToggleAnnounceLike;
CREATE VIEW sp_ToggleAnnounceLike AS
SELECT NULL AS announceUuid, NULL AS userUuid;

CREATE TRIGGER trg_sp_ToggleAnnounceLike INSTEAD OF INSERT ON sp_ToggleAnnounceLike
BEGIN
    DELETE FROM _sp_scratch;

    INSERT INTO _sp_scratch (announce_uuid, user_uuid, existed)
    SELECT NEW.announceUuid, NEW.userUuid,
           EXISTS (SELECT 1 FROM announce_likes
                   WHERE announce_uuid = NEW.announceUuid AND user_uuid = NEW.userUuid);

    DELETE FROM announce_likes
    WHERE announce_uuid = NEW.announceUuid
      AND user_uuid = NEW.userUuid
      AND (SELECT existed FROM _sp_scratch) = 1;

    INSERT INTO announce_likes (announce_uuid, user_uuid)
    SELECT NEW.announceUuid, NEW.userUuid
    WHERE (SELECT existed FROM _sp_scratch) = 0;

    DELETE FROM _sp_scratch;
END;

/* [sp_ToggleAnnounceLike - resultado: 1 = quedó con like, 0 = sin like]
SELECT EXISTS (
    SELECT 1 FROM announce_likes
    WHERE announce_uuid = :announceUuid
      AND user_uuid = :userUuid
) AS liked;
*/

/* [C] */
/* [sp_GetAnnounceComments]
SELECT
    c.uuid,
    c.announce_uuid,
    c.user_uuid,
    u.display AS author_name,
    c.content,
    c.createAt
FROM announce_comments c
INNER JOIN users u
        ON u.uuid = c.user_uuid
WHERE c.announce_uuid = :announceUuid
ORDER BY c.createAt ASC, c.uuid ASC;
*/

DROP VIEW IF EXISTS sp_AddAnnounceComment;
CREATE VIEW sp_AddAnnounceComment AS
SELECT NULL AS uuid, NULL AS announceUuid, NULL AS userUuid, NULL AS content, NULL AS createAt;

CREATE TRIGGER trg_sp_AddAnnounceComment INSTEAD OF INSERT ON sp_AddAnnounceComment
BEGIN
    INSERT INTO announce_comments (uuid, announce_uuid, user_uuid, content, createAt)
    VALUES (
        NEW.uuid, NEW.announceUuid, NEW.userUuid, NEW.content,
        COALESCE(NEW.createAt, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'))
    );
END;

DROP VIEW IF EXISTS sp_UpdateAnnounceComment;
CREATE VIEW sp_UpdateAnnounceComment AS
SELECT NULL AS commentUuid, NULL AS content;

CREATE TRIGGER trg_sp_UpdateAnnounceComment INSTEAD OF INSERT ON sp_UpdateAnnounceComment
BEGIN
    SELECT RAISE(ABORT, '[50001] No se encontró el comentario.')
    WHERE NOT EXISTS (SELECT 1 FROM announce_comments WHERE uuid = NEW.commentUuid);

    UPDATE announce_comments
    SET content = NEW.content
    WHERE uuid = NEW.commentUuid;
END;

DROP VIEW IF EXISTS sp_DeleteAnnounceComment;
CREATE VIEW sp_DeleteAnnounceComment AS
SELECT NULL AS commentUuid;

CREATE TRIGGER trg_sp_DeleteAnnounceComment INSTEAD OF INSERT ON sp_DeleteAnnounceComment
BEGIN
    SELECT RAISE(ABORT, '[50002] No se encontró el comentario.')
    WHERE NOT EXISTS (SELECT 1 FROM announce_comments WHERE uuid = NEW.commentUuid);

    DELETE FROM announce_comments WHERE uuid = NEW.commentUuid;
END;


/* ============================================================================
   LOGS Y ACTIVIDAD DE USUARIOS
   ============================================================================ */

/* Busca al usuario por su nombre (display) y registra el log. */
DROP VIEW IF EXISTS sp_InsertLog;
CREATE VIEW sp_InsertLog AS
SELECT NULL AS user_name, NULL AS log_type, NULL AS information;

CREATE TRIGGER trg_sp_InsertLog INSTEAD OF INSERT ON sp_InsertLog
BEGIN
    SELECT RAISE(ABORT, '[50001] No se encontró el usuario indicado.')
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE display = NEW.user_name);

    INSERT INTO users_logs (user_uuid, user_name, log_type, information)
    VALUES (
        (SELECT uuid FROM users WHERE display = NEW.user_name),
        NEW.user_name, NEW.log_type, NEW.information
    );
END;

/* [C] Valida en tu código que :limit > 0 (el original lanzaba el error 50001). */
/* [sp_GetLogs]
SELECT
    user_name,
    log_type,
    information,
    create_at
FROM users_logs
ORDER BY create_at DESC
LIMIT :limit;
*/

/* [C] CAST(create_at AS DATE) -> date(create_at) */
/* [sp_GetActivityByDay]
SELECT
    date(create_at) AS log_day,
    COUNT(*) AS total
FROM users_logs
WHERE user_uuid = :user_uuid
  AND create_at >= :from_date
GROUP BY date(create_at)
ORDER BY log_day;
*/

/* [C] */
/* [sp_GetMeTecStats]
SELECT
    SUM(CASE WHEN log_type = 'HOME_INSTALLED'  THEN 1 ELSE 0 END) AS total_installed,
    SUM(CASE WHEN log_type = 'ORDER_ACCEPTED'  THEN 1 ELSE 0 END) AS total_order_accepted,
    SUM(CASE WHEN log_type = 'ORDER_CANCELLED' THEN 1 ELSE 0 END) AS total_order_cancelled
FROM users_logs
WHERE user_uuid = :user_uuid;
*/


/* ============================================================================
   INVENTARIO
   Los saldos se redondean a 2 decimales (round(...,2)) porque SQLite guarda
   los DECIMAL como punto flotante.
   ============================================================================ */

/* item_uuid es opcional: si lo pasas, así consultas luego el artículo creado:
       SELECT item_uuid, item_code FROM inventory_items WHERE item_uuid = :item_uuid;
   El código se genera como 'INV-000001', 'INV-000002'... */
DROP VIEW IF EXISTS sp_CreateInventoryItem;
CREATE VIEW sp_CreateInventoryItem AS
SELECT NULL AS item_uuid, NULL AS item_name, NULL AS item_description,
       NULL AS item_category, NULL AS item_brand, NULL AS item_model,
       NULL AS item_recoverable, NULL AS item_unit, NULL AS item_min_stock,
       NULL AS item_purchase_price;

CREATE TRIGGER trg_sp_CreateInventoryItem INSTEAD OF INSERT ON sp_CreateInventoryItem
BEGIN
    SELECT RAISE(ABORT, '[50002] El nombre del artículo es obligatorio.')
    WHERE NEW.item_name IS NULL OR trim(NEW.item_name) = '';

    SELECT RAISE(ABORT, '[50003] La categoría es obligatoria.')
    WHERE NEW.item_category IS NULL OR trim(NEW.item_category) = '';

    SELECT RAISE(ABORT, '[50004] Debes indicar si el artículo es recuperable.')
    WHERE NEW.item_recoverable IS NULL;

    SELECT RAISE(ABORT, '[50005] Unidad de medida inválida.')
    WHERE upper(trim(COALESCE(NEW.item_unit, 'UNIDAD')))
          NOT IN ('UNIDAD', 'METRO', 'ROLLO', 'CAJA', 'PAQUETE');

    SELECT RAISE(ABORT, '[50006] El stock mínimo no puede ser negativo.')
    WHERE COALESCE(NEW.item_min_stock, 5) < 0;

    SELECT RAISE(ABORT, '[50007] El precio no puede ser negativo.')
    WHERE COALESCE(NEW.item_purchase_price, 0) < 0;

    INSERT INTO seq_inventory_code (id) VALUES (NULL);

    INSERT INTO inventory_items (
        item_uuid, item_code, item_name, item_description, item_category,
        item_brand, item_model, item_recoverable, item_unit,
        item_min_stock, item_purchase_price
    )
    VALUES (
        COALESCE(NEW.item_uuid, (SELECT id FROM _newid)),
        'INV-' || printf('%06d', (SELECT MAX(id) FROM seq_inventory_code)),
        trim(NEW.item_name),
        NEW.item_description,
        trim(NEW.item_category),
        NEW.item_brand,
        NEW.item_model,
        NEW.item_recoverable,
        upper(trim(COALESCE(NEW.item_unit, 'UNIDAD'))),
        COALESCE(NEW.item_min_stock, 5),
        COALESCE(NEW.item_purchase_price, 0)
    );
END;

DROP VIEW IF EXISTS sp_UpdateInventoryItem;
CREATE VIEW sp_UpdateInventoryItem AS
SELECT NULL AS item_uuid, NULL AS item_name, NULL AS item_description,
       NULL AS item_category, NULL AS item_brand, NULL AS item_model,
       NULL AS item_recoverable, NULL AS item_min_stock, NULL AS item_purchase_price;

CREATE TRIGGER trg_sp_UpdateInventoryItem INSTEAD OF INSERT ON sp_UpdateInventoryItem
BEGIN
    SELECT RAISE(ABORT, '[50200] El artículo es obligatorio.')
    WHERE NEW.item_uuid IS NULL;

    SELECT RAISE(ABORT, '[50201] El nombre es obligatorio.')
    WHERE NEW.item_name IS NULL OR trim(NEW.item_name) = '';

    SELECT RAISE(ABORT, '[50202] La categoría es obligatoria.')
    WHERE NEW.item_category IS NULL OR trim(NEW.item_category) = '';

    SELECT RAISE(ABORT, '[50203] El indicador recuperable es obligatorio.')
    WHERE NEW.item_recoverable IS NULL;

    SELECT RAISE(ABORT, '[50204] Stock mínimo inválido.')
    WHERE NEW.item_min_stock IS NULL OR NEW.item_min_stock < 0;

    SELECT RAISE(ABORT, '[50205] Precio inválido.')
    WHERE NEW.item_purchase_price IS NULL OR NEW.item_purchase_price < 0;

    SELECT RAISE(ABORT, '[50206] Artículo no encontrado o inactivo.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_items WHERE item_uuid = NEW.item_uuid AND item_active = 1
    );

    UPDATE inventory_items
    SET
        item_name = trim(NEW.item_name),
        item_description = NEW.item_description,
        item_category = trim(NEW.item_category),
        item_brand = NEW.item_brand,
        item_model = NEW.item_model,
        item_recoverable = NEW.item_recoverable,
        item_min_stock = NEW.item_min_stock,
        item_purchase_price = NEW.item_purchase_price,
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = NEW.item_uuid
      AND item_active = 1;
END;

DROP VIEW IF EXISTS sp_DeactivateInventoryItem;
CREATE VIEW sp_DeactivateInventoryItem AS
SELECT NULL AS item_uuid;

CREATE TRIGGER trg_sp_DeactivateInventoryItem INSTEAD OF INSERT ON sp_DeactivateInventoryItem
BEGIN
    SELECT RAISE(ABORT, '[50300] El artículo es obligatorio.')
    WHERE NEW.item_uuid IS NULL;

    SELECT RAISE(ABORT, '[50301] Artículo no encontrado o inactivo.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_items WHERE item_uuid = NEW.item_uuid AND item_active = 1
    );

    UPDATE inventory_items
    SET item_active = 0,
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = NEW.item_uuid
      AND item_active = 1;
END;

/* [B] */
DROP VIEW IF EXISTS sp_GetInventoryItems;
CREATE VIEW sp_GetInventoryItems AS
SELECT
    item_uuid,
    item_code,
    item_name,
    item_description,
    item_category,
    item_brand,
    item_model,
    item_recoverable,
    item_unit,
    item_stock,
    item_min_stock,
    item_purchase_price,
    item_active,
    item_created_at,
    CASE
        WHEN item_stock = 0 THEN 'OUT_OF_STOCK'
        WHEN item_stock <= item_min_stock THEN 'LOW_STOCK'
        ELSE 'AVAILABLE'
    END AS stock_status
FROM inventory_items
WHERE item_active = 1
ORDER BY item_name;

/* [B] */
DROP VIEW IF EXISTS sp_GetInventoryAlerts;
CREATE VIEW sp_GetInventoryAlerts AS
SELECT
    item_uuid,
    item_code,
    item_name,
    item_category,
    item_stock,
    item_min_stock,
    item_unit,
    CASE
        WHEN item_stock = 0 THEN 'OUT_OF_STOCK'
        ELSE 'LOW_STOCK'
    END AS stock_status
FROM inventory_items
WHERE item_active = 1
  AND item_stock <= item_min_stock
ORDER BY item_stock ASC, item_name;

/* [C] :item_uuid = NULL devuelve los movimientos de todos los artículos */
/* [sp_GetInventoryMovements]
SELECT
    m.movement_uuid,
    m.item_uuid,
    i.item_name,
    i.item_code,
    m.movement_type,
    m.movement_quantity,
    m.movement_previous_stock,
    m.movement_new_stock,
    m.movement_description,
    m.staff_uuid,
    m.order_uuid,
    m.movement_created_at
FROM inventory_movements m
INNER JOIN inventory_items i
        ON i.item_uuid = m.item_uuid
WHERE :item_uuid IS NULL
   OR m.item_uuid = :item_uuid
ORDER BY
    m.movement_created_at DESC,
    m.movement_uuid DESC;
*/

/* [A] + [C] ENTRADA y AJUSTE suman; SALIDA resta (igual que el original).
   movement_uuid es opcional: pásalo para poder consultar el resultado. */
DROP VIEW IF EXISTS sp_RegisterInventoryMovement;
CREATE VIEW sp_RegisterInventoryMovement AS
SELECT NULL AS movement_uuid, NULL AS item_uuid, NULL AS movement_type,
       NULL AS quantity, NULL AS staff_uuid, NULL AS order_uuid, NULL AS description;

CREATE TRIGGER trg_sp_RegisterInventoryMovement INSTEAD OF INSERT ON sp_RegisterInventoryMovement
BEGIN
    SELECT RAISE(ABORT, '[50100] El artículo es obligatorio.')
    WHERE NEW.item_uuid IS NULL;

    SELECT RAISE(ABORT, '[50101] La cantidad debe ser mayor a cero.')
    WHERE NEW.quantity IS NULL OR NEW.quantity <= 0;

    SELECT RAISE(ABORT, '[50102] Tipo de movimiento inválido.')
    WHERE upper(trim(COALESCE(NEW.movement_type, ''))) NOT IN ('ENTRADA', 'SALIDA', 'AJUSTE');

    SELECT RAISE(ABORT, '[50103] El artículo no existe.')
    WHERE NOT EXISTS (SELECT 1 FROM inventory_items WHERE item_uuid = NEW.item_uuid);

    SELECT RAISE(ABORT, '[50104] El artículo está desactivado.')
    WHERE EXISTS (
        SELECT 1 FROM inventory_items WHERE item_uuid = NEW.item_uuid AND item_active = 0
    );

    SELECT RAISE(ABORT, '[50105] Stock insuficiente.')
    WHERE upper(trim(NEW.movement_type)) = 'SALIDA'
      AND (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) < NEW.quantity;

    SELECT RAISE(ABORT, '[50107] El stock supera el límite permitido.')
    WHERE upper(trim(NEW.movement_type)) <> 'SALIDA'
      AND (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid)
          + NEW.quantity > 9999999999.99;

    INSERT INTO inventory_movements (
        movement_uuid, item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock, movement_description,
        staff_uuid, order_uuid
    )
    VALUES (
        COALESCE(NEW.movement_uuid, (SELECT id FROM _newid)),
        NEW.item_uuid,
        upper(trim(NEW.movement_type)),
        NEW.quantity,
        (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid),
        CASE WHEN upper(trim(NEW.movement_type)) = 'SALIDA'
             THEN round((SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) - NEW.quantity, 2)
             ELSE round((SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) + NEW.quantity, 2)
        END,
        NEW.description,
        NEW.staff_uuid,
        NEW.order_uuid
    );

    UPDATE inventory_items
    SET item_stock = CASE WHEN upper(trim(NEW.movement_type)) = 'SALIDA'
                          THEN round(item_stock - NEW.quantity, 2)
                          ELSE round(item_stock + NEW.quantity, 2)
                     END,
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = NEW.item_uuid;
END;

/* [sp_RegisterInventoryMovement - resultado] (lo que esperaba SqlInventoryRepository)
SELECT
    movement_uuid,
    movement_previous_stock AS previous_stock,
    movement_new_stock      AS new_stock
FROM inventory_movements
WHERE movement_uuid = :movement_uuid;
*/

/* [A] + [C] Registra un equipo (número de serie) y suma 1 al stock.
   equipment_uuid es opcional: pásalo para poder consultarlo después. */
DROP VIEW IF EXISTS sp_RegisterInventoryEquipment;
CREATE VIEW sp_RegisterInventoryEquipment AS
SELECT NULL AS equipment_uuid, NULL AS item_uuid, NULL AS serial_number, NULL AS staff_uuid;

CREATE TRIGGER trg_sp_RegisterInventoryEquipment INSTEAD OF INSERT ON sp_RegisterInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[51001] Artículo y número de serie obligatorios.')
    WHERE NEW.item_uuid IS NULL OR NULLIF(trim(NEW.serial_number), '') IS NULL;

    SELECT RAISE(ABORT, '[51002] Artículo recuperable inválido.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_items
        WHERE item_uuid = NEW.item_uuid
          AND item_active = 1
          AND item_recoverable = 1
          AND item_unit = 'UNIDAD'
    );

    SELECT RAISE(ABORT, '[51003] Límite de stock alcanzado.')
    WHERE (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) >= 9999999999.99;

    INSERT INTO inventory_equipment_units (equipment_uuid, item_uuid, serial_number, equipment_status)
    VALUES (
        COALESCE(NEW.equipment_uuid, (SELECT id FROM _newid)),
        NEW.item_uuid, trim(NEW.serial_number), 'AVAILABLE'
    );

    INSERT INTO inventory_movements (
        item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock,
        movement_description, staff_uuid
    )
    VALUES (
        NEW.item_uuid, 'ENTRADA', 1,
        (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid),
        round((SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) + 1, 2),
        'Registro de equipo: ' || trim(NEW.serial_number),
        NEW.staff_uuid
    );

    UPDATE inventory_items
    SET item_stock = round(item_stock + 1, 2),
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = NEW.item_uuid;

    INSERT INTO inventory_equipment_history (equipment_uuid, new_status, staff_uuid, description)
    VALUES (
        (SELECT equipment_uuid FROM inventory_equipment_units
         WHERE serial_number = trim(NEW.serial_number)),
        'AVAILABLE', NEW.staff_uuid, 'Equipo registrado en almacén'
    );
END;

/* [sp_RegisterInventoryEquipment - resultado]
SELECT equipment_uuid
FROM inventory_equipment_units
WHERE serial_number = :serial_number;
*/

/* Entrega un equipo del almacén a un técnico (descuenta 1 del stock). */
DROP VIEW IF EXISTS sp_AssignInventoryEquipment;
CREATE VIEW sp_AssignInventoryEquipment AS
SELECT NULL AS equipment_uuid, NULL AS staff_uuid;

CREATE TRIGGER trg_sp_AssignInventoryEquipment INSTEAD OF INSERT ON sp_AssignInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[51100] Equipo y técnico obligatorios.')
    WHERE NEW.equipment_uuid IS NULL OR NEW.staff_uuid IS NULL;

    SELECT RAISE(ABORT, '[51101] El equipo no está disponible.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_equipment_units
        WHERE equipment_uuid = NEW.equipment_uuid AND equipment_status = 'AVAILABLE'
    );

    SELECT RAISE(ABORT, '[51102] Stock insuficiente o artículo inactivo.')
    WHERE NOT EXISTS (
        SELECT 1
        FROM inventory_equipment_units e
        INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
        WHERE e.equipment_uuid = NEW.equipment_uuid
          AND i.item_active = 1
          AND i.item_recoverable = 1
          AND i.item_stock >= 1
    );

    INSERT INTO inventory_movements (
        item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock,
        movement_description, staff_uuid
    )
    SELECT i.item_uuid, 'SALIDA', 1, i.item_stock, round(i.item_stock - 1, 2),
           'Equipo entregado a técnico', NEW.staff_uuid
    FROM inventory_equipment_units e
    INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
    WHERE e.equipment_uuid = NEW.equipment_uuid;

    INSERT INTO inventory_equipment_history (
        equipment_uuid, previous_status, new_status, staff_uuid, description
    )
    VALUES (
        NEW.equipment_uuid, 'AVAILABLE', 'ASSIGNED', NEW.staff_uuid,
        'Equipo asignado al técnico'
    );

    UPDATE inventory_items
    SET item_stock = round(item_stock - 1, 2),
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = (SELECT item_uuid FROM inventory_equipment_units
                       WHERE equipment_uuid = NEW.equipment_uuid);

    UPDATE inventory_equipment_units
    SET equipment_status = 'ASSIGNED',
        assigned_staff_uuid = NEW.staff_uuid,
        client_dni = NULL,
        installed_order_uuid = NULL,
        installed_at = NULL,
        recovered_at = NULL
    WHERE equipment_uuid = NEW.equipment_uuid;
END;

/* Instala el equipo en un cliente. Si salía directo del almacén (AVAILABLE)
   también descuenta 1 del stock; si ya estaba ASSIGNED a ese técnico, no. */
DROP VIEW IF EXISTS sp_InstallInventoryEquipment;
CREATE VIEW sp_InstallInventoryEquipment AS
SELECT NULL AS equipment_uuid, NULL AS client_dni, NULL AS order_uuid, NULL AS staff_uuid;

CREATE TRIGGER trg_sp_InstallInventoryEquipment INSTEAD OF INSERT ON sp_InstallInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[51200] Faltan datos de instalación.')
    WHERE NEW.equipment_uuid IS NULL OR NEW.client_dni IS NULL
       OR NEW.order_uuid IS NULL OR NEW.staff_uuid IS NULL;

    SELECT RAISE(ABORT, '[51201] Equipo no encontrado.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_equipment_units WHERE equipment_uuid = NEW.equipment_uuid
    );

    SELECT RAISE(ABORT, '[51202] El equipo no puede instalarse.')
    WHERE (SELECT equipment_status FROM inventory_equipment_units
           WHERE equipment_uuid = NEW.equipment_uuid) NOT IN ('AVAILABLE', 'ASSIGNED');

    SELECT RAISE(ABORT, '[51203] El equipo pertenece a otro técnico.')
    WHERE EXISTS (
        SELECT 1 FROM inventory_equipment_units
        WHERE equipment_uuid = NEW.equipment_uuid
          AND equipment_status = 'ASSIGNED'
          AND (assigned_staff_uuid IS NULL OR assigned_staff_uuid <> NEW.staff_uuid)
    );

    SELECT RAISE(ABORT, '[51204] Cliente no encontrado.')
    WHERE NOT EXISTS (SELECT 1 FROM clients WHERE client_dni = NEW.client_dni);

    SELECT RAISE(ABORT, '[51205] Orden no encontrada.')
    WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_id = NEW.order_uuid);

    SELECT RAISE(ABORT, '[51206] Stock insuficiente.')
    WHERE EXISTS (
            SELECT 1 FROM inventory_equipment_units
            WHERE equipment_uuid = NEW.equipment_uuid AND equipment_status = 'AVAILABLE'
          )
      AND NOT EXISTS (
            SELECT 1
            FROM inventory_equipment_units e
            INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
            WHERE e.equipment_uuid = NEW.equipment_uuid
              AND i.item_active = 1
              AND i.item_recoverable = 1
              AND i.item_stock >= 1
          );

    /* Solo si sale directo de almacén: movimiento de SALIDA + descuento */
    INSERT INTO inventory_movements (
        item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock,
        movement_description, staff_uuid, order_uuid
    )
    SELECT i.item_uuid, 'SALIDA', 1, i.item_stock, round(i.item_stock - 1, 2),
           'Instalación directa desde almacén', NEW.staff_uuid, NEW.order_uuid
    FROM inventory_equipment_units e
    INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
    WHERE e.equipment_uuid = NEW.equipment_uuid
      AND e.equipment_status = 'AVAILABLE';

    UPDATE inventory_items
    SET item_stock = round(item_stock - 1, 2),
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = (SELECT item_uuid FROM inventory_equipment_units
                       WHERE equipment_uuid = NEW.equipment_uuid
                         AND equipment_status = 'AVAILABLE');

    INSERT INTO inventory_equipment_history (
        equipment_uuid, previous_status, new_status,
        staff_uuid, client_dni, order_uuid, description
    )
    SELECT NEW.equipment_uuid, equipment_status, 'INSTALLED',
           NEW.staff_uuid, NEW.client_dni, NEW.order_uuid, 'Equipo instalado en domicilio'
    FROM inventory_equipment_units
    WHERE equipment_uuid = NEW.equipment_uuid;

    UPDATE inventory_equipment_units
    SET equipment_status = 'INSTALLED',
        client_dni = NEW.client_dni,
        assigned_staff_uuid = NULL,
        installed_order_uuid = NEW.order_uuid,
        installed_at = strftime('%Y-%m-%d %H:%M:%f', 'now'),
        recovered_at = NULL
    WHERE equipment_uuid = NEW.equipment_uuid;
END;

/* Recupera un equipo instalado: pasa a MAINTENANCE en manos del técnico
   (no toca el stock). description es opcional. */
DROP VIEW IF EXISTS sp_RecoverInventoryEquipment;
CREATE VIEW sp_RecoverInventoryEquipment AS
SELECT NULL AS equipment_uuid, NULL AS staff_uuid, NULL AS description;

CREATE TRIGGER trg_sp_RecoverInventoryEquipment INSTEAD OF INSERT ON sp_RecoverInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[51300] Equipo y técnico obligatorios.')
    WHERE NEW.equipment_uuid IS NULL OR NEW.staff_uuid IS NULL;

    SELECT RAISE(ABORT, '[51301] El equipo no está instalado.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_equipment_units
        WHERE equipment_uuid = NEW.equipment_uuid
          AND equipment_status = 'INSTALLED'
          AND client_dni IS NOT NULL
    );

    INSERT INTO inventory_equipment_history (
        equipment_uuid, previous_status, new_status,
        staff_uuid, client_dni, order_uuid, description
    )
    SELECT NEW.equipment_uuid, 'INSTALLED', 'MAINTENANCE',
           NEW.staff_uuid, client_dni, installed_order_uuid,
           COALESCE(NEW.description, 'Equipo recuperado del cliente')
    FROM inventory_equipment_units
    WHERE equipment_uuid = NEW.equipment_uuid;

    UPDATE inventory_equipment_units
    SET equipment_status = 'MAINTENANCE',
        client_dni = NULL,
        assigned_staff_uuid = NEW.staff_uuid,
        installed_order_uuid = NULL,
        installed_at = NULL,
        recovered_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE equipment_uuid = NEW.equipment_uuid;
END;

/* Devuelve al almacén un equipo ASSIGNED o en MAINTENANCE (suma 1 al stock). */
DROP VIEW IF EXISTS sp_ReleaseInventoryEquipment;
CREATE VIEW sp_ReleaseInventoryEquipment AS
SELECT NULL AS equipment_uuid, NULL AS staff_uuid, NULL AS description;

CREATE TRIGGER trg_sp_ReleaseInventoryEquipment INSTEAD OF INSERT ON sp_ReleaseInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[51400] Equipo y responsable obligatorios.')
    WHERE NEW.equipment_uuid IS NULL OR NEW.staff_uuid IS NULL;

    SELECT RAISE(ABORT, '[51401] Equipo no encontrado.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_equipment_units WHERE equipment_uuid = NEW.equipment_uuid
    );

    SELECT RAISE(ABORT, '[51402] El equipo no puede devolverse.')
    WHERE (SELECT equipment_status FROM inventory_equipment_units
           WHERE equipment_uuid = NEW.equipment_uuid) NOT IN ('ASSIGNED', 'MAINTENANCE');

    SELECT RAISE(ABORT, '[51403] Artículo inactivo o inválido.')
    WHERE NOT EXISTS (
        SELECT 1
        FROM inventory_equipment_units e
        INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
        WHERE e.equipment_uuid = NEW.equipment_uuid
          AND i.item_active = 1
          AND i.item_recoverable = 1
    );

    SELECT RAISE(ABORT, '[51404] Límite de stock alcanzado.')
    WHERE (SELECT i.item_stock
           FROM inventory_equipment_units e
           INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
           WHERE e.equipment_uuid = NEW.equipment_uuid) >= 9999999999.99;

    INSERT INTO inventory_movements (
        item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock,
        movement_description, staff_uuid
    )
    SELECT i.item_uuid, 'ENTRADA', 1, i.item_stock, round(i.item_stock + 1, 2),
           'Equipo devuelto al almacén', NEW.staff_uuid
    FROM inventory_equipment_units e
    INNER JOIN inventory_items i ON i.item_uuid = e.item_uuid
    WHERE e.equipment_uuid = NEW.equipment_uuid;

    INSERT INTO inventory_equipment_history (
        equipment_uuid, previous_status, new_status, staff_uuid, description
    )
    SELECT NEW.equipment_uuid, equipment_status, 'AVAILABLE', NEW.staff_uuid,
           COALESCE(NEW.description, 'Equipo disponible nuevamente')
    FROM inventory_equipment_units
    WHERE equipment_uuid = NEW.equipment_uuid;

    UPDATE inventory_items
    SET item_stock = round(item_stock + 1, 2),
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = (SELECT item_uuid FROM inventory_equipment_units
                       WHERE equipment_uuid = NEW.equipment_uuid);

    UPDATE inventory_equipment_units
    SET equipment_status = 'AVAILABLE',
        assigned_staff_uuid = NULL,
        client_dni = NULL,
        installed_order_uuid = NULL,
        installed_at = NULL
    WHERE equipment_uuid = NEW.equipment_uuid;
END;

/* [A] + [C] Da de baja (RETIRED) automáticamente :quantity equipos
   disponibles del artículo y descuenta el stock. movement_uuid es opcional. */
DROP VIEW IF EXISTS sp_WithdrawInventoryEquipment;
CREATE VIEW sp_WithdrawInventoryEquipment AS
SELECT NULL AS movement_uuid, NULL AS item_uuid, NULL AS quantity,
       NULL AS staff_uuid, NULL AS description;

CREATE TRIGGER trg_sp_WithdrawInventoryEquipment INSTEAD OF INSERT ON sp_WithdrawInventoryEquipment
BEGIN
    SELECT RAISE(ABORT, '[52000] El artículo es obligatorio.')
    WHERE NEW.item_uuid IS NULL;

    SELECT RAISE(ABORT, '[52001] La cantidad debe ser mayor a cero.')
    WHERE NEW.quantity IS NULL OR NEW.quantity <= 0;

    SELECT RAISE(ABORT, '[52002] Artículo recuperable no encontrado o inactivo.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_items
        WHERE item_uuid = NEW.item_uuid AND item_active = 1 AND item_recoverable = 1
    );

    SELECT RAISE(ABORT, '[52003] Stock insuficiente.')
    WHERE (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) < NEW.quantity;

    SELECT RAISE(ABORT, '[52004] No hay suficientes equipos disponibles.')
    WHERE (SELECT COUNT(*) FROM inventory_equipment_units
           WHERE item_uuid = NEW.item_uuid AND equipment_status = 'AVAILABLE') < NEW.quantity;

    INSERT INTO inventory_movements (
        movement_uuid, item_uuid, movement_type, movement_quantity,
        movement_previous_stock, movement_new_stock, movement_description, staff_uuid
    )
    VALUES (
        COALESCE(NEW.movement_uuid, (SELECT id FROM _newid)),
        NEW.item_uuid, 'SALIDA', NEW.quantity,
        (SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid),
        round((SELECT item_stock FROM inventory_items WHERE item_uuid = NEW.item_uuid) - NEW.quantity, 2),
        COALESCE(NEW.description, 'Salida automática de equipos'),
        NEW.staff_uuid
    );

    /* historial primero, mientras los equipos siguen AVAILABLE */
    INSERT INTO inventory_equipment_history (
        equipment_uuid, previous_status, new_status, staff_uuid, description
    )
    SELECT equipment_uuid, 'AVAILABLE', 'RETIRED', NEW.staff_uuid,
           COALESCE(NEW.description, 'Salida automática de inventario')
    FROM inventory_equipment_units
    WHERE item_uuid = NEW.item_uuid AND equipment_status = 'AVAILABLE'
    ORDER BY equipment_uuid
    LIMIT NEW.quantity;

    UPDATE inventory_equipment_units
    SET equipment_status = 'RETIRED'
    WHERE equipment_uuid IN (
        SELECT equipment_uuid FROM inventory_equipment_units
        WHERE item_uuid = NEW.item_uuid AND equipment_status = 'AVAILABLE'
        ORDER BY equipment_uuid
        LIMIT NEW.quantity
    );

    UPDATE inventory_items
    SET item_stock = round(item_stock - NEW.quantity, 2),
        item_updated_at = strftime('%Y-%m-%d %H:%M:%f', 'now')
    WHERE item_uuid = NEW.item_uuid;
END;

/* [sp_WithdrawInventoryEquipment - resultado]
SELECT
    movement_uuid,
    movement_previous_stock AS previous_stock,
    movement_new_stock      AS new_stock
FROM inventory_movements
WHERE movement_uuid = :movement_uuid;
*/

/* [C] :item_uuid = NULL devuelve los equipos disponibles de todos los artículos */
/* [sp_GetAvailableInventoryEquipment]
SELECT
    e.equipment_uuid,
    e.item_uuid,
    i.item_code,
    i.item_name,
    e.serial_number,
    e.equipment_status
FROM inventory_equipment_units e
INNER JOIN inventory_items i
        ON i.item_uuid = e.item_uuid
WHERE e.equipment_status = 'AVAILABLE'
  AND i.item_active = 1
  AND (:item_uuid IS NULL OR e.item_uuid = :item_uuid)
ORDER BY i.item_name, e.serial_number;
*/

/* [C] */
/* [sp_GetTechnicianInventoryEquipment]
SELECT
    e.equipment_uuid,
    e.serial_number,
    e.equipment_status,
    i.item_uuid,
    i.item_name,
    i.item_code
FROM inventory_equipment_units e
INNER JOIN inventory_items i
        ON i.item_uuid = e.item_uuid
WHERE e.assigned_staff_uuid = :staff_uuid
  AND e.equipment_status IN ('ASSIGNED', 'MAINTENANCE')
ORDER BY i.item_name;
*/

/* [C] */
/* [sp_GetClientInventoryEquipment]
SELECT
    e.equipment_uuid,
    e.serial_number,
    e.equipment_status,
    e.installed_at,
    e.installed_order_uuid,
    i.item_uuid,
    i.item_name,
    i.item_code
FROM inventory_equipment_units e
INNER JOIN inventory_items i
        ON i.item_uuid = e.item_uuid
WHERE e.client_dni = :client_dni
  AND e.equipment_status = 'INSTALLED'
ORDER BY e.installed_at DESC;
*/


/* ============================================================================
   NO INCLUIDOS (comentados a propósito, igual que en el original)
   Estos 3 procedimientos usan el esquema VIEJO (o.client_dni, o.plan_id,
   o.orders_report, r.r_id, images_report, ...) que ya no existe en las tablas
   actuales, así que fallarían. Se pasan a sintaxis SQLite pero siguen
   comentados, por si quieres reescribirlos con las columnas nuevas
   (order_client_dni, order_plan_connect, etc.).
   Mes actual:  date('now','localtime','start of month')  hasta
                date('now','localtime','start of month','+1 month')
   ============================================================================ */

-- ---- sp_GetOrdersMinimized ----
/*
SELECT
    o.order_id,
    o.order_create,
    o.order_status,

    c.client_dni,
    c.client_name,
    c.client_last_name,
    c.client_phone,
    c.client_address,
    c.client_whatsapp,

    p.plan_id,
    p.plan_name,
    p.plan_speed,
    p.plan_price,
    p.plan_ispromo,

    o.orders_report AS report_id

FROM orders AS o
INNER JOIN clients AS c
        ON o.client_dni = c.client_dni
INNER JOIN plans AS p
        ON o.plan_id = p.plan_id
WHERE o.order_status = 'PENDIENTE'
ORDER BY o.order_create DESC;
*/

-- ---- sp_SearchClients (:search_by, :search_value) ----
/*
SELECT
    o.order_id,
    o.client_dni AS order_client_dni,
    o.plan_id AS order_plan_id,
    o.orders_report,
    o.order_create,
    o.order_start_install,
    o.order_end_install,
    o.order_elapsed_minutes,
    o.order_status,

    c.client_dni, c.client_name, c.client_last_name, c.client_phone,
    c.client_address, c.client_email, c.client_whatsapp, c.client_register,
    c.client_status,

    p.plan_id, p.plan_name, p.plan_created, p.plan_speed, p.plan_price, p.plan_ispromo,

    r.r_id, r.technician_id, r.r_observation, r.r_antenna_connect, r.r_create,
    r.r_status, r.r_ended, r.r_latitude, r.r_longitude,

    img.img_uuid, img.img_router, img.img_miniant, img.img_additional,

    CASE WHEN EXISTS (
        SELECT 1
        FROM payments pay
        WHERE pay.client_dni = c.client_dni
          AND pay.payment_date_paid >= date('now','localtime','start of month')
          AND pay.payment_date_paid <  date('now','localtime','start of month','+1 month')
    ) THEN 'AL_DIA'
      ELSE 'CON_DEUDA'
    END AS payment_status

FROM orders AS o
INNER JOIN clients AS c ON o.client_dni = c.client_dni
INNER JOIN plans AS p ON o.plan_id = p.plan_id
INNER JOIN orders_report AS r ON o.orders_report = r.r_id
LEFT JOIN images_report AS img ON r.r_id = img.report_id

WHERE
    CASE :search_by
        WHEN 'DNI' THEN c.client_dni
        WHEN 'NOMBRE' THEN c.client_name
        WHEN 'APELLIDO' THEN c.client_last_name
        WHEN 'DIRECCION' THEN c.client_address
        WHEN 'TELEFONO' THEN c.client_phone
        WHEN 'WHATSAPP' THEN c.client_whatsapp
        WHEN 'PLAN' THEN p.plan_name
    END LIKE '%' || :search_value || '%'

ORDER BY c.client_register DESC, o.order_id;
*/

-- ---- sp_GetOrdersExtendido ----
-- (el original primero ACTUALIZABA el estado de los clientes y luego consultaba)
/*
UPDATE clients
SET client_status =
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM payments pay
            WHERE pay.client_dni = clients.client_dni
              AND pay.payment_ispaid = 1
              AND pay.payment_date_paid >= date('now','localtime','start of month')
              AND pay.payment_date_paid <  date('now','localtime','start of month','+1 month')
        )
        THEN 'ACTIVO'
        ELSE 'SUSPENDIDO'
    END
WHERE client_status <> 'RETIRADO'
  AND EXISTS (SELECT 1 FROM orders o WHERE o.client_dni = clients.client_dni);

-- luego la misma consulta de sp_SearchClients, sin el WHERE de búsqueda y con
-- pay.payment_ispaid = 1 en el cálculo de payment_status.
*/
