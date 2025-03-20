DROP DATABASE IF EXISTS `s17_group8`;
CREATE DATABASE IF NOT EXISTS `s17_group8`;
USE `s17_group8`;

CREATE TABLE IF NOT EXISTS Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS TimeShift (
    time_shiftid INT AUTO_INCREMENT PRIMARY KEY,
    shift_type VARCHAR(20) NOT NULL,
    time_start TIME NOT NULL,
    time_end TIME NOT NULL
);

CREATE TABLE IF NOT EXISTS Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role_id INT NOT NULL,
    time_shiftid INT,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (time_shiftid) REFERENCES TimeShift(time_shiftid)
);

INSERT INTO Roles (role_name) VALUES
('Waiter'),
('Chef'),
('Cleaner'),
('Manager'),
('Cashier');

INSERT INTO TimeShift (shift_type, time_start, time_end) VALUES
-- MORNING 4am to 12noon
-- AFTERNOON 12noon to 8pm
-- NIGHT 8pm to 4am
('Morning', '04:00:00', '12:00:00'),
('Afternoon', '12:00:00', '20:00:00'),
('Night', '20:00:00', '04:00:00');

INSERT INTO Employee (first_name, last_name, role_id, time_shiftid) VALUES
('John', 'Wick', 1, 1),
('Sabrina', 'Carpenter', 2, 1),
('Spongebob', 'Squarepants', 1, 2),
('Bruno', 'Mars', 3, 1),
('Jennie', 'Kim', 2, 2),
('Ariana', 'Grande', 4, NULL),
('Nicki', 'Minaj', 5, 1),
('Peter', 'Parker', 1, 3),
('Donald', 'Trump', 3, 3),
('Patrick', 'Star', 4, 3);

CREATE TABLE IF NOT EXISTS Suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    address TEXT,
    status ENUM('Active', 'Inactive') DEFAULT 'Active'
);

CREATE TABLE IF NOT EXISTS Ingredients (
    ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL, -- e.g., kg, liters, pieces
    quantity_in_stock DECIMAL(10,2) DEFAULT 0,
    minimum_stock_level DECIMAL(10,2) NOT NULL,
    cost_per_unit DECIMAL(10,2) NOT NULL,
    last_restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_restocked_by INT,
    FOREIGN KEY (last_restocked_by) REFERENCES Employee(employee_id)
);

CREATE TABLE IF NOT EXISTS IngredientSuppliers (
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

CREATE TABLE IF NOT EXISTS Inventory (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL UNIQUE KEY,
    category ENUM('Main Course', 'Desserts', 'Beverages', 'Sides') NOT NULL,
    make_price DECIMAL(10, 2) NOT NULL CHECK (make_price > 0),
    sell_price DECIMAL(10, 2) NOT NULL CHECK (sell_price > 0),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    last_restock TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_restocked_by INT NOT NULL,
    recipe_instructions TEXT,
    status ENUM('Available', 'Unavailable') DEFAULT 'Available',
    FOREIGN KEY (last_restocked_by) REFERENCES Employee(employee_id)
);

CREATE TABLE IF NOT EXISTS DishIngredients (
    product_id INT,
    ingredient_id INT,
    quantity_needed DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id) REFERENCES Inventory(product_id),
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id)
);

CREATE TABLE IF NOT EXISTS IngredientTransactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT,
    transaction_type ENUM('Purchase', 'Usage', 'Adjustment', 'Waste') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unit_price DECIMAL(10,2),
    supplier_id INT,
    order_id INT,
    notes TEXT,
    recorded_by INT,
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (recorded_by) REFERENCES Employee(employee_id)
);

CREATE TABLE IF NOT EXISTS Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE KEY,
    phonenumber VARCHAR(20) NOT NULL UNIQUE KEY,
    address VARCHAR(200) NOT NULL
);

INSERT INTO Customers (customer_id, last_name, first_name, email, phonenumber, address)
VALUES
    (101, 'Smith', 'John', 'john.smith@example.com', '09171234567', '123 Main St, Cityville'),
    (102, 'Doe', 'Jane', 'jane.doe@example.com', '09171234568', '456 Oak St, Townsville'),
    (103, 'Roe', 'Richard', 'richard.roe@example.com', '09171234569', '789 Pine St, Villagetown'),
    (104, 'Taylor', 'Alex', 'alex.taylor@example.com', '09171234570', '101 Birch St, Foresthill'),
    (105, 'Brown', 'Emily', 'emily.brown@example.com', '09171234571', '202 Maple St, Lakeside');

