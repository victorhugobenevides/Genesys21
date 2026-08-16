-- Genesys21 Initial Schema (Multi-DB Compatible)

-- 1. Users
CREATE TABLE users (
    id VARCHAR(100) PRIMARY KEY,
    email VARCHAR(200) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    avatar_url TEXT,
    phone VARCHAR(50),
    role VARCHAR(50) DEFAULT 'CUSTOMER',
    status VARCHAR(50) DEFAULT 'APPROVED',
    permissions TEXT DEFAULT '',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 2. Stores
CREATE TABLE stores (
    id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(100) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url TEXT,
    whatsapp VARCHAR(50),
    origin_zip_code VARCHAR(20),
    origin_street VARCHAR(255),
    origin_number VARCHAR(20),
    origin_neighborhood VARCHAR(100),
    origin_city VARCHAR(100),
    origin_state VARCHAR(50),
    allow_pay_on_location BOOLEAN DEFAULT TRUE,
    allow_pay_in_app BOOLEAN DEFAULT TRUE,
    allow_pickup BOOLEAN DEFAULT TRUE,
    allow_delivery BOOLEAN DEFAULT TRUE,
    stripe_public_key TEXT,
    stripe_secret_key TEXT,
    stripe_account_id VARCHAR(100),
    asaas_api_key TEXT,
    payment_gateway VARCHAR(20) DEFAULT 'STRIPE',
    custom_domain VARCHAR(255) UNIQUE,
    theme VARCHAR(50) DEFAULT 'ELEGANCE',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 3. Categories
CREATE TABLE categories (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    parent_id VARCHAR(50) REFERENCES categories(id) ON DELETE SET NULL,
    icon_name VARCHAR(50),
    color_hex VARCHAR(10),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    UNIQUE(store_id, name)
);

-- 4. Pages
CREATE TABLE pages (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    custom_domain VARCHAR(255) UNIQUE,
    whatsapp VARCHAR(50),
    theme VARCHAR(50) DEFAULT 'ELEGANCE',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 5. Page Components
CREATE TABLE page_components (
    id VARCHAR(50) PRIMARY KEY,
    page_id VARCHAR(50) NOT NULL REFERENCES pages(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    custom_label VARCHAR(100),
    is_filterable BOOLEAN DEFAULT TRUE,
    order_index INTEGER NOT NULL,
    content TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 6. Products
CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    description TEXT,
    category_id VARCHAR(50) REFERENCES categories(id) ON DELETE SET NULL,
    stock INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 7. Product Images
CREATE TABLE product_images (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    image_order INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 8. Component Products (Join Table)
CREATE TABLE component_products (
    id SERIAL PRIMARY KEY,
    component_id VARCHAR(50) NOT NULL REFERENCES page_components(id) ON DELETE CASCADE,
    product_id VARCHAR(50) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    product_order INTEGER NOT NULL
);

-- 9. Carts
CREATE TABLE carts (
    user_id VARCHAR(100) PRIMARY KEY,
    created_at BIGINT NOT NULL DEFAULT 0
);

-- 10. Cart Items
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL REFERENCES carts(user_id) ON DELETE CASCADE,
    product_id VARCHAR(50),
    service_id VARCHAR(50),
    appointment_data TEXT,
    quantity INTEGER NOT NULL
);

-- 11. Orders
CREATE TABLE orders (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE RESTRICT,
    customer_id VARCHAR(100) REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    total DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'LOCAL',
    whatsapp_contact VARCHAR(50),
    theme VARCHAR(50) DEFAULT 'ELEGANCE',
    shipping_street VARCHAR(255),
    shipping_number VARCHAR(20),
    shipping_complement VARCHAR(255),
    shipping_neighborhood VARCHAR(100),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(50),
    shipping_zip_code VARCHAR(20),
    shipping_price DOUBLE PRECISION DEFAULT 0.0,
    shipping_method VARCHAR(100),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

CREATE INDEX idx_orders_store_id ON orders(store_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- 12. Order Items
CREATE TABLE order_items (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id VARCHAR(50) REFERENCES products(id) ON DELETE SET NULL,
    service_id VARCHAR(50), -- Can reference booking_services later
    appointment_id VARCHAR(50),
    product_name VARCHAR(255) NOT NULL,
    product_price DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 13. Booking Services
CREATE TABLE booking_services (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    buffer_time_minutes INTEGER DEFAULT 0,
    category_id VARCHAR(50) REFERENCES categories(id) ON DELETE SET NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    is_online BOOLEAN DEFAULT FALSE,
    is_home_service BOOLEAN DEFAULT FALSE,
    max_participants INTEGER DEFAULT 1,
    meeting_link TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 14. Booking Service Images
CREATE TABLE booking_service_images (
    id VARCHAR(50) PRIMARY KEY,
    service_id VARCHAR(50) NOT NULL REFERENCES booking_services(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    image_order INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 15. Merchant Availability
CREATE TABLE merchant_availability (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL UNIQUE REFERENCES stores(id) ON DELETE CASCADE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 16. Weekly Availability
CREATE TABLE weekly_availability (
    id VARCHAR(50) PRIMARY KEY,
    availability_id VARCHAR(50) NOT NULL REFERENCES merchant_availability(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL,
    start_time VARCHAR(5) NOT NULL,
    end_time VARCHAR(5) NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 17. Blocked Dates
CREATE TABLE blocked_dates (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    blocked_date DATE NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 18. Appointments
CREATE TABLE appointments (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50) NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    service_id VARCHAR(50) NOT NULL REFERENCES booking_services(id) ON DELETE RESTRICT,
    customer_id VARCHAR(100) REFERENCES users(id) ON DELETE SET NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    start_time_ms BIGINT NOT NULL,
    end_time_ms BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    meeting_link TEXT,
    travel_fee DOUBLE PRECISION DEFAULT 0.0,
    street VARCHAR(255),
    number VARCHAR(20),
    neighborhood VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    order_id VARCHAR(50) REFERENCES orders(id) ON DELETE SET NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

CREATE INDEX idx_appointments_start_time ON appointments(start_time_ms);

-- 19. Appointment Notes
CREATE TABLE appointment_notes (
    id VARCHAR(50) PRIMARY KEY,
    appointment_id VARCHAR(50) NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    author_id VARCHAR(100) REFERENCES users(id) ON DELETE SET NULL,
    author_name VARCHAR(255) NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 20. Addresses
CREATE TABLE addresses (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(255),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 21. Audit Logs
CREATE TABLE audit_logs (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) REFERENCES users(id) ON DELETE SET NULL,
    store_id VARCHAR(50) REFERENCES stores(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 22. Receipts
CREATE TABLE receipts (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL REFERENCES users(id),
    store_id VARCHAR(50) REFERENCES stores(id),
    chave_acesso VARCHAR(100),
    emitente VARCHAR(200) NOT NULL,
    cnpj_emitente VARCHAR(50),
    data_emissao VARCHAR(50) NOT NULL,
    valor_total DOUBLE PRECISION NOT NULL,
    categoria VARCHAR(100) DEFAULT 'Geral',
    image_path TEXT,
    online_url TEXT,
    file_base64 TEXT,
    file_mime_type VARCHAR(100),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 23. Receipt Items
CREATE TABLE receipt_items (
    id SERIAL PRIMARY KEY,
    receipt_id VARCHAR(100) NOT NULL REFERENCES receipts(id),
    descricao VARCHAR(500) NOT NULL,
    quantidade DOUBLE PRECISION DEFAULT 1.0,
    valor_unitario DOUBLE PRECISION NOT NULL,
    valor_total DOUBLE PRECISION NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

-- 24. Order Status Logs
CREATE TABLE order_status_logs (
    id SERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    timestamp BIGINT NOT NULL,
    note VARCHAR(255)
);
