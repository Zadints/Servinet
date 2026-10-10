USE Servinet;
GO





CREATE TABLE roles(
                      uuid UNIQUEIDENTIFIER NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      hexColor VARCHAR(20) NOT NULL,
                      CONSTRAINT PK_roles PRIMARY KEY (uuid)
)
ALTER TABLE roles
    ADD CONSTRAINT UQ_roles_name UNIQUE (name);

CREATE TABLE role_permissions (
                                  role_uuid UNIQUEIDENTIFIER NOT NULL,
                                  permission VARCHAR(50) NOT NULL,
                                  CONSTRAINT PK_role_permissions PRIMARY KEY (role_uuid, permission),
                                  CONSTRAINT FK_role_permissions FOREIGN KEY (role_uuid) REFERENCES roles(uuid)
);

SELECT * FROM users;
GO

CREATE TABLE users (
                       uuid UNIQUEIDENTIFIER NOT NULL,
                       display VARCHAR(50) NOT NULL,
                       email VARCHAR(50) NOT NULL,
                       role UNIQUEIDENTIFIER NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       create_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                       perfil_img VARBINARY(MAX) NULL,
                       CONSTRAINT PK_uuid PRIMARY KEY (uuid),
                       CONSTRAINT UQ_usuarios_display UNIQUE (display),
                       CONSTRAINT UQ_usuarios_email UNIQUE (email),
                       CONSTRAINT FK_users FOREIGN KEY(role) REFERENCES roles(uuid)
);





CREATE TABLE users_session (
                               hardware_id VARCHAR(50) NOT NULL,
                               user_uuid UNIQUEIDENTIFIER NOT NULL,
                               expires_at DATETIME2 NOT NULL

                                   CONSTRAINT DF_users_session_expires_at DEFAULT DATEADD(DAY, 2, SYSDATETIME()),
                               CONSTRAINT PK_uuidhardware PRIMARY KEY (hardware_id),
                               CONSTRAINT FK_user_uuid FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);
go

SELECT * FROM api_sessions;
GO

CREATE TABLE api_sessions (
                              sessions_id UNIQUEIDENTIFIER NOT NULL,
                              user_uuid UNIQUEIDENTIFIER NOT NULL,

                              access_token_id UNIQUEIDENTIFIER NOT NULL,
                              refresh_token_id UNIQUEIDENTIFIER NOT NULL,

                              user_agent VARCHAR(500) NULL,
                              user_ip VARCHAR(45) NULL,

                              created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                              expires_at DATETIME2 NULL ,
                              revoked_at DATETIME2 NULL,

                              CONSTRAINT PK_api_sessions PRIMARY KEY (sessions_id),
                              CONSTRAINT FK_api_sessions_user FOREIGN KEY (user_uuid) REFERENCES users(uuid)
);
go



ALTER TABLE api_sessions
    ADD CONSTRAINT DF_api_sessions_expires_at
        DEFAULT DATEADD(MINUTE, 60, SYSDATETIME())
        FOR expires_at;
GO

CREATE TABLE antennas (
                          uuid UNIQUEIDENTIFIER NOT NULL,
                          priority INT NOT NULL DEFAULT 0,
                          name VARCHAR(100) NOT NULL,
                          for_repair BIT NOT NULL,
                          for_maintenance BIT NOT NULL,
                          date_create DATETIME2 NOT NULL,
                          date_last_maintenance DATETIME2 NULL,
                          count_days_on INT NOT NULL DEFAULT 0,
                          count_days_off INT NOT NULL DEFAULT 0,
                          image VARBINARY(MAX) NULL,
                          status VARCHAR(30) NOT NULL

                              CONSTRAINT PK_antennas PRIMARY KEY (uuid),
                          CONSTRAINT UQ_name UNIQUE (name ),
);
ALTER TABLE antennas
    ADD
        latitude DECIMAL(10, 7) NULL,
        longitude DECIMAL(10, 7) NULL
GO

CREATE TABLE appGeneral (
                            name VARCHAR(12) NOT NULL
);

/*==================================== CLIENTES Y VENTAS PAPARTADOS =======================================*/


CREATE TABLE plans (
                       plan_id VARCHAR(10) NOT NULL,
                       plan_name VARCHAR(8) NOT NULL,
                       plan_created DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                       plan_speed INT NOT NULL,
                       plan_price DECIMAL(10, 2) NOT NULL,
                       plan_ispromo BIT NOT NULL DEFAULT 0,

                       CONSTRAINT PK_plan_id PRIMARY KEY (plan_id),
);

