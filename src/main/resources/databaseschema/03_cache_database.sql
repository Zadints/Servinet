CREATE TABLE IF NOT EXISTS roles (
    uuid     TEXT NOT NULL,
    name     TEXT NOT NULL COLLATE NOCASE CHECK (length(name) <= 50),
    hexColor TEXT NOT NULL CHECK (length(hexColor) <= 20),
    CONSTRAINT PK_roles      PRIMARY KEY (uuid),
    CONSTRAINT UQ_roles_name UNIQUE (name)
);