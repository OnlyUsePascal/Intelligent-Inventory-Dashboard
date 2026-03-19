-- =============================================
-- V2: Add status column to reservation table
-- =============================================

-- Add status column with default 'ACTIVE' for existing rows
ALTER TABLE reservation ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add constraint to ensure valid status values
ALTER TABLE reservation ADD CONSTRAINT chk_reservation_status 
    CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED'));

-- Create index for efficient status queries
CREATE INDEX idx_reservation_status ON reservation(status);

-- Create index for finding expired reservations
CREATE INDEX idx_reservation_reserved_until ON reservation(reserved_until_date);
