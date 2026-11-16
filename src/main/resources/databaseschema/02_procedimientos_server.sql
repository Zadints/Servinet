/* =============================================================================
   SERVINET - 02_procedimientos_server.sql
   EJECUTAR DESPUES de 01_tablas_server.sql.

   - Una sola definicion por procedimiento (la version FINAL del script original).
   - Todo es CREATE OR ALTER: se puede volver a correr cuantas veces quieras.
   - Agrupado por modulo. Los procedimientos no se llaman entre si, asi que el
     orden entre ellos no importa; lo importante es que las tablas ya existan.
   ============================================================================= */

USE Servinet;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO


/* ============================================================================
   ROLES Y PERMISOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_CreateRole
    @uuid UNIQUEIDENTIFIER,
    @name VARCHAR(50),
    @hexColor VARCHAR(20),
    @permissions VARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO roles (uuid, name, hexColor)
        VALUES (@uuid, @name, @hexColor);

        INSERT INTO role_permissions (role_uuid, permission)
        SELECT
            @uuid,
            TRIM(value)
        FROM STRING_SPLIT(@permissions, ',');

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_DeleteRole
    @role_uuid UNIQUEIDENTIFIER
AS
BEGIN
SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        DELETE FROM role_permissions WHERE role_uuid = @role_uuid
        DELETE FROM roles WHERE uuid = @role_uuid
       
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_GetRoles
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        r.uuid,
        r.name,
        r.hexColor,
        rp.permission
    FROM roles r
             LEFT JOIN role_permissions rp
                       ON r.uuid = rp.role_uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateRole
    @uuid        UNIQUEIDENTIFIER,
    @name        VARCHAR(50),
    @hexColor    VARCHAR(20),
    @permissions dbo.PermissionListType READONLY
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Actualizar el rol
        UPDATE dbo.roles
        SET
            name = @name,
            hexColor = @hexColor
        WHERE uuid = @uuid;

        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50001, 'No se encontró el rol que se intenta actualizar.', 1;
        END;

        -- Eliminar permisos anteriores
        DELETE FROM dbo.role_permissions
        WHERE role_uuid = @uuid;

        -- Insertar los permisos nuevos
        INSERT INTO dbo.role_permissions (role_uuid, permission)
        SELECT @uuid, permission
        FROM @permissions;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO


/* ============================================================================
   USUARIOS / STAFF (PC)
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_getStaffIsActiveHadwareId
    @hardware_id VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
        SELECT user_uuid, expires_at 
        FROM users_session 
        WHERE hardware_id = @hardware_id
    COMMIT TRANSACTION;
END;
GO

CREATE OR ALTER PROCEDURE sp_getUserPasswordByEmail
    @user_email VARCHAR(50)

AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
        SELECT password_hash, uuid
        FROM users 
        WHERE email = @user_email

    COMMIT TRANSACTION;
END;
GO

CREATE OR ALTER PROCEDURE sp_removeStaffActiveHadwareId
    @hardware_id VARCHAR(50)

AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
        SELECT user_uuid, expires_at 
        FROM users_session 
        WHERE hardware_id = @hardware_id
    COMMIT TRANSACTION;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_getRole
    @role_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        uuid,
        name,
        hexColor
    FROM dbo.roles
    WHERE uuid = @role_uuid;

    SELECT permission
    FROM dbo.role_permissions
    WHERE role_uuid = @role_uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_getAllStaff 
AS
BEGIN
    SET NOCOUNT ON;

    SELECT * FROM users ORDER BY create_at
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_updateUser
    @uuid UNIQUEIDENTIFIER,
    @email VARCHAR(50) = NULL,
    @role_uuid UNIQUEIDENTIFIER = NULL,
    @new_password_hash VARCHAR(255) = NULL,
    @image_bytes VARBINARY(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.users
    SET
        email = COALESCE(@email, email),
        role = COALESCE(@role_uuid, role),
        password_hash = COALESCE(@new_password_hash, password_hash),
        perfil_img = COALESCE(@image_bytes, perfil_img)
    WHERE uuid = @uuid;

    SELECT @@ROWCOUNT AS affected_rows;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_getStaffAndNewSession
    @hardware_id VARCHAR(50),
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.users
            WHERE uuid = @staff_uuid
        )
        BEGIN
            ROLLBACK TRANSACTION;
            RETURN;
        END;
        IF EXISTS (
            SELECT 1
            FROM dbo.users_session
            WHERE hardware_id = @hardware_id
        )
        BEGIN
            UPDATE dbo.users_session
            SET user_uuid = @staff_uuid,
                expires_at = DATEADD(DAY, 2, SYSDATETIME())
            WHERE hardware_id = @hardware_id;
        END
        ELSE
        BEGIN
            INSERT INTO dbo.users_session (
                hardware_id,
                user_uuid
            )
            VALUES (
                @hardware_id,
                @staff_uuid
            );
        END;

        SELECT
            u.uuid,
            u.phone,
            u.display,
            u.email,
            u.role AS role_id,
            u.create_at,
            u.perfil_img
        FROM dbo.users AS u
        WHERE u.uuid = @staff_uuid;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_isNameOrEmail
    @email VARCHAR(50)
AS
BEGIN
    SELECT 1 FROM users WHERE (email = @email)
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_createUser
    @uuid UNIQUEIDENTIFIER,
    @display VARCHAR(50),
    @email VARCHAR(50),
    @phone VARCHAR(9),
    @role UNIQUEIDENTIFIER,
    @password_hash VARCHAR(255),
    @create_at DATETIME2,
    @perfil_img VARBINARY(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM dbo.users
        WHERE email = @email
    )
    BEGIN
        SELECT CAST(NULL AS UNIQUEIDENTIFIER) AS uuid;
        RETURN;
    END;

    INSERT INTO dbo.users (
        uuid,
        display,
        email,
        phone,
        role,
        password_hash,
        create_at,
        perfil_img
    )
    VALUES (
        @uuid,
        @display,
        @email,
        @phone,
        @role,
        @password_hash,
        @create_at,
        @perfil_img
    );

    SELECT @uuid AS uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_deleteUser
    @uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM dbo.users_session
        WHERE user_uuid = @uuid;

        DELETE FROM dbo.users
        WHERE uuid = @uuid;

        IF @@ROWCOUNT = 0
        BEGIN
            THROW 50001, 'No se encontró el usuario que se desea eliminar', 1;
        END;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_countUsersWithRole
    @role_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(*) AS total
    FROM dbo.users
    WHERE role = @role_uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_updatePassword
    @uuid UNIQUEIDENTIFIER,
    @new_password_hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.users
    SET password_hash = @new_password_hash
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
    BEGIN
        THROW 50002, 'No se encontró el usuario cuya contraseña se desea actualizar', 1;
    END;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_getStaffByUuid
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        uuid,
        email,
        phone,
        role AS role_id,
        create_at,
        display,
        perfil_img
    FROM dbo.users
    WHERE uuid = @staff_uuid;
END;
GO


/* ============================================================================
   LOGIN MOVIL
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_logoutSession
    @sessions_id UNIQUEIDENTIFIER,
    @user_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE api_sessions
    SET revoked_at = SYSDATETIME()
    WHERE sessions_id = @sessions_id
      AND user_uuid = @user_uuid;
END;
GO

CREATE OR ALTER PROCEDURE sp_GetSession
    @sessions_id UNIQUEIDENTIFIER,
    @access_token_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

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
    WHERE sessions_id = @sessions_id
      AND access_token_id = @access_token_id;
END;
GO

CREATE OR ALTER PROCEDURE sp_SetSession
    @sessions_id UNIQUEIDENTIFIER,
    @user_uuid UNIQUEIDENTIFIER,
    @access_token_id UNIQUEIDENTIFIER,
    @refresh_token_id UNIQUEIDENTIFIER,
    @user_agent VARCHAR(500),
    @user_ip VARCHAR(45)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
    
        INSERT INTO api_sessions (
            sessions_id,
            user_uuid,
            access_token_id,
            refresh_token_id,
            user_agent,
            user_ip
        )
        VALUES (
            @sessions_id,
            @user_uuid,
            @access_token_id,
            @refresh_token_id,
            @user_agent,
            @user_ip
        );

    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_GetUserMovil
    @user_name VARCHAR(50),
    @email VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF @user_name IS NULL
            BEGIN
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
                WHERE u.email = @email;
            END
        ELSE
            BEGIN
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
                WHERE u.display = @user_name;
            END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_GetMeMovil
    @user_uuid VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

            BEGIN
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
                WHERE u.uuid = @user_uuid;
            END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO


/* ============================================================================
   ANTENAS
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_GetClientsByAntenna
    @antenna_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT client_dni
    FROM dbo.antennasClient
    WHERE antenna_uuid = @antenna_uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AddAntennaClient
    @antenna_uuid UNIQUEIDENTIFIER,
    @client_dni VARCHAR(8)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF NOT EXISTS (
        SELECT 1
        FROM dbo.antennas
        WHERE uuid = @antenna_uuid
    )
        THROW 50001, 'La antena no existe.', 1;

    IF NOT EXISTS (
        SELECT 1
        FROM dbo.clients
        WHERE client_dni = @client_dni
    )
        THROW 50002, 'El cliente no existe.', 1;

    IF EXISTS (
        SELECT 1
        FROM dbo.antennasClient
        WHERE antenna_uuid = @antenna_uuid
          AND client_dni = @client_dni
    )
        THROW 50003, 'La relación entre antena y cliente ya existe.', 1;

    INSERT INTO dbo.antennasClient (
        antenna_uuid,
        client_dni
    )
    VALUES (
        @antenna_uuid,
        @client_dni
    );
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAntennasWithMaintenance
AS
BEGIN
    SET NOCOUNT ON;
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
    FROM dbo.antennas AS a
    ORDER BY
        a.priority DESC,
        a.name ASC;
    SELECT
        m.uuid,
        m.antenna_uuid,
        m.technical_uuid,
        m.description,
        m.date_start,
        m.date_ended,
        m.image
    FROM dbo.maintenance AS m
    INNER JOIN dbo.antennas AS a
        ON a.uuid = m.antenna_uuid
    ORDER BY
        m.date_start DESC,
        m.uuid ASC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeleteAntenna
    @uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (
            SELECT 1
            FROM dbo.antennas
            WHERE uuid = @uuid
        )
        BEGIN
            THROW 50001, 'La antena no existe.', 1;
        END;

        DELETE FROM dbo.maintenance
        WHERE antenna_uuid = @uuid;

        DELETE FROM dbo.antennas
        WHERE uuid = @uuid;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateAntenna
    @uuid UNIQUEIDENTIFIER,
    @priority INT,
    @name VARCHAR(100),
    @for_repair BIT,
    @for_maintenance BIT,
    @date_last_maintenance DATETIME2 = NULL,
    @count_days_on INT,
    @count_days_off INT,
    @status VARCHAR(30),
    @latitude DECIMAL(10, 7) = NULL,
    @longitude DECIMAL(10, 7) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    UPDATE dbo.antennas
    SET priority = @priority,
        name = @name,
        for_repair = @for_repair,
        for_maintenance = @for_maintenance,
        date_last_maintenance = @date_last_maintenance,
        count_days_on = @count_days_on,
        count_days_off = @count_days_off,
        status = @status,
        latitude = @latitude,
        longitude = @longitude
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
        THROW 50002, 'La antena no existe.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateAntennaStatus
    @uuid UNIQUEIDENTIFIER,
    @status VARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    UPDATE dbo.antennas
    SET status = @status
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
        THROW 50002, 'La antena no existe.', 1;

    SELECT status
    FROM dbo.antennas
    WHERE uuid = @uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ToggleAntennaMaintenance
    @uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    UPDATE dbo.antennas
    SET for_maintenance = CASE
        WHEN for_maintenance = 1 THEN 0
        ELSE 1
    END
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
        THROW 50002, 'La antena no existe.', 1;

    SELECT for_maintenance
    FROM dbo.antennas
    WHERE uuid = @uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_InsertAntenna
    @uuid UNIQUEIDENTIFIER,
    @priority INT,
    @name VARCHAR(100),
    @for_repair BIT,
    @for_maintenance BIT,
    @date_create DATETIME2,
    @date_last_maintenance DATETIME2 = NULL,
    @count_days_on INT = 0,
    @count_days_off INT = 0,
    @status VARCHAR(30),
    @image VARBINARY(MAX) = NULL,
    @latitude DECIMAL(10, 7) = NULL,
    @longitude DECIMAL(10, 7) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    INSERT INTO dbo.antennas (
        uuid, priority, name, for_repair, for_maintenance,
        date_create, date_last_maintenance, count_days_on,
        count_days_off, image, status, latitude, longitude
    )
    VALUES (
        @uuid, @priority, @name, @for_repair, @for_maintenance,
        @date_create, @date_last_maintenance, @count_days_on,
        @count_days_off, @image, @status, @latitude, @longitude
    );

    SELECT @uuid AS uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAntennaImage
    @uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT image
    FROM dbo.antennas
    WHERE uuid = @uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetConnectedClientsByAntenna
    @antenna_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(DISTINCT c.client_dni) AS total_connections
    FROM dbo.clients c
    INNER JOIN dbo.orders o
        ON o.order_client_dni = c.client_dni
    WHERE o.order_antenna_connect = @antenna_uuid
      AND c.client_status IN ('ACTIVO', 'SUSPENDIDO');
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAllConnectedClients
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(DISTINCT c.client_dni) AS total_connections
    FROM dbo.clients c
    INNER JOIN dbo.orders o
        ON o.order_client_dni = c.client_dni
    WHERE c.client_status IN ('ACTIVO', 'SUSPENDIDO')
      AND o.order_antenna_connect IS NOT NULL;
END;
GO


/* ============================================================================
   CONFIGURACION DE LA APP
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_GetAppGeneral
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        app_name,
        CAST(NULL AS VARBINARY(MAX)) AS app_image
    FROM dbo.appGeneral;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RenameApp
    @name VARCHAR(12)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    UPDATE dbo.appGeneral
    SET app_name = @name;

    IF @@ROWCOUNT = 0
        THROW 50010, 'No existe la configuración de la aplicación.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ChangeAppImage
    @image VARBINARY(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    UPDATE dbo.appGeneral
    SET app_image = @image;

    IF @@ROWCOUNT = 0
        THROW 50011, 'No existe la configuración de la aplicación.', 1;
END;
GO


/* ============================================================================
   PLANES
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_AddPlan
    @plan_id UNIQUEIDENTIFIER,
    @plan_name VARCHAR(8),
    @plan_created DATETIME2,
    @plan_speed INT,
    @plan_price DECIMAL(10,2),
    @plan_ispromo BIT,
    @plan_isactive BIT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    INSERT INTO dbo.plans (
        plan_id,
        plan_name,
        plan_created,
        plan_speed,
        plan_price,
        plan_ispromo,
        plan_isactive
    )
    VALUES (
        @plan_id,
        @plan_name,
        @plan_created,
        @plan_speed,
        @plan_price,
        @plan_ispromo,
        @plan_isactive
    );
END;
GO

CREATE OR ALTER PROCEDURE sp_GetAllPlans
AS
BEGIN
    SET NOCOUNT ON;

    SELECT * FROM plans;

END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeactivatePlan
    @plan_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.plans
    SET plan_isactive = 0
    WHERE plan_id = @plan_id;

    IF @@ROWCOUNT = 0
        THROW 50001, 'El plan no existe', 1;
END;
GO


/* ============================================================================
   CLIENTES Y PEDIDOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_GetClientsWithOrders
AS
BEGIN
    SET NOCOUNT ON;

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
END;
GO

CREATE OR ALTER PROCEDURE sp_CreateClientAndOrder

    @client_dni VARCHAR(8),
    @client_name VARCHAR(100),
    @client_last_name VARCHAR(100),
    @client_phone VARCHAR(9),
    @client_address VARCHAR(100),
    @client_email VARCHAR(50),
    @client_whatsapp VARCHAR(9) = NULL,

    @client_register DATETIME2,
    @client_status VARCHAR(20),

    @longitud DECIMAL(10,7) = NULL,
    @latitud DECIMAL(10,7) = NULL,

    @order_id UNIQUEIDENTIFIER,
    @order_antenna_connect UNIQUEIDENTIFIER = NULL,
    @order_plan_connect UNIQUEIDENTIFIER,
    @order_created DATETIME2,
    @order_status VARCHAR(30)

AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO clients (
            client_dni,
            client_name,
            client_last_name,
            client_phone,
            client_address,
            client_email,
            client_whatsapp,
            client_register,
            client_status,
            longitud,
            latitud
        )
        VALUES (
            @client_dni,
            @client_name,
            @client_last_name,
            @client_phone,
            @client_address,
            @client_email,
            @client_whatsapp,
            @client_register,
            @client_status,
            @longitud,
            @latitud
        );

        INSERT INTO orders (
            order_id,
            order_client_dni,
            order_antenna_connect,
            order_plan_connect,
            order_created,
            order_status
        )
        VALUES (
            @order_id,
            @client_dni,
            @order_antenna_connect,
            @order_plan_connect,
            @order_created,
            @order_status
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE sp_updateClient
    @client_dni VARCHAR(8),
    @client_phone VARCHAR(9) = NULL,
    @client_address VARCHAR(100) = NULL,
    @client_email VARCHAR(50) = NULL,
    @client_whatsapp VARCHAR(9) = NULL,
    @client_status VARCHAR(20) = NULL,
    @plan_id VARCHAR(10) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (
            SELECT 1
            FROM clients
            WHERE client_dni = @client_dni
        )
        BEGIN
            THROW 50001, 'El cliente no existe.', 1;
        END;

        IF @plan_id IS NOT NULL
           AND NOT EXISTS (
                SELECT 1
                FROM plans
                WHERE plan_id = @plan_id
           )
        BEGIN
            THROW 50002, 'El plan seleccionado no existe.', 1;
        END;

        -- Actualizar únicamente los campos enviados
        UPDATE clients
        SET
            client_phone = COALESCE(@client_phone, client_phone),
            client_address = COALESCE(@client_address, client_address),
            client_email = COALESCE(@client_email, client_email),
            client_whatsapp = COALESCE(@client_whatsapp, client_whatsapp),
            client_status = COALESCE(@client_status, client_status)
        WHERE client_dni = @client_dni;

        -- Actualizar el plan solo si se proporcionó uno
        IF @plan_id IS NOT NULL
        BEGIN
            UPDATE orders
            SET order_plan_connect = @plan_id
            WHERE order_client_dni = @client_dni;
        END;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE sp_UpdateClientStatus
    @client_dni VARCHAR(8),
    @client_status VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    IF @client_status NOT IN ('ACTIVO', 'SUSPENDIDO', 'RETIRADO')
    BEGIN
        THROW 50002, 'Estado inválido. Use ACTIVO, SUSPENDIDO o RETIRADO.', 1;
    END;

    UPDATE clients
    SET client_status = @client_status
    WHERE client_dni = @client_dni;

    IF @@ROWCOUNT = 0
    BEGIN
        THROW 50001, 'No se encontró un cliente con ese DNI.', 1;
    END;
END;
GO


/* ============================================================================
   INSTALACIONES Y TECNICOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_GetActiveInstallation
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        servicio_uuid,
        staff_uuid,
        client_dni,
        accion_tipo,
        order_id,
        description_tipo,
        current_order
    FROM dbo.tecnicos_logs
    WHERE staff_uuid = @staff_uuid
      AND current_order = 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AcceptInstallation
    @staff_uuid UNIQUEIDENTIFIER,
    @order_id UNIQUEIDENTIFIER,
    @client_dni VARCHAR(8)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF EXISTS (
            SELECT 1
            FROM dbo.tecnicos_logs WITH (UPDLOCK, HOLDLOCK)
            WHERE staff_uuid = @staff_uuid
              AND current_order = 1
        )
        BEGIN
            THROW 50001,
                'El técnico ya tiene una instalación activa.',
                1;
        END;

        IF EXISTS (
            SELECT 1
            FROM dbo.tecnicos_logs WITH (UPDLOCK, HOLDLOCK)
            WHERE order_id = @order_id
              AND current_order = 1
        )
        BEGIN
            THROW 50002,
                'Este pedido ya fue aceptado por otro técnico.',
                1;
        END;

        INSERT INTO dbo.tecnicos_logs (
            servicio_uuid,
            staff_uuid,
            client_dni,
            accion_tipo,
            order_id,
            description_tipo,
            current_order
        )
        VALUES (
            NEWID(),
            @staff_uuid,
            @client_dni,
            'INTSTALACION',
            @order_id,
            'Instalación aceptada por el técnico',
            1
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;

    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RejectInstallation
    @staff_uuid UNIQUEIDENTIFIER,
    @order_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM dbo.tecnicos_logs
        WHERE staff_uuid = @staff_uuid
          AND order_id = @order_id
          AND accion_tipo = 'INTSTALACION'
          AND current_order = 1;

        IF @@ROWCOUNT = 0
            THROW 50009, 'No tienes esta instalación asignada.', 1;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_IsInstallationInProgress
    @order_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT CAST(
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM dbo.tecnicos_logs
                WHERE order_id = @order_id
                  AND current_order = 1
                  AND accion_tipo = 'INTSTALACION'
            )
            THEN 1
            ELSE 0
        END AS BIT
    ) AS in_progress;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_FinishInstallation
    @staff_uuid UNIQUEIDENTIFIER,
    @order_id UNIQUEIDENTIFIER,
    @report_client_dni VARCHAR(8),
    @report_observation VARCHAR(MAX) = NULL,
    @img_router VARBINARY(MAX) = NULL,
    @img_conexion VARBINARY(MAX) = NULL,
    @img_mini_antenna VARBINARY(MAX) = NULL,
    @router_password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Verificar que el técnico tenga este pedido activo
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.tecnicos_logs WITH (UPDLOCK, HOLDLOCK)
            WHERE staff_uuid = @staff_uuid
              AND order_id = @order_id
              AND client_dni = @report_client_dni
              AND accion_tipo = 'INTSTALACION'
              AND current_order = 1
        )
        BEGIN
            THROW 50003,
                'No tienes esta instalación asignada.',
                1;
        END;

        -- 2. Verificar que el pedido corresponda al cliente
        IF NOT EXISTS (
            SELECT 1
            FROM dbo.orders
            WHERE order_id = @order_id
              AND order_client_dni = @report_client_dni
        )
        BEGIN
            THROW 50004,
                'El pedido no corresponde al cliente.',
                1;
        END;

        -- 3. Evitar reportes duplicados
        IF EXISTS (
            SELECT 1
            FROM dbo.orders_report
            WHERE order_id = @order_id
        )
        BEGIN
            THROW 50005,
                'Este pedido ya tiene un reporte registrado.',
                1;
        END;

        -- 4. Registrar reporte de instalación
        INSERT INTO dbo.orders_report (
            order_id,
            report_client_dni,
            report_observation,
            img_router,
            img_conexion,
            img_mini_antenna,
            router_password,
            report_status,
            report_ended
        )
        VALUES (
            @order_id,
            @report_client_dni,
            @report_observation,
            @img_router,
            @img_conexion,
            @img_mini_antenna,
            @router_password,
            'INSTALLED_ACTIVE',
            SYSDATETIME()
        );

        -- 5. Liberar al técnico
        UPDATE dbo.tecnicos_logs
        SET
            current_order = 0,
            description_tipo = 'Instalación finalizada'
        WHERE staff_uuid = @staff_uuid
          AND order_id = @order_id
          AND current_order = 1;

        IF @@ROWCOUNT <> 1
        BEGIN
            THROW 50006,
                'No se pudo liberar la instalación.',
                1;
        END;

        -- 6. Activar cliente
        UPDATE dbo.clients
        SET client_status = 'ACTIVO'
        WHERE client_dni = @report_client_dni
          AND client_status = 'EN_INSTALACION';

        IF @@ROWCOUNT <> 1
        BEGIN
            THROW 50007,
                'No se pudo activar al cliente.',
                1;
        END;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;

    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetTechnicianLogs
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

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

    FROM dbo.tecnicos_logs tl
    INNER JOIN dbo.orders o
        ON o.order_id = tl.order_id
    LEFT JOIN dbo.orders_report r
        ON r.order_id = o.order_id

    WHERE tl.staff_uuid = @staff_uuid
    ORDER BY tl.servicio_uuid DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetTechnicianLogsByClient
    @order_client_dni VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

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

    FROM dbo.orders o
    LEFT JOIN dbo.orders_report r
        ON r.order_id = o.order_id

    WHERE o.order_client_dni = @order_client_dni
    ORDER BY o.order_created DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetTechnicianStatistics
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        staff_uuid,
        COUNT(*) AS total_instalaciones
    FROM dbo.tecnicos_logs
    WHERE accion_tipo = 'INTSTALACION'
    GROUP BY staff_uuid
    ORDER BY total_instalaciones DESC;
END;
GO


/* ============================================================================
   ATENCION AL CLIENTE
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_RegisterAttentionLog
    @staff_uuid UNIQUEIDENTIFIER,
    @client_dni VARCHAR(8) = NULL,
    @client_name VARCHAR(100),
    @client_last_name VARCHAR(100),
    @atencion_tipo VARCHAR(30),
    @valoracion INT,
    @description_tipo VARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.atencion_logs (
        atencion_uuid,
        staff_uuid,
        client_dni,
        client_name,
        client_last_name,
        atencion_tipo,
        valoracion,
        description_tipo
    )
    VALUES (
        NEWID(),
        @staff_uuid,
        @client_dni,
        @client_name,
        @client_last_name,
        @atencion_tipo,
        @valoracion,
        @description_tipo
    );
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAttentionStatistics
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        staff_uuid,

        COUNT(*) AS total_atenciones,

        AVG(CAST(valoracion AS DECIMAL(10,2))) AS promedio_valoracion,

        SUM(CASE WHEN atencion_tipo = 'VENTA' THEN 1 ELSE 0 END) AS total_ventas,

        SUM(CASE WHEN atencion_tipo = 'INFORME' THEN 1 ELSE 0 END) AS total_informes,

        SUM(CASE WHEN atencion_tipo = 'SOPORTE' THEN 1 ELSE 0 END) AS total_soportes,

        SUM(CASE WHEN atencion_tipo = 'SUSPENCION' THEN 1 ELSE 0 END) AS total_suspensiones,

        SUM(CASE WHEN atencion_tipo = 'COBRO' THEN 1 ELSE 0 END) AS total_cobros,

        SUM(CASE WHEN atencion_tipo = 'ATENCION_CLIENTE' THEN 1 ELSE 0 END) AS total_atenciones_cliente

    FROM dbo.atencion_logs
    GROUP BY staff_uuid
    ORDER BY total_atenciones DESC;
END;
GO


/* ============================================================================
   PAGOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_GetAllMoney
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        ISNULL(SUM(payment_amount), 0) AS total_money
    FROM dbo.payments;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAllPayments
AS
BEGIN
    SET NOCOUNT ON;

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
    FROM dbo.payments
    ORDER BY payment_created_at DESC;
END;
GO

CREATE OR ALTER PROCEDURE sp_GetPaymentsByClient
    @client_dni VARCHAR(8)
AS
BEGIN
    SET NOCOUNT ON;

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
    FROM dbo.payments
    WHERE client_dni = @client_dni
    ORDER BY payment_created_at DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetPaymentsByClientPage
    @client_dni VARCHAR(8),
    @offset INT,
    @page_size INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @offset < 0 OR @page_size <= 0
        THROW 50001, 'Los parámetros de paginación no son válidos.', 1;

    SELECT
        payment_id,
        client_dni,
        payment_amount,
        payment_created_at,
        payment_date_paid,
        payment_method,
        payment_ispaid,
        CAST(NULL AS VARBINARY(MAX)) AS payment_image,
        payment_description,
        payment_reference
    FROM dbo.payments
    WHERE client_dni = @client_dni
    ORDER BY payment_created_at DESC, payment_id DESC
    OFFSET @offset ROWS
    FETCH NEXT @page_size ROWS ONLY;
END;
GO

CREATE OR ALTER PROCEDURE sp_CreatePendingPayment
    @client_dni VARCHAR(8),
    @payment_amount DECIMAL(10, 2),
    @payment_description VARCHAR(255) = NULL,
    @payment_reference VARCHAR(100) = NULL,
    @payment_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO payments (
        payment_id,
        client_dni,
        payment_amount,
        payment_description,
        payment_reference
    )
    VALUES (
        @payment_id,
        @client_dni,
        @payment_amount,
        @payment_description,
        @payment_reference
    );
END;
GO

CREATE OR ALTER PROCEDURE sp_CompletePayment
    @payment_id UNIQUEIDENTIFIER,
    @payment_method VARCHAR(30),
    @payment_image VARBINARY(MAX)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (
        SELECT 1
        FROM payments
        WHERE payment_id = @payment_id
    )
    BEGIN
        THROW 50001, 'El pago no existe.', 1;
    END

    IF EXISTS (
        SELECT 1
        FROM dbo.payments
        WHERE payment_id = @payment_id
          AND payment_ispaid = 1
    )
    BEGIN
        THROW 50002, 'Este pago ya fue registrado como pagado.', 1;
    END;

    UPDATE payments
    SET
        payment_date_paid = SYSDATETIME(),
        payment_method = @payment_method,
        payment_image = @payment_image,
        payment_ispaid = 1
    WHERE payment_id = @payment_id;

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
    FROM dbo.payments
    WHERE payment_id = @payment_id;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeletePayment
    @payment_id VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DELETE FROM dbo.payments
    WHERE payment_id = @payment_id;

    IF @@ROWCOUNT = 0
        THROW 50022, 'El pago no existe.', 1;
END;
GO


/* ============================================================================
   ANUNCIOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE sp_GetAnnounces
AS
BEGIN
    SELECT * FROM announce
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAnnouncesPage
    @offset INT = 0,
    @pageSize INT = 5
AS
BEGIN
    SET NOCOUNT ON;

    IF @offset < 0 OR @pageSize < 1
        THROW 50001, 'Parámetros de paginación inválidos.', 1;

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
         FROM dbo.announce_likes l
         WHERE l.announce_uuid = a.uuid) AS like_count,

        (SELECT COUNT(*)
         FROM dbo.announce_comments c
         WHERE c.announce_uuid = a.uuid) AS comment_count,

        CASE WHEN EXISTS (
            SELECT 1
            FROM dbo.announce_likes l2
            WHERE l2.announce_uuid = a.uuid
        ) THEN 1 ELSE 0 END AS liked_by_me

    FROM dbo.announce a
    ORDER BY a.sendAt DESC, a.uuid DESC
    OFFSET @offset ROWS
    FETCH NEXT @pageSize ROWS ONLY;
END;
GO

CREATE OR ALTER PROCEDURE sp_AddAnnounces

    @uuid UNIQUEIDENTIFIER,
    @priority VARCHAR(50),
    @title VARCHAR(20),
    @description VARCHAR(150), 
    @author VARCHAR(20),
    @author_uuid UNIQUEIDENTIFIER,
    @image VARBINARY(MAX) = NULL,
    @sendAt DATETIME2

AS
BEGIN
    BEGIN TRY
    
        INSERT INTO announce(
            uuid,
            priority,
            title,
            description, 
            author,
            author_uuid,
            image,
            sendAt  
        )
        VALUES (
        @uuid,
            @priority,
            @title,
            @description, 
            @author,
            @author_uuid,
            @image,
            @sendAt  
        )

    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateAnnounce
    @uuid VARCHAR(36),
    @title NVARCHAR(20),
    @description NVARCHAR(150),
    @priority VARCHAR(20),
    @replaceImage BIT,
    @newImage VARBINARY(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.announce
    SET title = @title,
        description = @description,
        priority = @priority,
        image = CASE
                    WHEN @replaceImage = 1 THEN @newImage
                    ELSE image
                END,
        update_at = SYSDATETIME()
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
        THROW 50002, 'No se encontró el anuncio que se desea actualizar.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeleteAnnounce
    @uuid VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM dbo.announce
    WHERE uuid = @uuid;

    IF @@ROWCOUNT = 0
        THROW 50001, 'No se encontró el anuncio que se desea eliminar.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ToggleAnnounceLike
    @announceUuid VARCHAR(36),
    @userUuid VARCHAR(36),
    @liked BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF EXISTS (
            SELECT 1
            FROM dbo.announce_likes WITH (UPDLOCK, HOLDLOCK)
            WHERE announce_uuid = @announceUuid
              AND user_uuid = @userUuid
        )
        BEGIN
            DELETE FROM dbo.announce_likes
            WHERE announce_uuid = @announceUuid
              AND user_uuid = @userUuid;

            SET @liked = 0;
        END
        ELSE
        BEGIN
            INSERT INTO dbo.announce_likes (
                announce_uuid,
                user_uuid
            )
            VALUES (
                @announceUuid,
                @userUuid
            );

            SET @liked = 1;
        END;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAnnounceComments
    @announceUuid VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        c.uuid,
        c.announce_uuid,
        c.user_uuid,
        u.display AS author_name,
        c.content,
        c.createAt
    FROM dbo.announce_comments c
    INNER JOIN dbo.users u
        ON u.uuid = c.user_uuid
    WHERE c.announce_uuid = @announceUuid
    ORDER BY c.createAt ASC, c.uuid ASC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AddAnnounceComment
    @uuid VARCHAR(36),
    @announceUuid VARCHAR(36),
    @userUuid VARCHAR(36),
    @content NVARCHAR(500),
    @createAt DATETIME2
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.announce_comments
        (uuid, announce_uuid, user_uuid, content, createAt)
    VALUES
        (@uuid, @announceUuid, @userUuid, @content, @createAt);
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateAnnounceComment
    @commentUuid VARCHAR(36),
    @content NVARCHAR(500)
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dbo.announce_comments
    SET content = @content
    WHERE uuid = @commentUuid;

    IF @@ROWCOUNT = 0
        THROW 50001, 'No se encontró el comentario.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeleteAnnounceComment
    @commentUuid VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM dbo.announce_comments
    WHERE uuid = @commentUuid;

    IF @@ROWCOUNT = 0
        THROW 50002, 'No se encontró el comentario.', 1;
END;
GO


/* ============================================================================
   LOGS Y ACTIVIDAD DE USUARIOS
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_InsertLog
    @user_name NVARCHAR(255),
    @log_type NVARCHAR(100),
    @information NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @user_uuid UNIQUEIDENTIFIER;

    SELECT @user_uuid = uuid
    FROM dbo.users
    WHERE display = @user_name;

    IF @user_uuid IS NULL
        THROW 50001, 'No se encontró el usuario indicado.', 1;

    INSERT INTO dbo.users_logs (
        user_uuid,
        user_name,
        log_type,
        information
    )
    VALUES (
        @user_uuid,
        @user_name,
        @log_type,
        @information
    );
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetLogs
    @limit INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @limit <= 0
        THROW 50001, 'El límite debe ser mayor que cero.', 1;

    SELECT TOP (@limit)
        user_name,
        log_type,
        information,
        create_at
    FROM dbo.users_logs
    ORDER BY create_at DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetActivityByDay
    @user_uuid UNIQUEIDENTIFIER,
    @from DATETIME2
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        CAST(create_at AS DATE) AS log_day,
        COUNT(*) AS total
    FROM dbo.users_logs
    WHERE user_uuid = @user_uuid
      AND create_at >= @from
    GROUP BY CAST(create_at AS DATE)
    ORDER BY log_day;
END;
GO

CREATE OR ALTER PROCEDURE sp_GetMeTecStats
    @user_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        SUM(CASE WHEN log_type = 'HOME_INSTALLED' THEN 1 ELSE 0 END) AS total_installed,
        SUM(CASE WHEN log_type = 'ORDER_ACCEPTED' THEN 1 ELSE 0 END) AS total_order_accepted,
        SUM(CASE WHEN log_type = 'ORDER_CANCELLED' THEN 1 ELSE 0 END) AS total_order_cancelled
    FROM users_logs
    WHERE user_uuid = @user_uuid;
END;
GO


/* ============================================================================
   INVENTARIO
   ============================================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_CreateInventoryItem
    @item_name NVARCHAR(150),
    @item_description NVARCHAR(500) = NULL,
    @item_category VARCHAR(60),
    @item_brand NVARCHAR(100) = NULL,
    @item_model NVARCHAR(100) = NULL,
    @item_recoverable BIT,
    @item_unit VARCHAR(20) = 'UNIDAD',
    @item_min_stock DECIMAL(12,2) = 5,
    @item_purchase_price DECIMAL(12,2) = 0
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

   
    SET @item_name = TRIM(@item_name);
    SET @item_category = TRIM(@item_category);
    SET @item_unit = UPPER(TRIM(@item_unit));

    

    IF @item_name IS NULL OR @item_name = ''
        THROW 50002, 'El nombre del artículo es obligatorio.', 1;

    IF @item_category IS NULL OR @item_category = ''
        THROW 50003, 'La categoría es obligatoria.', 1;

    IF @item_recoverable IS NULL
        THROW 50004, 'Debes indicar si el artículo es recuperable.', 1;

    IF @item_unit IS NULL OR
       @item_unit NOT IN ('UNIDAD','METRO','ROLLO','CAJA','PAQUETE')
        THROW 50005, 'Unidad de medida inválida.', 1;

    IF @item_min_stock IS NULL OR @item_min_stock < 0
        THROW 50006, 'El stock mínimo no puede ser negativo.', 1;

    IF @item_purchase_price IS NULL OR @item_purchase_price < 0
        THROW 50007, 'El precio no puede ser negativo.', 1;

    DECLARE @new_uuid UNIQUEIDENTIFIER = NEWID();
    DECLARE @item_code VARCHAR(40);

    SET @item_code = 'INV-' +
        RIGHT(
            '000000' + CAST(NEXT VALUE FOR dbo.seq_inventory_code AS VARCHAR(20)),
            6
        );
    INSERT INTO dbo.inventory_items (
        item_uuid,
        item_code,
        item_name,
        item_description,
        item_category,
        item_brand,
        item_model,
        item_recoverable,
        item_unit,
        item_min_stock,
        item_purchase_price
    )
    VALUES (
        @new_uuid,
        @item_code,
        @item_name,
        @item_description,
        @item_category,
        @item_brand,
        @item_model,
        @item_recoverable,
        @item_unit,
        @item_min_stock,
        @item_purchase_price
    );

    SELECT @new_uuid AS item_uuid;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_UpdateInventoryItem

    @item_uuid UNIQUEIDENTIFIER,
    @item_name NVARCHAR(150),
    @item_description NVARCHAR(500) = NULL,
    @item_category VARCHAR(60),
    @item_brand NVARCHAR(100) = NULL,
    @item_model NVARCHAR(100) = NULL,
    @item_recoverable BIT,
    @item_min_stock DECIMAL(12,2),
    @item_purchase_price DECIMAL(12,2)

AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @item_name = TRIM(@item_name);
    SET @item_category = TRIM(@item_category);

    IF @item_uuid IS NULL
        THROW 50200, 'El artículo es obligatorio.', 1;

    IF @item_name IS NULL OR @item_name = ''
        THROW 50201, 'El nombre es obligatorio.', 1;

    IF @item_category IS NULL OR @item_category = ''
        THROW 50202, 'La categoría es obligatoria.', 1;

    IF @item_recoverable IS NULL
        THROW 50203, 'El indicador recuperable es obligatorio.', 1;

    IF @item_min_stock IS NULL OR @item_min_stock < 0
        THROW 50204, 'Stock mínimo inválido.', 1;

    IF @item_purchase_price IS NULL OR @item_purchase_price < 0
        THROW 50205, 'Precio inválido.', 1;

    UPDATE dbo.inventory_items
    SET
        item_name = @item_name,
        item_description = @item_description,
        item_category = @item_category,
        item_brand = @item_brand,
        item_model = @item_model,
        item_recoverable = @item_recoverable,
        item_min_stock = @item_min_stock,
        item_purchase_price = @item_purchase_price,
        item_updated_at = SYSUTCDATETIME()
    WHERE item_uuid = @item_uuid
      AND item_active = 1;

    IF @@ROWCOUNT = 0
        THROW 50206, 'Artículo no encontrado o inactivo.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_DeactivateInventoryItem
    @item_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @item_uuid IS NULL
        THROW 50300, 'El artículo es obligatorio.', 1;

    UPDATE dbo.inventory_items
    SET
        item_active = 0,
        item_updated_at = SYSUTCDATETIME()
    WHERE item_uuid = @item_uuid
      AND item_active = 1;

    IF @@ROWCOUNT = 0
        THROW 50301, 'Artículo no encontrado o inactivo.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetInventoryItems
AS
BEGIN
    SET NOCOUNT ON;

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

    FROM dbo.inventory_items
    WHERE item_active = 1
    ORDER BY item_name;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetInventoryAlerts
AS
BEGIN
    SET NOCOUNT ON;

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

    FROM dbo.inventory_items
    WHERE item_active = 1
      AND item_stock <= item_min_stock
    ORDER BY item_stock ASC, item_name;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetInventoryMovements
    @item_uuid UNIQUEIDENTIFIER = NULL
AS
BEGIN
    SET NOCOUNT ON;

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

    FROM dbo.inventory_movements m
    INNER JOIN dbo.inventory_items i
        ON i.item_uuid = m.item_uuid

    WHERE @item_uuid IS NULL
       OR m.item_uuid = @item_uuid

    ORDER BY
        m.movement_created_at DESC,
        m.movement_uuid DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegisterInventoryMovement
    @item_uuid UNIQUEIDENTIFIER,
    @movement_type VARCHAR(20),
    @quantity DECIMAL(12,2),
    @staff_uuid UNIQUEIDENTIFIER = NULL,
    @order_uuid UNIQUEIDENTIFIER = NULL,
    @description NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @item_uuid IS NULL
        THROW 50100, 'El artículo es obligatorio.', 1;

    IF @quantity IS NULL OR @quantity <= 0
        THROW 50101, 'La cantidad debe ser mayor a cero.', 1;

    SET @movement_type = UPPER(TRIM(@movement_type));

    IF @movement_type IS NULL OR
       @movement_type NOT IN ('ENTRADA', 'SALIDA', 'AJUSTE')
        THROW 50102, 'Tipo de movimiento inválido.', 1;

    DECLARE @previous_stock DECIMAL(12,2);
    DECLARE @new_stock DECIMAL(12,2);
    DECLARE @active BIT;
    DECLARE @recoverable BIT;
    DECLARE @movement_uuid UNIQUEIDENTIFIER = NEWID();

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT
            @previous_stock = item_stock,
            @active = item_active,
            @recoverable = item_recoverable
        FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
        WHERE item_uuid = @item_uuid;

        IF @previous_stock IS NULL
            THROW 50103, 'El artículo no existe.', 1;

       /* IF @recoverable = 1
            THROW 50106,
                'Los equipos recuperables requieren movimientos individuales.',
                1;*/

        IF @active = 0
            THROW 50104, 'El artículo está desactivado.', 1;

        IF @movement_type IN ('ENTRADA', 'AJUSTE')
        BEGIN
            SET @new_stock = @previous_stock + @quantity;
        END
        ELSE
        BEGIN
            IF @previous_stock < @quantity
                THROW 50105, 'Stock insuficiente.', 1;

            SET @new_stock = @previous_stock - @quantity;
        END;

        IF @new_stock > 9999999999.99
            THROW 50107, 'El stock supera el límite permitido.', 1;

        UPDATE dbo.inventory_items
        SET
            item_stock = @new_stock,
            item_updated_at = SYSUTCDATETIME()
        WHERE item_uuid = @item_uuid;

        INSERT INTO dbo.inventory_movements (
            movement_uuid,
            item_uuid,
            movement_type,
            movement_quantity,
            movement_previous_stock,
            movement_new_stock,
            movement_description,
            staff_uuid,
            order_uuid
        )
        VALUES (
            @movement_uuid,
            @item_uuid,
            @movement_type,
            @quantity,
            @previous_stock,
            @new_stock,
            @description,
            @staff_uuid,
            @order_uuid
        );

        COMMIT TRANSACTION;

        -- Resultado esperado por SqlInventoryRepository
        SELECT
            @movement_uuid AS movement_uuid,
            @previous_stock AS previous_stock,
            @new_stock AS new_stock;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;

    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegisterInventoryEquipment
    @item_uuid UNIQUEIDENTIFIER,
    @serial_number VARCHAR(100),
    @staff_uuid UNIQUEIDENTIFIER = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @serial_number = NULLIF(TRIM(@serial_number), '');

    IF @item_uuid IS NULL OR @serial_number IS NULL
        THROW 51001, 'Artículo y número de serie obligatorios.', 1;

    DECLARE @previous_stock DECIMAL(12,2);
    DECLARE @new_stock DECIMAL(12,2);
    DECLARE @equipment_uuid UNIQUEIDENTIFIER = NEWID();

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @previous_stock = item_stock
        FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
        WHERE item_uuid = @item_uuid
          AND item_active = 1
          AND item_recoverable = 1
          AND item_unit = 'UNIDAD';

        IF @previous_stock IS NULL
            THROW 51002, 'Artículo recuperable inválido.', 1;

        IF @previous_stock >= 9999999999.99
            THROW 51003, 'Límite de stock alcanzado.', 1;

        SET @new_stock = @previous_stock + 1;

        INSERT INTO dbo.inventory_equipment_units (
            equipment_uuid,
            item_uuid,
            serial_number,
            equipment_status
        )
        VALUES (
            @equipment_uuid,
            @item_uuid,
            @serial_number,
            'AVAILABLE'
        );

        UPDATE dbo.inventory_items
        SET item_stock = @new_stock,
            item_updated_at = SYSUTCDATETIME()
        WHERE item_uuid = @item_uuid;

        INSERT INTO dbo.inventory_movements (
            item_uuid, movement_type, movement_quantity,
            movement_previous_stock, movement_new_stock,
            movement_description, staff_uuid
        )
        VALUES (
            @item_uuid, 'ENTRADA', 1,
            @previous_stock, @new_stock,
            N'Registro de equipo: ' + @serial_number,
            @staff_uuid
        );

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid, new_status, staff_uuid, description
        )
        VALUES (
            @equipment_uuid, 'AVAILABLE', @staff_uuid,
            N'Equipo registrado en almacén'
        );

        COMMIT TRANSACTION;

        SELECT @equipment_uuid AS equipment_uuid;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AssignInventoryEquipment
    @equipment_uuid UNIQUEIDENTIFIER,
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @equipment_uuid IS NULL OR @staff_uuid IS NULL
        THROW 51100, 'Equipo y técnico obligatorios.', 1;

    DECLARE @item_uuid UNIQUEIDENTIFIER;
    DECLARE @previous_stock DECIMAL(12,2);

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @item_uuid = item_uuid
        FROM dbo.inventory_equipment_units WITH (UPDLOCK, HOLDLOCK)
        WHERE equipment_uuid = @equipment_uuid
          AND equipment_status = 'AVAILABLE';

        IF @item_uuid IS NULL
            THROW 51101, 'El equipo no está disponible.', 1;

        SELECT @previous_stock = item_stock
        FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
        WHERE item_uuid = @item_uuid
          AND item_active = 1
          AND item_recoverable = 1;

        IF @previous_stock IS NULL OR @previous_stock < 1
            THROW 51102, 'Stock insuficiente o artículo inactivo.', 1;

        UPDATE dbo.inventory_equipment_units
        SET equipment_status = 'ASSIGNED',
            assigned_staff_uuid = @staff_uuid,
            client_dni = NULL,
            installed_order_uuid = NULL,
            installed_at = NULL,
            recovered_at = NULL
        WHERE equipment_uuid = @equipment_uuid;

        UPDATE dbo.inventory_items
        SET item_stock = item_stock - 1,
            item_updated_at = SYSUTCDATETIME()
        WHERE item_uuid = @item_uuid;

        INSERT INTO dbo.inventory_movements (
            item_uuid, movement_type, movement_quantity,
            movement_previous_stock, movement_new_stock,
            movement_description, staff_uuid
        )
        VALUES (
            @item_uuid, 'SALIDA', 1,
            @previous_stock, @previous_stock - 1,
            N'Equipo entregado a técnico', @staff_uuid
        );

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid, previous_status, new_status,
            staff_uuid, description
        )
        VALUES (
            @equipment_uuid, 'AVAILABLE', 'ASSIGNED',
            @staff_uuid, N'Equipo asignado al técnico'
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_InstallInventoryEquipment
    @equipment_uuid UNIQUEIDENTIFIER,
    @client_dni VARCHAR(8),
    @order_uuid UNIQUEIDENTIFIER,
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @equipment_uuid IS NULL
       OR @client_dni IS NULL
       OR @order_uuid IS NULL
       OR @staff_uuid IS NULL
        THROW 51200, 'Faltan datos de instalación.', 1;

    DECLARE @item_uuid UNIQUEIDENTIFIER;
    DECLARE @status VARCHAR(20);
    DECLARE @assigned_staff UNIQUEIDENTIFIER;
    DECLARE @previous_stock DECIMAL(12,2);

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT
            @item_uuid = item_uuid,
            @status = equipment_status,
            @assigned_staff = assigned_staff_uuid
        FROM dbo.inventory_equipment_units WITH (UPDLOCK, HOLDLOCK)
        WHERE equipment_uuid = @equipment_uuid;

        IF @item_uuid IS NULL
            THROW 51201, 'Equipo no encontrado.', 1;

        IF @status NOT IN ('AVAILABLE', 'ASSIGNED')
            THROW 51202, 'El equipo no puede instalarse.', 1;

        IF @status = 'ASSIGNED'
           AND (@assigned_staff IS NULL OR @assigned_staff <> @staff_uuid)
            THROW 51203, 'El equipo pertenece a otro técnico.', 1;

        -- Validación básica de referencias.
        IF NOT EXISTS (
            SELECT 1 FROM dbo.clients WHERE client_dni = @client_dni
        )
            THROW 51204, 'Cliente no encontrado.', 1;

        IF NOT EXISTS (
            SELECT 1 FROM dbo.orders WHERE order_id = @order_uuid
        )
            THROW 51205, 'Orden no encontrada.', 1;

        -- Si sale directamente de almacén, descontar stock.
        IF @status = 'AVAILABLE'
        BEGIN
            SELECT @previous_stock = item_stock
            FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
            WHERE item_uuid = @item_uuid
              AND item_active = 1
              AND item_recoverable = 1;

            IF @previous_stock IS NULL OR @previous_stock < 1
                THROW 51206, 'Stock insuficiente.', 1;

            UPDATE dbo.inventory_items
            SET item_stock = item_stock - 1,
                item_updated_at = SYSUTCDATETIME()
            WHERE item_uuid = @item_uuid;

            INSERT INTO dbo.inventory_movements (
                item_uuid, movement_type, movement_quantity,
                movement_previous_stock, movement_new_stock,
                movement_description, staff_uuid, order_uuid
            )
            VALUES (
                @item_uuid, 'SALIDA', 1,
                @previous_stock, @previous_stock - 1,
                N'Instalación directa desde almacén',
                @staff_uuid, @order_uuid
            );
        END;

        UPDATE dbo.inventory_equipment_units
        SET equipment_status = 'INSTALLED',
            client_dni = @client_dni,
            assigned_staff_uuid = NULL,
            installed_order_uuid = @order_uuid,
            installed_at = SYSUTCDATETIME(),
            recovered_at = NULL
        WHERE equipment_uuid = @equipment_uuid;

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid, previous_status, new_status,
            staff_uuid, client_dni, order_uuid, description
        )
        VALUES (
            @equipment_uuid, @status, 'INSTALLED',
            @staff_uuid, @client_dni, @order_uuid,
            N'Equipo instalado en domicilio'
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RecoverInventoryEquipment
    @equipment_uuid UNIQUEIDENTIFIER,
    @staff_uuid UNIQUEIDENTIFIER,
    @description NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @equipment_uuid IS NULL OR @staff_uuid IS NULL
        THROW 51300, 'Equipo y técnico obligatorios.', 1;

    DECLARE @client_dni VARCHAR(8);
    DECLARE @order_uuid UNIQUEIDENTIFIER;

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT
            @client_dni = client_dni,
            @order_uuid = installed_order_uuid
        FROM dbo.inventory_equipment_units WITH (UPDLOCK, HOLDLOCK)
        WHERE equipment_uuid = @equipment_uuid
          AND equipment_status = 'INSTALLED';

        IF @client_dni IS NULL
            THROW 51301, 'El equipo no está instalado.', 1;

        UPDATE dbo.inventory_equipment_units
        SET equipment_status = 'MAINTENANCE',
            client_dni = NULL,
            assigned_staff_uuid = @staff_uuid,
            installed_order_uuid = NULL,
            installed_at = NULL,
            recovered_at = SYSUTCDATETIME()
        WHERE equipment_uuid = @equipment_uuid;

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid, previous_status, new_status,
            staff_uuid, client_dni, order_uuid, description
        )
        VALUES (
            @equipment_uuid, 'INSTALLED', 'MAINTENANCE',
            @staff_uuid, @client_dni, @order_uuid,
            COALESCE(@description, N'Equipo recuperado del cliente')
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ReleaseInventoryEquipment
    @equipment_uuid UNIQUEIDENTIFIER,
    @staff_uuid UNIQUEIDENTIFIER,
    @description NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @equipment_uuid IS NULL OR @staff_uuid IS NULL
        THROW 51400, 'Equipo y responsable obligatorios.', 1;

    DECLARE @item_uuid UNIQUEIDENTIFIER;
    DECLARE @previous_status VARCHAR(20);
    DECLARE @previous_stock DECIMAL(12,2);

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT
            @item_uuid = item_uuid,
            @previous_status = equipment_status
        FROM dbo.inventory_equipment_units WITH (UPDLOCK, HOLDLOCK)
        WHERE equipment_uuid = @equipment_uuid;

        IF @item_uuid IS NULL
            THROW 51401, 'Equipo no encontrado.', 1;

        IF @previous_status NOT IN ('ASSIGNED', 'MAINTENANCE')
            THROW 51402, 'El equipo no puede devolverse.', 1;

        SELECT @previous_stock = item_stock
        FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
        WHERE item_uuid = @item_uuid
          AND item_active = 1
          AND item_recoverable = 1;

        IF @previous_stock IS NULL
            THROW 51403, 'Artículo inactivo o inválido.', 1;

        IF @previous_stock >= 9999999999.99
            THROW 51404, 'Límite de stock alcanzado.', 1;

        UPDATE dbo.inventory_equipment_units
        SET equipment_status = 'AVAILABLE',
            assigned_staff_uuid = NULL,
            client_dni = NULL,
            installed_order_uuid = NULL,
            installed_at = NULL
        WHERE equipment_uuid = @equipment_uuid;

        UPDATE dbo.inventory_items
        SET item_stock = item_stock + 1,
            item_updated_at = SYSUTCDATETIME()
        WHERE item_uuid = @item_uuid;

        INSERT INTO dbo.inventory_movements (
            item_uuid, movement_type, movement_quantity,
            movement_previous_stock, movement_new_stock,
            movement_description, staff_uuid
        )
        VALUES (
            @item_uuid, 'ENTRADA', 1,
            @previous_stock, @previous_stock + 1,
            N'Equipo devuelto al almacén', @staff_uuid
        );

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid, previous_status, new_status,
            staff_uuid, description
        )
        VALUES (
            @equipment_uuid, @previous_status, 'AVAILABLE',
            @staff_uuid,
            COALESCE(@description, N'Equipo disponible nuevamente')
        );

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_WithdrawInventoryEquipment
    @item_uuid UNIQUEIDENTIFIER,
    @quantity INT,
    @staff_uuid UNIQUEIDENTIFIER = NULL,
    @description NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @item_uuid IS NULL
        THROW 52000, 'El artículo es obligatorio.', 1;

    IF @quantity IS NULL OR @quantity <= 0
        THROW 52001, 'La cantidad debe ser mayor a cero.', 1;

    DECLARE @previous_stock DECIMAL(12,2);
    DECLARE @new_stock DECIMAL(12,2);
    DECLARE @movement_uuid UNIQUEIDENTIFIER = NEWID();

    DECLARE @withdrawn TABLE (
        equipment_uuid UNIQUEIDENTIFIER PRIMARY KEY
    );

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @previous_stock = item_stock
        FROM dbo.inventory_items WITH (UPDLOCK, HOLDLOCK)
        WHERE item_uuid = @item_uuid
          AND item_active = 1
          AND item_recoverable = 1;

        IF @previous_stock IS NULL
            THROW 52002, 'Artículo recuperable no encontrado o inactivo.', 1;

        IF @previous_stock < @quantity
            THROW 52003, 'Stock insuficiente.', 1;

        -- Elegir automáticamente la cantidad solicitada
        ;WITH SelectedEquipment AS (
            SELECT TOP (@quantity)
                equipment_uuid,
                equipment_status
            FROM dbo.inventory_equipment_units
                WITH (UPDLOCK, HOLDLOCK)
            WHERE item_uuid = @item_uuid
              AND equipment_status = 'AVAILABLE'
            ORDER BY equipment_uuid
        )
        UPDATE SelectedEquipment
        SET equipment_status = 'RETIRED'
        OUTPUT inserted.equipment_uuid
        INTO @withdrawn (equipment_uuid);

        IF (SELECT COUNT(*) FROM @withdrawn) <> @quantity
            THROW 52004, 'No hay suficientes equipos disponibles.', 1;

        SET @new_stock = @previous_stock - @quantity;

        UPDATE dbo.inventory_items
        SET item_stock = @new_stock,
            item_updated_at = SYSUTCDATETIME()
        WHERE item_uuid = @item_uuid;

        INSERT INTO dbo.inventory_movements (
            movement_uuid,
            item_uuid,
            movement_type,
            movement_quantity,
            movement_previous_stock,
            movement_new_stock,
            movement_description,
            staff_uuid
        )
        VALUES (
            @movement_uuid,
            @item_uuid,
            'SALIDA',
            @quantity,
            @previous_stock,
            @new_stock,
            COALESCE(@description, N'Salida automática de equipos'),
            @staff_uuid
        );

        INSERT INTO dbo.inventory_equipment_history (
            equipment_uuid,
            previous_status,
            new_status,
            staff_uuid,
            description
        )
        SELECT
            equipment_uuid,
            'AVAILABLE',
            'RETIRED',
            @staff_uuid,
            COALESCE(@description, N'Salida automática de inventario')
        FROM @withdrawn;

        COMMIT TRANSACTION;

        SELECT
            @movement_uuid AS movement_uuid,
            @previous_stock AS previous_stock,
            @new_stock AS new_stock;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetAvailableInventoryEquipment
    @item_uuid UNIQUEIDENTIFIER = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        e.equipment_uuid,
        e.item_uuid,
        i.item_code,
        i.item_name,
        e.serial_number,
        e.equipment_status
    FROM dbo.inventory_equipment_units e
    INNER JOIN dbo.inventory_items i
        ON i.item_uuid = e.item_uuid
    WHERE e.equipment_status = 'AVAILABLE'
      AND i.item_active = 1
      AND (@item_uuid IS NULL OR e.item_uuid = @item_uuid)
    ORDER BY i.item_name, e.serial_number;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetTechnicianInventoryEquipment
    @staff_uuid UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        e.equipment_uuid,
        e.serial_number,
        e.equipment_status,
        i.item_uuid,
        i.item_name,
        i.item_code
    FROM dbo.inventory_equipment_units e
    INNER JOIN dbo.inventory_items i
        ON i.item_uuid = e.item_uuid
    WHERE e.assigned_staff_uuid = @staff_uuid
      AND e.equipment_status IN ('ASSIGNED', 'MAINTENANCE')
    ORDER BY i.item_name;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_GetClientInventoryEquipment
    @client_dni VARCHAR(8)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        e.equipment_uuid,
        e.serial_number,
        e.equipment_status,
        e.installed_at,
        e.installed_order_uuid,
        i.item_uuid,
        i.item_name,
        i.item_code
    FROM dbo.inventory_equipment_units e
    INNER JOIN dbo.inventory_items i
        ON i.item_uuid = e.item_uuid
    WHERE e.client_dni = @client_dni
      AND e.equipment_status = 'INSTALLED'
    ORDER BY e.installed_at DESC;
