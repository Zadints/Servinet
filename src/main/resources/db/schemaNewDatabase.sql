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

CREATE TABLE appGeneral (
                            name VARCHAR(12) NOT NULL
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

/*
SELECT * from

SELECT u.*, r.*
                FROM users AS u
                INNER JOIN roles AS r
                    ON u.role = r.uuid
                WHERE u.display = 'Augusto';*/

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