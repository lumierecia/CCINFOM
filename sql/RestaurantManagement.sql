DROP DATABASE IF EXISTS `restaurantdb`;
CREATE DATABASE IF NOT EXISTS `restaurantdb`;
USE `restaurantdb`;

-- Reference tables for normalization (no dependencies)
CREATE TABLE IF NOT EXISTS `Units` (
    unit_id INT AUTO_INCREMENT PRIMARY KEY,
    unit_name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `Categories` (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS `Roles` (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `TimeShifts` (
    time_shiftid INT AUTO_INCREMENT PRIMARY KEY,
    shift_type VARCHAR(20) NOT NULL,
    time_start TIME NOT NULL,
    time_end TIME NOT NULL
);

-- Core tables for employee management
CREATE TABLE IF NOT EXISTS `Employees` (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role_id INT NOT NULL,
    time_shiftid INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (time_shiftid) REFERENCES TimeShifts(time_shiftid)
);

-- Core tables for customer management
CREATE TABLE IF NOT EXISTS `Customers` (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE KEY,
    phonenumber VARCHAR(20) NOT NULL UNIQUE KEY,
    address VARCHAR(200) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Core tables for order management
CREATE TABLE IF NOT EXISTS `Orders` (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_type ENUM('Dine-In', 'Takeout', 'Delivery') NOT NULL,
    order_status ENUM('In Progress', 'Ready', 'Served', 'Completed', 'Cancelled') NOT NULL DEFAULT 'In Progress',
    order_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method ENUM('Cash', 'Credit Card') NULL,
    payment_status ENUM('Pending', 'Paid') NOT NULL DEFAULT 'Pending',
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

-- Core tables for supplier management
CREATE TABLE IF NOT EXISTS `Suppliers` (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    address TEXT,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Core tables for inventory and ingredient management
CREATE TABLE IF NOT EXISTS `Ingredients` (
    ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit_id INT NOT NULL,
    quantity_in_stock DECIMAL(10,2) DEFAULT 0,
    minimum_stock_level DECIMAL(10,2) NOT NULL,
    cost_per_unit DECIMAL(10,2) NOT NULL,
    last_restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_restocked_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (unit_id) REFERENCES Units(unit_id),
    FOREIGN KEY (last_restocked_by) REFERENCES Employees(employee_id)
);

CREATE TABLE IF NOT EXISTS `IngredientBatches` (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT NOT NULL,
    supplier_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,
    purchase_price DECIMAL(10,2) NOT NULL,
    remaining_quantity DECIMAL(10,2) NOT NULL,
    status ENUM('Available', 'Low', 'Expired', 'Depleted') DEFAULT 'Available',
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id)
);

CREATE TABLE IF NOT EXISTS `IngredientSuppliers` (
    ingredient_id INT,
    supplier_id INT,
    unit_price DECIMAL(10,2) NOT NULL,
    lead_time_days INT,
    minimum_order_quantity DECIMAL(10,2),
    is_primary_supplier BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (ingredient_id, supplier_id),
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id)
);

CREATE TABLE IF NOT EXISTS `Dishes` (
    dish_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category_id INT NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    recipe_instructions TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

CREATE TABLE IF NOT EXISTS `DishIngredients` (
    dish_id INT,
    ingredient_id INT,
    quantity_needed DECIMAL(10,2) NOT NULL,
    unit_id INT NOT NULL,
    PRIMARY KEY (dish_id, ingredient_id),
    FOREIGN KEY (dish_id) REFERENCES Dishes(dish_id),
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    FOREIGN KEY (unit_id) REFERENCES Units(unit_id)
);

CREATE TABLE IF NOT EXISTS `OrderItems` (
    order_id INT NOT NULL,
    dish_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_time DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, dish_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (dish_id) REFERENCES Dishes(dish_id)
);

CREATE TABLE IF NOT EXISTS `AssignedEmployeesToOrders` (
    order_id INT NOT NULL,
    employee_id INT NOT NULL,
    PRIMARY KEY (order_id, employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

CREATE TABLE IF NOT EXISTS `IngredientTransactions` (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT NOT NULL,
    transaction_type ENUM('Purchase', 'Usage', 'Adjustment', 'Waste') NOT NULL,
    quantity_change DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unit_price DECIMAL(10,2),
    supplier_id INT,
    order_id INT,
    employee_id INT NOT NULL,
    notes TEXT,
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- Insert reference data
INSERT INTO Units (unit_name) VALUES
('kg'),        -- 1
('liters'),    -- 2
('pieces'),    -- 3
('grams'),     -- 4
('milliliters'); -- 5

INSERT INTO Categories (category_name) VALUES
('Main Course'), -- 1
('Desserts'),   -- 2
('Beverages'),  -- 3
('Sides');      -- 4

INSERT INTO Roles (role_name) VALUES
('Waiter'),   -- 1
('Chef'),     -- 2
('Cleaner'),  -- 3
('Manager'),  -- 4
('Cashier');  -- 5

INSERT INTO TimeShifts (shift_type, time_start, time_end) VALUES
('Morning', '04:00:00', '12:00:00'),    -- 1
('Afternoon', '12:00:00', '20:00:00'),  -- 2
('Night', '20:00:00', '04:00:00');      -- 3

-- Insert sample data
INSERT INTO Employees (first_name, last_name, role_id, time_shiftid) VALUES
('John', 'Wick', 1, 1),           -- 1: Waiter, Morning
('Sabrina', 'Carpenter', 2, 1),   -- 2: Chef, Morning
('Spongebob', 'Squarepants', 1, 2), -- 3: Waiter, Afternoon
('Bruno', 'Mars', 3, 1),          -- 4: Cleaner, Morning
('Jennie', 'Kim', 2, 2),          -- 5: Chef, Afternoon
('Ariana', 'Grande', 4, NULL),     -- 6: Manager, No shift
('Nicki', 'Minaj', 5, 1),         -- 7: Cashier, Morning
('Peter', 'Parker', 1, 3),        -- 8: Waiter, Night
('Donald', 'Trump', 3, 3),        -- 9: Cleaner, Night
('Patrick', 'Star', 4, 3);        -- 10: Manager, Night

INSERT INTO Suppliers (name, contact_person, email, phone, address) VALUES
('Metro Ingredients Co.', 'John Metro', 'john@metroingredients.com', '09171234572', '123 Supply St, Metro Manila'),     -- 1
('Fresh Foods Supply', 'Maria Fresh', 'maria@freshfoods.com', '09171234573', '456 Market Ave, Quezon City'),           -- 2
('Quality Grocers', 'Peter Quality', 'peter@qualitygrocers.com', '09171234574', '789 Grocery Lane, Makati');          -- 3

INSERT INTO Ingredients (name, unit_id, quantity_in_stock, minimum_stock_level, cost_per_unit, last_restocked_by) VALUES
('Rice', 1, 100.00, 20.00, 50.00, 1),           -- 1: kg
('Pork', 1, 50.00, 10.00, 280.00, 2),           -- 2: kg
('Chicken', 1, 50.00, 10.00, 220.00, 2),        -- 3: kg
('Beef', 1, 40.00, 8.00, 350.00, 2),            -- 4: kg
('Cooking Oil', 2, 30.00, 5.00, 90.00, 1),      -- 5: liters
('Soy Sauce', 2, 20.00, 4.00, 85.00, 1),        -- 6: liters
('Vinegar', 2, 20.00, 4.00, 75.00, 1),          -- 7: liters
('Garlic', 1, 10.00, 2.00, 120.00, 1),          -- 8: kg
('Onion', 1, 15.00, 3.00, 90.00, 1),            -- 9: kg
('Tomato', 1, 15.00, 3.00, 95.00, 1),           -- 10: kg
('Tamarind Base', 1, 20.00, 5.00, 150.00, 2),   -- 11: kg
('Peanut Butter', 1, 15.00, 3.00, 180.00, 2),   -- 12: kg
('Ground Beef', 1, 30.00, 5.00, 320.00, 2),     -- 13: kg
('Fish', 1, 40.00, 8.00, 250.00, 2),            -- 14: kg
('Rice Noodles', 1, 25.00, 5.00, 100.00, 1),    -- 15: kg
('Eggs', 3, 100.00, 20.00, 8.00, 1),            -- 16: pieces
('Milk', 2, 30.00, 5.00, 80.00, 1),             -- 17: liters
('Potatoes', 1, 40.00, 8.00, 60.00, 1),         -- 18: kg
('Mayonnaise', 1, 10.00, 2.00, 150.00, 1),      -- 19: kg
('Kangkong', 1, 20.00, 5.00, 80.00, 1),         -- 20: kg
('Radish', 1, 15.00, 3.00, 70.00, 1),           -- 21: kg
('Ice', 1, 50.00, 10.00, 20.00, 1),             -- 22: kg
('Tea', 1, 10.00, 2.00, 200.00, 1);             -- 23: kg

INSERT INTO IngredientSuppliers (ingredient_id, supplier_id, unit_price, lead_time_days, minimum_order_quantity, is_primary_supplier) VALUES
(1, 1, 48.00, 2, 50.00, TRUE),   -- Rice from Metro
(2, 2, 275.00, 1, 20.00, TRUE),  -- Pork from Fresh Foods
(3, 2, 215.00, 1, 20.00, TRUE),  -- Chicken from Fresh Foods
(4, 2, 345.00, 1, 15.00, TRUE),  -- Beef from Fresh Foods
(5, 3, 88.00, 3, 10.00, TRUE),   -- Cooking Oil from Quality
(6, 3, 82.00, 3, 5.00, TRUE),    -- Soy Sauce from Quality
(7, 3, 72.00, 3, 5.00, TRUE),    -- Vinegar from Quality
(8, 1, 115.00, 2, 5.00, TRUE),   -- Garlic from Metro
(9, 1, 87.00, 2, 5.00, TRUE),    -- Onion from Metro
(10, 1, 92.00, 2, 5.00, TRUE),   -- Tomato from Metro
(20, 1, 75.00, 1, 5.00, TRUE),   -- Kangkong from Metro
(21, 1, 65.00, 1, 5.00, TRUE),   -- Radish from Metro
(22, 3, 18.00, 1, 20.00, TRUE),  -- Ice from Quality
(23, 3, 190.00, 3, 2.00, TRUE);  -- Tea from Quality

INSERT INTO Dishes (name, category_id, selling_price, recipe_instructions) VALUES
('Pork Sinigang', 1, 250.00, 'Boil pork with vegetables in tamarind-based soup until tender.'),
('Kare-kare', 1, 350.00, 'Cook beef until tender. Prepare peanut sauce.'),
('Chicken Adobo', 1, 220.00, 'Marinate chicken in soy sauce and vinegar.'),
('Sisig', 1, 280.00, 'Grill pork parts, chop finely. Mix with onions.'),
('Burger Steak', 1, 190.00, 'Form seasoned ground beef into patties.'),
('Halo-halo', 2, 120.00, 'Layer sweetened ingredients with shaved ice.'),
('Leche Flan', 2, 90.00, 'Caramelize sugar for top. Steam custard mixture.'),
('Mango Shake', 3, 85.00, 'Blend fresh ripe mangoes with milk and ice.'),
('Bottomless Iced Tea', 3, 60.00, 'Brew tea. Chill and serve with ice.'),
('Steamed Rice', 4, 40.00, 'Wash rice thoroughly. Cook in rice cooker.'),
('Mashed Potatoes', 4, 70.00, 'Boil potatoes until tender. Mash with butter and milk.');

INSERT INTO DishIngredients (dish_id, ingredient_id, quantity_needed, unit_id) VALUES
-- Pork Sinigang (dish_id = 1)
(1, 2, 1.5, 1),    -- Pork, 1.5 kg
(1, 9, 0.2, 1),    -- Onion, 0.2 kg
(1, 10, 0.3, 1),   -- Tomato, 0.3 kg
(1, 11, 0.2, 1),   -- Tamarind Base, 0.2 kg
(1, 20, 0.3, 1),   -- Kangkong, 0.3 kg
(1, 21, 0.2, 1),   -- Radish, 0.2 kg

-- Kare-kare (dish_id = 2)
(2, 4, 1.2, 1),     -- Beef, 1.2 kg
(2, 12, 0.4, 1),    -- Peanut Butter, 0.4 kg
(2, 9, 0.2, 1),     -- Onion, 0.2 kg
(2, 8, 0.1, 1),     -- Garlic, 0.1 kg
(2, 5, 0.1, 2),     -- Cooking Oil, 0.1 liters

-- Chicken Adobo (dish_id = 3)
(3, 3, 1.0, 1),     -- Chicken, 1.0 kg
(3, 6, 0.3, 2),     -- Soy Sauce, 0.3 liters
(3, 7, 0.3, 2),     -- Vinegar, 0.3 liters
(3, 8, 0.1, 1),     -- Garlic, 0.1 kg
(3, 9, 0.2, 1),     -- Onion, 0.2 kg

-- Sisig (dish_id = 4)
(4, 2, 1.0, 1),     -- Pork, 1.0 kg
(4, 9, 0.3, 1),     -- Onion, 0.3 kg
(4, 19, 0.1, 1),    -- Mayonnaise, 0.1 kg
(4, 5, 0.1, 2),     -- Cooking Oil, 0.1 liters

-- Burger Steak (dish_id = 5)
(5, 13, 0.8, 1),    -- Ground Beef, 0.8 kg
(5, 9, 0.2, 1),     -- Onion, 0.2 kg
(5, 16, 1.0, 3),    -- Eggs, 1 piece
(5, 5, 0.1, 2),     -- Cooking Oil, 0.1 liters

-- Halo-halo (dish_id = 6)
(6, 17, 0.2, 2),    -- Milk, 0.2 liters
(6, 22, 0.5, 1),    -- Ice, 0.5 kg

-- Leche Flan (dish_id = 7)
(7, 16, 4.0, 3),    -- Eggs, 4 pieces
(7, 17, 0.2, 2),    -- Milk, 0.2 liters

-- Mango Shake (dish_id = 8)
(8, 17, 0.2, 2),    -- Milk, 0.2 liters

-- Bottomless Iced Tea (dish_id = 9)
(9, 23, 0.1, 1),    -- Tea, 0.1 kg

-- Steamed Rice (dish_id = 10)
(10, 1, 1.0, 1),    -- Rice, 1.0 kg

-- Mashed Potatoes (dish_id = 11)
(11, 18, 0.8, 1),   -- Potatoes, 0.8 kg
(11, 17, 0.1, 2),   -- Milk, 0.1 liters
(11, 5, 0.05, 2);   -- Cooking Oil, 0.05 liters

INSERT INTO Customers (last_name, first_name, email, phonenumber, address) VALUES
('Smith', 'John', 'john.smith@example.com', '09171234567', '123 Main St, Cityville'),      -- 1
('Doe', 'Jane', 'jane.doe@example.com', '09171234568', '456 Oak St, Townsville'),         -- 2
('Roe', 'Richard', 'richard.roe@example.com', '09171234569', '789 Pine St, Villagetown'), -- 3
('Taylor', 'Alex', 'alex.taylor@example.com', '09171234570', '101 Birch St, Foresthill'), -- 4
('Brown', 'Emily', 'emily.brown@example.com', '09171234571', '202 Maple St, Lakeside');   -- 5

INSERT INTO Orders (customer_id, order_type, order_status, total_amount, payment_method, payment_status) VALUES
(1, 'Dine-In', 'In Progress', 450.00, NULL, 'Pending'),      -- 1
(2, 'Takeout', 'Ready', 350.00, 'Cash', 'Paid'),            -- 2
(3, 'Delivery', 'Completed', 550.00, 'Credit Card', 'Paid'), -- 3
(4, 'Dine-In', 'Served', 650.00, NULL, 'Pending'),          -- 4
(5, 'Takeout', 'In Progress', 250.00, NULL, 'Pending');      -- 5

INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES
(1, 1, 1, 250.00),  -- Pork Sinigang
(1, 10, 2, 40.00),  -- Steamed Rice x2
(2, 2, 1, 350.00),  -- Kare-kare
(3, 3, 2, 220.00),  -- Chicken Adobo x2
(3, 11, 1, 70.00),  -- Mashed Potatoes
(4, 4, 1, 280.00),  -- Sisig
(4, 8, 2, 85.00),   -- Mango Shake x2
(5, 5, 1, 190.00),  -- Burger Steak
(5, 10, 1, 40.00);  -- Steamed Rice

INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES
(1, 1),  -- John Wick (Waiter) for order 1
(1, 2),  -- Sabrina Carpenter (Chef) for order 1
(2, 3),  -- Spongebob (Waiter) for order 2
(2, 5),  -- Jennie Kim (Chef) for order 2
(3, 8),  -- Peter Parker (Waiter) for order 3
(3, 2),  -- Sabrina Carpenter (Chef) for order 3
(4, 1),  -- John Wick (Waiter) for order 4
(4, 5),  -- Jennie Kim (Chef) for order 4
(5, 3),  -- Spongebob (Waiter) for order 5
(5, 2);  -- Sabrina Carpenter (Chef) for order 5
