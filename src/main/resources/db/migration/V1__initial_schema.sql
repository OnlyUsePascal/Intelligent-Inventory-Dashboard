-- =============================================
-- V1: Initial Schema for Inventory Dashboard
-- =============================================

-- Tenant table
CREATE TABLE tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Employee table
CREATE TABLE employee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_employee_role CHECK (role IN ('ADMIN', 'INVENTORY', 'SALE'))
);

CREATE INDEX idx_employee_tenant_id ON employee(tenant_id);

-- Vehicle table
CREATE TABLE vehicle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    vin VARCHAR(17) NOT NULL,
    license_plate VARCHAR(20),
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER,
    mileage INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    inventory_type VARCHAR(50) NOT NULL,
    received_date DATE,
    available_for_sale_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD', 'UNAVAILABLE')),
    CONSTRAINT chk_vehicle_inventory_type CHECK (inventory_type IN ('NEW', 'USED', 'DEMO')),
    CONSTRAINT uk_vehicle_tenant_vin UNIQUE (tenant_id, vin)
);

CREATE INDEX idx_vehicle_tenant_id ON vehicle(tenant_id);
CREATE INDEX idx_vehicle_status ON vehicle(status);
CREATE INDEX idx_vehicle_make ON vehicle(make);
CREATE INDEX idx_vehicle_model ON vehicle(model);

-- Reservation table
CREATE TABLE reservation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    reservation_date TIMESTAMP NOT NULL,
    reserved_until_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservation_vehicle_id ON reservation(vehicle_id);
CREATE INDEX idx_reservation_employee_id ON reservation(employee_id);

-- Vehicle Action table (Insights)
CREATE TABLE vehicle_action (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    action_text TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vehicle_action_vehicle_id ON vehicle_action(vehicle_id);
CREATE INDEX idx_vehicle_action_employee_id ON vehicle_action(employee_id);
CREATE INDEX idx_vehicle_action_timestamp ON vehicle_action(timestamp);
