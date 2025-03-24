-- Create Payments table
CREATE TABLE IF NOT EXISTS Payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('Cash', 'Credit Card') NOT NULL,
    status ENUM('Pending', 'Completed', 'Failed', 'Refunded') NOT NULL DEFAULT 'Pending',
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100),
    notes TEXT,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

-- Add indexes for better performance
CREATE INDEX idx_payment_date ON Payments(payment_date);
CREATE INDEX idx_order_id ON Payments(order_id);
CREATE INDEX idx_status ON Payments(status); 