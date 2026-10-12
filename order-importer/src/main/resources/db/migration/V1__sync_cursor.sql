CREATE SCHEMA IF NOT EXISTS importer;

CREATE TABLE importer.sync_cursor (
    stream VARCHAR(64) PRIMARY KEY,
    cursor_at TIMESTAMPTZ NOT NULL
);
