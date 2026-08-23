-- Migração para padronização de IDs (Integer -> UUID) e herança de BaseTable

-- 1. Cart Items
DROP TABLE IF EXISTS cart_items;
CREATE TABLE cart_items (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(50),
    service_id VARCHAR(50),
    appointment_data TEXT,
    quantity INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES carts(user_id) ON DELETE CASCADE
);

-- 2. Carts (Adding Audit Columns)
-- SQLite doesn't support adding multiple columns in one ALTER TABLE easily or adding NOT NULL without default.
-- Since it's a structural reset phase, we can drop and recreate or alter carefully.
DROP TABLE IF EXISTS carts;
CREATE TABLE carts (
    user_id VARCHAR(100) PRIMARY KEY,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 3. Order Status Logs
DROP TABLE IF EXISTS order_status_logs;
CREATE TABLE order_status_logs (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    timestamp BIGINT NOT NULL,
    note VARCHAR(255),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 4. Component Products
DROP TABLE IF EXISTS component_products;
CREATE TABLE component_products (
    id VARCHAR(50) PRIMARY KEY,
    component_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_order INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    FOREIGN KEY (component_id) REFERENCES page_components(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