END;
GO


/* ============================================================================
   NO INCLUIDOS (comentados a proposito)
   Estos 3 procedimientos usan el esquema VIEJO (o.client_dni, o.plan_id,
   o.orders_report, r.r_id, images_report, ...) que ya no existe en las tablas
   actuales, asi que fallan al crearse. Estan aqui por si quieres reescribirlos
   con las columnas nuevas (order_client_dni, order_plan_connect, etc.).
   ============================================================================

-- ---- sp_GetOrdersMinimized ----
/*
CREATE OR ALTER PROCEDURE sp_GetOrdersMinimized
AS
BEGIN
    SET NOCOUNT ON;

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
END;
*/

-- ---- sp_SearchClients ----
/*
CREATE OR ALTER PROCEDURE dbo.sp_SearchClients
    @search_by VARCHAR(30),
    @search_value VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        -- ORDERS
        o.order_id,
        o.client_dni AS order_client_dni,
        o.plan_id AS order_plan_id,
        o.orders_report,
        o.order_create,
        o.order_start_install,
        o.order_end_install,
        o.order_elapsed_minutes,
        o.order_status,

        -- CLIENTS
        c.client_dni,
        c.client_name,
        c.client_last_name,
        c.client_phone,
        c.client_address,
        c.client_email,
        c.client_whatsapp,
        c.client_register,
        c.client_status,

        -- PLANS
        p.plan_id,
        p.plan_name,
        p.plan_created,
        p.plan_speed,
        p.plan_price,
        p.plan_ispromo,

        -- ORDERS REPORT
        r.r_id,
        r.technician_id,
        r.r_observation,
        r.r_antenna_connect,
        r.r_create,
        r.r_status,
        r.r_ended,
        r.r_latitude,
        r.r_longitude,

        -- IMÁGENES
        img.img_uuid,
        img.img_router,
        img.img_miniant,
        img.img_additional,

        -- ESTADO DE PAGO
        CASE WHEN EXISTS (
            SELECT 1
            FROM dbo.payments pay
            WHERE pay.client_dni = c.client_dni
              AND pay.payment_date_paid >= DATEADD(
                  MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0
              )
              AND pay.payment_date_paid < DATEADD(
                  MONTH, DATEDIFF(MONTH, 0, GETDATE()) + 1, 0
              )
        ) THEN 'AL_DIA'
          ELSE 'CON_DEUDA'
        END AS payment_status

    FROM dbo.orders AS o

    INNER JOIN dbo.clients AS c
        ON o.client_dni = c.client_dni

    INNER JOIN dbo.plans AS p
        ON o.plan_id = p.plan_id

    INNER JOIN dbo.orders_report AS r
        ON o.orders_report = r.r_id

    LEFT JOIN dbo.images_report AS img
        ON r.r_id = img.report_id

    WHERE
        CASE
            WHEN @search_by = 'DNI' THEN c.client_dni
            WHEN @search_by = 'NOMBRE' THEN c.client_name
            WHEN @search_by = 'APELLIDO' THEN c.client_last_name
            WHEN @search_by = 'DIRECCION' THEN c.client_address
            WHEN @search_by = 'TELEFONO' THEN c.client_phone
            WHEN @search_by = 'WHATSAPP' THEN c.client_whatsapp
            WHEN @search_by = 'PLAN' THEN p.plan_name
        END LIKE '%' + @search_value + '%'

    ORDER BY c.client_register DESC, o.order_id;
END;
*/