DROP TABLE cliente;
GO

CREATE TABLE clients (
                         client_dni VARCHAR(8) NOT NULL,
                         client_name VARCHAR(8) NOT NULL,
                         client_last_name VARCHAR(8) NOT NULL,
                         client_phone VARCHAR(9) NOT NULL,
                         client_address VARCHAR(100) NOT NULL,
                         client_email VARCHAR(50) NOT NULL,
                         client_whatsapp VARCHAR(9) NULL,
                         client_register DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                         CONSTRAINT PK_orders PRIMARY KEY (client_dni),
                         CONSTRAINT UQ_client_name_last_name UNIQUE (client_name, client_last_name)
);

ALTER TABLE clients
    ALTER COLUMN client_name VARCHAR(100) NOT NULL;

ALTER TABLE clients
    ALTER COLUMN client_last_name VARCHAR(100) NOT NULL;

ALTER TABLE clients
    ALTER COLUMN client_whatsapp VARCHAR(9) NOT NULL;
GO


CREATE TABLE orders_report (
                               r_id UNIQUEIDENTIFIER NOT NULL,
                               technician_id UNIQUEIDENTIFIER NULL,

                               r_observation VARCHAR(MAX) NULL,
                               r_antenna_connect UNIQUEIDENTIFIER NULL,

                               r_create DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                               r_status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
                               r_ended DATETIME2 NULL,

                               r_latitude DECIMAL(10, 7) NULL,
                               r_longitude DECIMAL(10, 7) NULL,

                               CONSTRAINT PK_orders_report PRIMARY KEY (r_id),
                               CONSTRAINT FK_technician_id FOREIGN KEY (technician_id) REFERENCES users(uuid),
                               CONSTRAINT FK_antenna_connect FOREIGN KEY (r_antenna_connect) REFERENCES antennas(uuid)
);

ALTER TABLE orders_report
    ALTER COLUMN r_antenna_connect UNIQUEIDENTIFIER NULL;
GO

ALTER TABLE orders_report
    ALTER COLUMN technician_id UNIQUEIDENTIFIER NULL;
GO
ALTER TABLE orders_report
    ADD CONSTRAINT DF_r_id
        DEFAULT NEWID() FOR r_id;
GO

CREATE TABLE images_report (
                               img_uuid UNIQUEIDENTIFIER NOT NULL,
                               img_link VARCHAR(8) NULL,
                               report_id UNIQUEIDENTIFIER NOT NULL,
                               CONSTRAINT PK_img_uuid PRIMARY KEY (img_uuid),
                               CONSTRAINT FK_images_report_report FOREIGN KEY (report_id) REFERENCES orders_report(r_id)
);
ALTER TABLE images_report
    ALTER COLUMN img_link VARCHAR(8) NULL;
GO
ALTER TABLE images_report
    ADD CONSTRAINT DF_images_report_img_uuid
        DEFAULT NEWID() FOR img_uuid;
GO

CREATE TABLE orders (
                        order_id VARCHAR(45) NOT NULL,
                        client_dni VARCHAR(8) NOT NULL,
                        plan_id VARCHAR(10) NOT NULL,
                        orders_report UNIQUEIDENTIFIER NOT NULL,

                        order_create DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                        order_start_install DATETIME2 NULL,
                        order_end_install DATETIME2 NULL,
                        order_elapsed_minutes INT NULL,
                        order_status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',

                        CONSTRAINT PK_order PRIMARY KEY (order_id),
                        CONSTRAINT FK_client_id FOREIGN KEY (client_dni) REFERENCES clients(client_dni),
                        CONSTRAINT FK_plan_id FOREIGN KEY (plan_id) REFERENCES plans(plan_id),
                        CONSTRAINT FK_orders_report FOREIGN KEY (orders_report) REFERENCES orders_report(r_id),
);



/*pc procedures ----------=========================*/
CREATE PROCEDURE sp_CreateRole
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

CREATE PROCEDURE sp_GetRoles
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



/*Movil procedures ---------------------------------------------------------------------------*/
use Servinet;
GO

CREATE PROCEDURE sp_logoutSession
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


DELETE FROM api_sessions;
GO
SELECT * FROM api_sessions;
go

CREATE PROCEDURE sp_GetSession
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


CREATE PROCEDURE sp_SetSession
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

CREATE PROCEDURE sp_GetAnnounces
AS
BEGIN
    SELECT * FROM announce
END;
GO

CREATE PROCEDURE sp_GetUserMovil
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

