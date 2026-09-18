USE Servinet;
GO

CREATE TABLE roles(
                      uuid UNIQUEIDENTIFIER NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      hexColor VARCHAR(20) NOT NULL,
                      CONSTRAINT PK_roles PRIMARY KEY (uuid)
)
CREATE TABLE role_permissions (
                                  role_uuid UNIQUEIDENTIFIER NOT NULL,
                                  permission VARCHAR(50) NOT NULL,
                                  CONSTRAINT PK_role_permissions PRIMARY KEY (role_uuid, permission),
                                  CONSTRAINT FK_role_permissions FOREIGN KEY (role_uuid) REFERENCES roles(uuid)
);

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