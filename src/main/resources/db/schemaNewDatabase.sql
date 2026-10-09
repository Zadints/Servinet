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


CREATE TABLE orders_report (
                               r_id UNIQUEIDENTIFIER NOT NULL,
                               technician_id UNIQUEIDENTIFIER NOT NULL,

                               r_observation VARCHAR(MAX) NULL,
                               r_antenna_connect UNIQUEIDENTIFIER NOT NULL,

                               r_create DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                               r_status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
                               r_ended DATETIME2 NULL,

                               r_latitude DECIMAL(10, 7) NULL,
                               r_longitude DECIMAL(10, 7) NULL,

                               CONSTRAINT PK_orders_report PRIMARY KEY (r_id),
                               CONSTRAINT FK_technician_id FOREIGN KEY (technician_id) REFERENCES users(uuid),
                               CONSTRAINT FK_antenna_connect FOREIGN KEY (r_antenna_connect) REFERENCES antennas(uuid)
);


CREATE TABLE images_report (
                               img_uuid UNIQUEIDENTIFIER NOT NULL,
                               img_link VARCHAR(8) NOT NULL,
                               report_id UNIQUEIDENTIFIER NOT NULL,
                               CONSTRAINT PK_img_uuid PRIMARY KEY (img_uuid),
                               CONSTRAINT FK_images_report_report FOREIGN KEY (report_id) REFERENCES orders_report(r_id)
);


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