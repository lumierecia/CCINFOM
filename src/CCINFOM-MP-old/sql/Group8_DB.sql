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

CREATE TABLE IF NOT EXISTS Inventory (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL UNIQUE KEY,
    category ENUM('Main Course', 'Desserts', 'Beverages', 'Sides') NOT NULL,
    make_price DECIMAL(10, 2) NOT NULL CHECK (make_price > 0),
    sell_price DECIMAL(10, 2) NOT NULL CHECK (sell_price > 0),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    last_restock TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_restocked_by INT NOT NULL,
    FOREIGN KEY (last_restocked_by) REFERENCES Employee(employee_id)
);

INSERT INTO Inventory (product_name, category, make_price, sell_price, quantity, last_restocked_by) 
VALUES 	('Pork Sinigang', 'Main Course', 170.00, 250.00, 50, 1),
		('Kare-kare', 'Main Course', 230.00, 350.00, 50, 2),
		('Chicken Adobo', 'Main Course', 120.00, 220.00, 50, 3),
		('Sisig', 'Main Course', 160.00, 280.00, 50, 4),
		('Burger Steak', 'Main Course', 90.00, 190.00, 50, 5),
		('Pancit Palabok', 'Main Course', 100.00, 180.00, 50, 6),
		('Pancit Bihon', 'Main Course', 80.00, 170.00, 50, 7),
		('Fish Sinigang', 'Main Course', 120.00, 240.00, 50, 8),
		('Halo-halo', 'Desserts', 40.00, 120.00, 50, 9),
		('Leche Flan', 'Desserts', 50.00, 90.00, 50, 10),
		('Dulce de Leche Cake', 'Desserts', 70.00, 150.00, 50, 1),
		('Bibingka', 'Desserts', 40.00, 100.00, 50, 2),
		('Peach Mango Pie', 'Desserts', 50.00, 110.00, 50, 3),
		('Buko Pandan', 'Desserts', 50.00, 130.00, 50, 4),
		('Mango Shake', 'Beverages', 40.00, 85.00, 50, 5),
		('Bottomless Iced Tea', 'Beverages', 30.00, 60.00, 50, 6),
		('Buko Juice', 'Beverages', 40.00, 70.00, 50, 7),
		('Sago\'t Gulaman', 'Beverages', 30.00, 50.00, 50, 8),
		('Steamed Rice', 'Sides', 20.00, 40.00, 50, 9),
		('Mashed Potatoes', 'Sides', 50.00, 70.00, 50, 10),
		('Garlic Fried Rice', 'Sides', 40.00, 60.00, 50, 1),
		('Macaroni Salad', 'Sides', 30.00, 50.00, 50, 2);


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