CREATE TABLE IF NOT EXISTS Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_type ENUM('Dine-In', 'Takeout', 'Delivery') NOT NULL,
    order_status ENUM('In Progress', 'Ready', 'Served', 'Completed') NOT NULL,
    order_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

CREATE TABLE IF NOT EXISTS Payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    amount_paid DECIMAL(10, 2) NOT NULL CHECK (amount_paid >= 0),
    payment_method ENUM('Cash', 'Credit Card') NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

INSERT INTO Orders (customer_id, order_type, order_status)
VALUES 	(101,'Dine-In', 'In Progress'),
		(102, 'Takeout', 'Ready'),
		(103, 'Delivery', 'Served'),
		(104, 'Dine-In', 'Completed'),
		(105, 'Takeout', 'In Progress');
        
CREATE TABLE IF NOT EXISTS Order_History (
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

INSERT INTO Order_History (order_id, customer_id)
VALUES
    (1, 101),
    (2, 102),
    (3, 103),
    (4, 104),
    (5, 105);

CREATE TABLE IF NOT EXISTS Order_Item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 0 CHECK (quantity > 0),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Inventory(product_id)
);

INSERT INTO Order_Item (order_id, product_id, quantity)
VALUES
    -- Order 1
    (1, 1, 1),   -- Pork Sinigang: 1pc
    (1, 19, 1),  -- Steamed Rice: 1pc
    (1, 9, 1),   -- Halo-halo: 1pc
    (1, 15, 1),  -- Mango Shake: 1pc

    -- Order 2
    (2, 2, 1),   -- Kare-kare: 1
    (2, 20, 1),  -- Mashed Potatoes: 1
    (2, 10, 1),  -- Leche Flan: 1
    (2, 16, 1),  -- Bottomless Iced Tea: 1

    -- Order 3
    (3, 3, 1),  -- Chicken Adobo: 1
    (3, 21, 1),  -- Garlic Fried Rice: 1
    (3, 11, 1), -- Dulce de Leche Cake: 1
    (3, 18, 1),  -- Sago't Gulaman: 1

    -- Order 4
    (4, 4, 1),   -- Sisig: 1
    (4, 22, 1),  -- Macaroni Salad: 1
    (4, 14, 1),  -- Buko Pandan: 1
    (4, 17, 1),  -- Buko Juice: 1

    -- Order 5
    (5, 5, 1),   -- Burger Steak: 1
    (5, 19, 1),  -- Steamed Rice: 1
    (5, 13, 1),  -- Peach Mango Pie: 1
    (5, 15, 1);  -- Mango Shake: 1

