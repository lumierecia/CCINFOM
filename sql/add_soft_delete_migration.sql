-- Add is_deleted column to existing tables
ALTER TABLE Employees ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE Suppliers ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE InventoryItems ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE Customers ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE Orders ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Create indexes for better performance on is_deleted queries
CREATE INDEX IF NOT EXISTS idx_employees_is_deleted ON Employees(is_deleted);
CREATE INDEX IF NOT EXISTS idx_suppliers_is_deleted ON Suppliers(is_deleted);
CREATE INDEX IF NOT EXISTS idx_inventory_is_deleted ON InventoryItems(is_deleted);
CREATE INDEX IF NOT EXISTS idx_customers_is_deleted ON Customers(is_deleted);
CREATE INDEX IF NOT EXISTS idx_orders_is_deleted ON Orders(is_deleted);

-- Update all existing records to set is_deleted to FALSE
UPDATE Employees SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE Suppliers SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE InventoryItems SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE Customers SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE Orders SET is_deleted = FALSE WHERE is_deleted IS NULL; 