Use Servinet;
GO

CREATE PROCEDURE sp_GetMeMovil
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

/*
SELECT * from

SELECT u.*, r.*
                FROM users AS u
                INNER JOIN roles AS r
                    ON u.role = r.uuid
                WHERE u.display = 'Augusto';

                SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'cliente'
ORDER BY ORDINAL_POSITION;

SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

                */

USE SERVINET
SELECT
    TABLE_SCHEMA,
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'announce';

SELECT *
FROM dbo.announce;


CREATE TABLE announce(

                         uuid UNIQUEIDENTIFIER NOT NULL,
                         priority VARCHAR(50) NOT NULL,
                         title VARCHAR(20) NOT NULL,
                         description VARCHAR(150) NOT NULL,
                         author VARCHAR(20) NOT NULL,
                         sendAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                         CONSTRAINT PK_announce PRIMARY KEY (uuid)
);





USE Servinet;
GO

CREATE OR ALTER PROCEDURE sp_GetOrders
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
GO


USE Servinet;
GO

CREATE OR ALTER PROCEDURE sp_CreateOrder
    @order_id VARCHAR(45),
    @client_dni VARCHAR(8),
    @client_name VARCHAR(100),
    @client_last_name VARCHAR(100),
    @client_phone VARCHAR(9),
    @client_address VARCHAR(100),
    @client_email VARCHAR(50),
    @client_whatsapp VARCHAR(9) = NULL,
    @plan_id VARCHAR(10),
    @report_id UNIQUEIDENTIFIER = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;
        IF @report_id IS NULL
            SET @report_id = NEWID();

        IF NOT EXISTS (
            SELECT 1
            FROM clients
            WHERE client_dni = @client_dni
        )
            BEGIN
                INSERT INTO clients (
                    client_dni,
                    client_name,
                    client_last_name,
                    client_phone,
                    client_address,
                    client_email,
                    client_whatsapp
                )
                VALUES (
                           @client_dni,
                           @client_name,
                           @client_last_name,
                           @client_phone,
                           @client_address,
                           @client_email,
                           @client_whatsapp
                       );
            END;

        INSERT INTO orders_report (
            r_id
        )
        VALUES (
                   @report_id
               );
        INSERT INTO orders (
            order_id,
            client_dni,
            plan_id,
            orders_report
        )
        VALUES (
                   @order_id,
                   @client_dni,
                   @plan_id,
                   @report_id
               );

        COMMIT TRANSACTION;
        EXEC sp_GetOrders;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO


SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE';


SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    NUMERIC_PRECISION,
    NUMERIC_SCALE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users_logs'
ORDER BY ORDINAL_POSITION;
SELECT
    tc.CONSTRAINT_NAME,
    tc.CONSTRAINT_TYPE,
    kcu.COLUMN_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS tc
         LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS kcu
                   ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
                       AND tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA
                       AND tc.TABLE_NAME = kcu.TABLE_NAME
WHERE tc.TABLE_NAME = 'users_logs'
  AND tc.TABLE_SCHEMA = 'dbo'
ORDER BY tc.CONSTRAINT_TYPE, tc.CONSTRAINT_NAME;


SELECT * FROM users_logs;
GO


USE Servinet;
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

/*
Input request api or java
{
  "total_login": 15,
  "total_logout": 10,
  "total_error": 3
}

*/


USE Servinet;
GO

INSERT INTO plans (
    plan_id,
    plan_name,
    plan_speed,
    plan_price,
    plan_ispromo
)
VALUES
    ('PLAN001', 'BASICO', 50, 49.90, 0),
    ('PLAN002', 'PREMIUM', 200, 89.90, 0);
GO

EXEC sp_CreateOrder
     @order_id = 'ORD-2026-001',
     @client_dni = '74125836',
     @client_name = 'Carlos',
     @client_last_name = 'Ramirez',
     @client_phone = '987654321',
     @client_address = 'Av. America Norte 123',
     @client_email = 'carlos.ramirez@example.com',
     @client_whatsapp = '987654321',
     @plan_id = 'PLAN001';
GO

EXEC sp_CreateOrder
     @order_id = 'ORD-2026-002',
     @client_dni = '70856321',
     @client_name = 'Andrea',
     @client_last_name = 'Torres',
     @client_phone = '912345678',
     @client_address = 'Jr. Pizarro 456',
     @client_email = 'andrea.torres@example.com',
     @client_whatsapp = '912345678',
     @plan_id = 'PLAN002';
GO