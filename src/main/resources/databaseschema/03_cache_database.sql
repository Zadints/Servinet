CREATE TABLE IF NOT EXISTS database_config (
    mode TEXT NOT NULL CHECK (mode IN ('SINGLE_DEVICE', 'MULTI_DEVICE')),
    host TEXT,
    port INTEGER,
    database_name TEXT,
    username TEXT,
    password TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (
        (mode = 'SINGLE_DEVICE')
        OR
        (
            mode = 'MULTI_DEVICE'
            AND host IS NOT NULL
            AND port BETWEEN 1 AND 65535
            AND database_name IS NOT NULL
            AND username IS NOT NULL
        )
    )
);