-- ---- sp_GetOrdersExtendido ----
/*
CREATE OR ALTER PROCEDURE sp_GetOrdersExtendido
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @fechaInicio DATETIME2 =
        DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0);

    DECLARE @fechaFin DATETIME2 =
        DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) + 1, 0);

    -- ACTUALIZAR ESTADO DE LOS CLIENTES
    UPDATE c
    SET c.client_status =
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM dbo.payments pay
                WHERE pay.client_dni = c.client_dni
                  AND pay.payment_ispaid = 1
                  AND pay.payment_date_paid >= @fechaInicio
                  AND pay.payment_date_paid < @fechaFin
            )
            THEN 'ACTIVO'
            ELSE 'SUSPENDIDO'
        END
    FROM dbo.clients c
    WHERE c.client_status <> 'RETIRADO'
      AND EXISTS (
          SELECT 1
          FROM dbo.orders o
          WHERE o.client_dni = c.client_dni
      );

    -- CONSULTAR ÓRDENES
    SELECT
        -- ORDERS
        o.order_id,
        o.client_dni AS order_client_dni,
        o.plan_id AS order_plan_id,
        o.orders_report,
        o.order_create,
        o.order_start_install,
        o.order_end_install,
        o.order_elapsed_minutes,
        o.order_status,

        -- CLIENTS
        c.client_dni,
        c.client_name,
        c.client_last_name,
        c.client_phone,
        c.client_address,
        c.client_email,
        c.client_whatsapp,
        c.client_register,
        c.client_status,

        -- PLANS
        p.plan_id,
        p.plan_name,
        p.plan_created,
        p.plan_speed,
        p.plan_price,
        p.plan_ispromo,

        -- ORDERS REPORT
        r.r_id,
        r.technician_id,
        r.r_observation,
        r.r_antenna_connect,
        r.r_create,
        r.r_status,
        r.r_ended,
        r.r_latitude,
        r.r_longitude,

        -- IMÁGENES DEL REPORTE
        img.img_uuid,
        img.img_router,
        img.img_miniant,
        img.img_additional,

        -- ESTADO DEL PAGO
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM dbo.payments pay
                WHERE pay.client_dni = c.client_dni
                  AND pay.payment_ispaid = 1
                  AND pay.payment_date_paid >= @fechaInicio
                  AND pay.payment_date_paid < @fechaFin
            )
            THEN 'AL_DIA'
            ELSE 'CON_DEUDA'
        END AS payment_status

    FROM orders AS o

    INNER JOIN clients AS c
        ON o.client_dni = c.client_dni

    INNER JOIN plans AS p
        ON o.plan_id = p.plan_id

    INNER JOIN orders_report AS r
        ON o.orders_report = r.r_id

    LEFT JOIN images_report AS img
        ON r.r_id = img.report_id

    ORDER BY c.client_register DESC, o.order_id;

END;
*/
