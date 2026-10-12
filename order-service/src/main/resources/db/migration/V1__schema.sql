CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    barcode VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    low_stock_threshold INT NOT NULL,
    sale_price_minor BIGINT NOT NULL,
    low_stock BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    marketplace VARCHAR(64) NOT NULL,
    external_order_id VARCHAR(128) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    ordered_at TIMESTAMPTZ NOT NULL,
    total_minor BIGINT NOT NULL,
    CONSTRAINT uq_orders_external UNIQUE (marketplace, external_order_id)
);

CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id),
    barcode VARCHAR(64) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price_minor BIGINT NOT NULL
);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id),
    order_id UUID NOT NULL REFERENCES orders (id),
    delta INT NOT NULL,
    reason VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    marketplace VARCHAR(64) NOT NULL,
    external_question_id VARCHAR(128) NOT NULL,
    product_barcode VARCHAR(64) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    asked_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT uq_questions_external UNIQUE (marketplace, external_question_id)
);

CREATE TABLE answer_drafts (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL UNIQUE REFERENCES questions (id),
    draft_text TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE sync_failures (
    id UUID PRIMARY KEY,
    marketplace VARCHAR(64) NOT NULL,
    operation VARCHAR(64) NOT NULL,
    external_id VARCHAR(128),
    retry_topic VARCHAR(255) NOT NULL,
    retry_key VARCHAR(255),
    payload TEXT NOT NULL,
    error_message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_orders_ordered_at ON orders (ordered_at);
CREATE INDEX idx_sync_failures_created_at ON sync_failures (created_at);
