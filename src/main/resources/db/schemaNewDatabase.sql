USE servinet2;

CREATE TABLE users (
                       uuid UNIQUEIDENTIFIER NOT NULL,
                       display VARCHAR(50) NOT NULL,
                       email VARCHAR(50) NOT NULL,
                       rol VARCHAR(30) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       create_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                       perfil_img TEXT NULL,
                       CONSTRAINT PK_uuid PRIMARY KEY (uuid),
                       CONSTRAINT UQ_usuarios_display UNIQUE (display),
                       CONSTRAINT UQ_usuarios_email UNIQUE (email),
                       CONSTRAINT CHK_rol CHECK (rol IN ('OWNER', 'ADMIN', 'TECHNICAL', 'SELLER'))
);


CREATE TABLE users_session (
                               hardware_id VARCHAR(50) NOT NULL,
                               user_uuid UNIQUEIDENTIFIER NOT NULL,
                               expires_at DATETIME2 NOT NULL

                                   CONSTRAINT DF_users_session_expires_at DEFAULT DATEADD(DAY, 2, SYSDATETIME()),
                               CONSTRAINT PK_uuidhardware PRIMARY KEY (hardware_id),
                               CONSTRAINT FK_user_uuid FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);

SELECT * FROM  users;