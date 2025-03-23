# Restaurant Management System

A Java-based restaurant management system that handles inventory, orders, employees, and customer management.

## Project Structure

```
src/
├── controller/
│   └── RestaurantController.java    # Main controller handling business logic
├── dao/
│   ├── CustomerDAO.java            # Data access for customers
│   ├── EmployeeDAO.java            # Data access for employees
│   ├── IngredientDAO.java          # Data access for ingredients
│   ├── InventoryDAO.java           # Data access for inventory items
│   ├── OrderDAO.java               # Data access for orders
│   └── SupplierDAO.java            # Data access for suppliers
├── model/
│   ├── Customer.java               # Customer entity
│   ├── Employee.java               # Employee entity
│   ├── Ingredient.java             # Ingredient entity
│   ├── Inventory.java              # Inventory item entity
│   ├── Order.java                  # Order entity
│   ├── OrderItem.java              # Order item entity
│   └── Supplier.java               # Supplier entity
├── util/
│   ├── DatabaseConnection.java     # Database connection utility
│   └── DatabaseErrorHandler.java   # Error handling utility
└── view/
    ├── MainFrame.java              # Main application window
    ├── CustomerPanel.java          # Customer management interface
    ├── EmployeePanel.java          # Employee management interface
    ├── InventoryPanel.java         # Inventory management interface
    ├── OrderPanel.java             # Order management interface
    ├── RecordsPanel.java           # Records and reporting interface
    └── SupplierPanel.java          # Supplier management interface

sql/
├── Group8_DB.sql                   # Database schema and sample data
└── infomdbreal.eerd                # Entity Relationship Diagram
```

## Features

### Inventory Management
- View all inventory items
- Filter items by category (Main Course, Desserts, Beverages, Sides)
- Add new inventory items
- Update existing items
- Delete items
- Track item status (Available/Unavailable)
- Monitor low stock items
- Automatic status updates based on quantity

### Order Management
- Create new orders
- Track order status (In Progress, Ready, Served, Completed, Cancelled)
- Process payments
- Assign employees to orders
- View order history

### Customer Management
- Add new customers
- Update customer information
- View customer history
- Track customer orders

### Employee Management
- Add new employees
- Assign roles and shifts
- Track employee schedules
- Monitor employee performance

### Supplier Management
- Add new suppliers
- Track supplier information
- Manage supplier relationships
- Monitor ingredient supplies

### Reporting
- Sales reports
- Customer order reports
- Employee shift reports
- Profit margin analysis

## Database Design

The system uses MySQL with a well-structured database design. The Entity Relationship Diagram (ERD) can be found in `sql/infomdbreal.eerd`.

### Main Tables
- `InventoryItems`: Stores product information
  - Tracks quantity, prices, and status
  - Links to categories and employees
  - Includes recipe instructions
- `Orders`: Tracks customer orders
  - Supports multiple order types (Dine-In, Takeout, Delivery)
  - Tracks payment status and method
  - Links to customers and employees
- `Customers`: Stores customer information
  - Tracks contact details and order history
- `Employees`: Manages employee data
  - Assigns roles and shifts
  - Tracks performance and assignments
- `Suppliers`: Tracks supplier information
  - Manages supplier relationships
  - Tracks contact details and status
- `Categories`: Product categories
  - Organizes inventory items
- `Ingredients`: Raw materials
  - Tracks stock levels and costs
- `OrderItems`: Order line items
  - Links orders to products
  - Records quantities and prices

### Constraints and Validations
- Check constraints for prices and quantities
- Foreign key relationships for data integrity
- ENUM types for status fields
- Unique constraints where appropriate

## Getting Started

1. Ensure you have Java 17 or later installed
2. Set up MySQL database
3. Run the `Group8_DB.sql` script to create the database schema
4. Update database connection details in `DatabaseConnection.java`
5. Compile and run the application:
   ```bash
   javac src/view/MainFrame.java
   java src.view.MainFrame
   ```

## Error Handling

The system includes comprehensive error handling through `DatabaseErrorHandler.java`:
- Database constraints
- Invalid input validation
- Business logic violations
- Connection issues
- User-friendly error messages
- Proper exception propagation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Future Improvements

### 1. Restock History Tracking
- Implement a dedicated `RestockHistory` table to track:
  - Complete history of all restocking events
  - Previous quantities and changes
  - Timestamps of all restocks
  - Employee responsible for each restock
  - Notes and reasons for restocking
- Benefits:
  - Better audit trails
  - Inventory movement analysis
  - Employee performance tracking
  - Seasonal trend identification
  - More accurate reporting

### 2. Additional Feature Ideas
- Advanced reporting and analytics
- Inventory forecasting
- Employee scheduling optimization
- Customer loyalty program
- Mobile application support

## Current Implementation

The current system tracks the most recent restock through:
- `last_restock` timestamp in `InventoryItems`
- `last_restocked_by` employee reference
- Current quantity tracking
- Status updates (Available/Unavailable)

This provides basic inventory management while maintaining simplicity and staying within the current database specifications.