CREATE TABLE IF NOT EXISTS Assigned_Employee_to_Order (
    order_id INT NOT NULL,
    employee_id INT NOT NULL,
    PRIMARY KEY (order_id, employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

INSERT INTO Assigned_Employee_to_Order (order_id, employee_id)
VALUES
-- Order 1: Dine-In, Shift: Afternoon
    (1, 2),  -- Sabrina Carpenter (Chef)
    (1, 8),  -- Peter Parker (Waiter)

-- Order 2: Takeout, Shift: Night
    (2, 5),  -- Jennie Kim (Chef)
    (2, 3),  -- Spongebob Squarepants (Waiter)

-- Order 3: Delivery, Shift: Morning
    (3, 7),  -- Nicki Minaj (Cashier)
    (3, 10), -- Patrick Star (Manager)

-- Order 4: Dine-In, Shift: Morning
    (4, 2),  -- Sabrina Carpenter (Chef)
    (4, 1),  -- John Wick (Waiter)
    (4, 4),  -- Bruno Mars (Cleaner)
    (4, 10), -- Patrick Star (Manager)

-- Order 5: Takeout, Shift: Afternoon
    (5, 2),  -- Sabrina Carpenter (Chef)
    (5, 8);  -- Peter Parker (Waiter)

INSERT INTO Suppliers (name, contact_person, email, phone, address) VALUES
('Metro Ingredients Co.', 'John Metro', 'john@metroingredients.com', '09171234572', '123 Supply St, Metro Manila'),
('Fresh Foods Supply', 'Maria Fresh', 'maria@freshfoods.com', '09171234573', '456 Market Ave, Quezon City'),
('Quality Grocers', 'Peter Quality', 'peter@qualitygrocers.com', '09171234574', '789 Grocery Lane, Makati');

INSERT INTO Ingredients (name, unit, quantity_in_stock, minimum_stock_level, cost_per_unit, last_restocked_by) VALUES
('Rice', 'kg', 100.00, 20.00, 50.00, 1),
('Pork', 'kg', 50.00, 10.00, 280.00, 2),
('Chicken', 'kg', 50.00, 10.00, 220.00, 2),
('Beef', 'kg', 40.00, 8.00, 350.00, 2),
('Cooking Oil', 'liters', 30.00, 5.00, 90.00, 1),
('Soy Sauce', 'liters', 20.00, 4.00, 85.00, 1),
('Vinegar', 'liters', 20.00, 4.00, 75.00, 1),
('Garlic', 'kg', 10.00, 2.00, 120.00, 1),
('Onion', 'kg', 15.00, 3.00, 90.00, 1),
('Tomato', 'kg', 15.00, 3.00, 95.00, 1);

INSERT INTO IngredientSuppliers (ingredient_id, supplier_id, unit_price, lead_time_days, minimum_order_quantity, is_primary_supplier) VALUES
(1, 1, 48.00, 2, 50.00, TRUE),  -- Rice from Metro
(2, 2, 275.00, 1, 20.00, TRUE), -- Pork from Fresh Foods
(3, 2, 215.00, 1, 20.00, TRUE), -- Chicken from Fresh Foods
(4, 2, 345.00, 1, 15.00, TRUE), -- Beef from Fresh Foods
(5, 3, 88.00, 3, 10.00, TRUE),  -- Cooking Oil from Quality
(6, 3, 82.00, 3, 5.00, TRUE),   -- Soy Sauce from Quality
(7, 3, 72.00, 3, 5.00, TRUE),   -- Vinegar from Quality
(8, 1, 115.00, 2, 5.00, TRUE),  -- Garlic from Metro
(9, 1, 87.00, 2, 5.00, TRUE),   -- Onion from Metro
(10, 1, 92.00, 2, 5.00, TRUE);  -- Tomato from Metro

INSERT INTO DishIngredients (product_id, ingredient_id, quantity_needed, unit) VALUES
-- Pork Sinigang (product_id = 1)
(1, 2, 0.3, 'kg'),    -- Pork
(1, 9, 0.1, 'kg'),    -- Onion
(1, 10, 0.2, 'kg'),   -- Tomato

-- Chicken Adobo (product_id = 3)
(3, 3, 0.3, 'kg'),    -- Chicken
(3, 6, 0.1, 'liters'), -- Soy Sauce
(3, 7, 0.1, 'liters'), -- Vinegar
(3, 8, 0.05, 'kg');    -- Garlic

-- Reinsert Inventory data with recipe instructions
INSERT INTO Inventory (product_name, category, make_price, sell_price, quantity, last_restocked_by, recipe_instructions) 
VALUES 	
('Pork Sinigang', 'Main Course', 170.00, 250.00, 50, 1, 'Boil pork with vegetables in tamarind-based soup until tender. Add vegetables according to cooking time.'),
('Kare-kare', 'Main Course', 230.00, 350.00, 50, 2, 'Cook beef until tender. Prepare peanut sauce. Combine and simmer with vegetables.'),
('Chicken Adobo', 'Main Course', 120.00, 220.00, 50, 3, 'Marinate chicken in soy sauce and vinegar. Brown chicken then simmer in marinade until cooked.'),
('Sisig', 'Main Course', 160.00, 280.00, 50, 4, 'Grill pork parts, chop finely. Mix with onions and seasonings. Serve on sizzling plate.'),
('Burger Steak', 'Main Course', 90.00, 190.00, 50, 5, 'Form seasoned ground beef into patties. Pan-fry. Serve with mushroom gravy.'),
('Pancit Palabok', 'Main Course', 100.00, 180.00, 50, 6, 'Cook noodles. Prepare shrimp sauce. Top with various toppings.'),
('Pancit Bihon', 'Main Course', 80.00, 170.00, 50, 7, 'Stir-fry vegetables and meat. Add soaked rice noodles and season.'),
('Fish Sinigang', 'Main Course', 120.00, 240.00, 50, 8, 'Similar to pork sinigang but with fish. Add vegetables last to prevent overcooking.'),
('Halo-halo', 'Desserts', 40.00, 120.00, 50, 9, 'Layer sweetened ingredients with shaved ice. Top with leche flan and ube ice cream.'),
('Leche Flan', 'Desserts', 50.00, 90.00, 50, 10, 'Caramelize sugar for top. Steam custard mixture until set.'),
('Dulce de Leche Cake', 'Desserts', 70.00, 150.00, 50, 1, 'Bake vanilla cake. Fill and frost with dulce de leche buttercream.'),
('Bibingka', 'Desserts', 40.00, 100.00, 50, 2, 'Mix rice flour batter. Cook in special clay pot with coals top and bottom.'),
('Peach Mango Pie', 'Desserts', 50.00, 110.00, 50, 3, 'Prepare pie crust. Fill with peach-mango mixture. Bake until golden.'),
('Buko Pandan', 'Desserts', 50.00, 130.00, 50, 4, 'Mix young coconut strips with pandan-flavored gelatin and cream.'),
('Mango Shake', 'Beverages', 40.00, 85.00, 50, 5, 'Blend fresh ripe mangoes with milk and ice.'),
('Bottomless Iced Tea', 'Beverages', 30.00, 60.00, 50, 6, 'Brew tea. Chill and serve with ice.'),
('Buko Juice', 'Beverages', 40.00, 70.00, 50, 7, 'Extract and strain fresh coconut water. Serve with coconut strips.'),
('Sago\'t Gulaman', 'Beverages', 30.00, 50.00, 50, 8, 'Combine brown sugar syrup with sago pearls and gelatin. Add water and ice.'),
('Steamed Rice', 'Sides', 20.00, 40.00, 50, 9, 'Wash rice thoroughly. Cook in rice cooker with appropriate water ratio.'),
('Mashed Potatoes', 'Sides', 50.00, 70.00, 50, 10, 'Boil potatoes until tender. Mash with butter, milk, and seasonings.'),
('Garlic Fried Rice', 'Sides', 40.00, 60.00, 50, 1, 'Sauté minced garlic until golden. Add cooked rice and season.'),
('Macaroni Salad', 'Sides', 30.00, 50.00, 50, 2, 'Cook macaroni. Mix with mayonnaise, vegetables, and seasonings.');

-- Add more ingredients needed for other dishes
INSERT INTO Ingredients (name, unit, quantity_in_stock, minimum_stock_level, cost_per_unit, last_restocked_by) VALUES
('Tamarind Base', 'kg', 20.00, 5.00, 150.00, 2),
('Peanut Butter', 'kg', 15.00, 3.00, 180.00, 2),
('Ground Beef', 'kg', 30.00, 5.00, 320.00, 2),
('Fish', 'kg', 40.00, 8.00, 250.00, 2),
('Rice Noodles', 'kg', 25.00, 5.00, 100.00, 1),
('Eggs', 'pieces', 100.00, 20.00, 8.00, 1),
('Milk', 'liters', 30.00, 5.00, 80.00, 1),
('Potatoes', 'kg', 40.00, 8.00, 60.00, 1),
('Mayonnaise', 'kg', 10.00, 2.00, 150.00, 1);

-- Add more recipe relationships
INSERT INTO DishIngredients (product_id, ingredient_id, quantity_needed, unit) VALUES
-- Kare-kare (product_id = 2)
(2, 4, 0.4, 'kg'),     -- Beef
(2, 12, 0.2, 'kg'),    -- Peanut Butter
(2, 5, 0.05, 'liters'), -- Cooking Oil

-- Sisig (product_id = 4)
(4, 2, 0.3, 'kg'),     -- Pork
(4, 8, 0.05, 'kg'),    -- Garlic
(4, 9, 0.1, 'kg'),     -- Onion

-- Burger Steak (product_id = 5)
(5, 13, 0.2, 'kg'),    -- Ground Beef
(5, 8, 0.02, 'kg'),    -- Garlic
(5, 9, 0.05, 'kg'),    -- Onion

-- Fish Sinigang (product_id = 8)
(8, 14, 0.3, 'kg'),    -- Fish
(8, 11, 0.05, 'kg'),   -- Tamarind Base
(8, 10, 0.1, 'kg'),    -- Tomato

-- Mashed Potatoes (product_id = 20)
(20, 18, 0.3, 'kg'),   -- Potatoes
(20, 17, 0.1, 'liters'), -- Milk

-- Macaroni Salad (product_id = 22)
(22, 19, 0.1, 'kg'),   -- Mayonnaise
(22, 16, 2, 'pieces'); -- Eggs

