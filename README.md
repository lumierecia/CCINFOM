# Restaurant Management System - CCINFOM G5 MP

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
├── icons/                          # Application icons and images
└── view/
    ├── MainFrame.java              # Main application window
    ├── CustomerPanel.java          # Customer management interface
    ├── EmployeePanel.java          # Employee management interface
    ├── InventoryPanel.java         # Inventory management interface
    ├── OrderPanel.java             # Order management interface
    ├── OrderHistoryPanel.java      # Order history interface
    ├── PaymentPanel.java           # Payment processing interface
    ├── RecordsPanel.java           # Records and reporting interface
    ├── ReportsPanel.java           # Reports generation interface
    ├── ShiftManagementPanel.java   # Employee shift management
    ├── SupplierPanel.java          # Supplier management interface
    ├── TransactionsPanel.java      # Transaction history interface
    ├── DeletedRecordsPanel.java    # Deleted records management
    └── HelpDialog.java             # Help documentation interface

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
- Track payment status and methods
- Generate order receipts

### Customer Management
- Add new customers
- Update customer information
- View customer history
- Track customer orders
- Soft delete customer records
- Restore deleted customer data

### Employee Management
- Add new employees
- Assign roles and shifts
- Track employee schedules
- Monitor employee performance
- Manage employee shifts
- Track employee attendance
- Soft delete employee records
- Restore deleted employee data

### Supplier Management
- Add new suppliers
- Track supplier information
- Manage supplier relationships
- Monitor ingredient supplies
- Soft delete supplier records
- Restore deleted supplier data

### Payment Processing
- Process multiple payment methods
- Track transaction history
- Generate payment receipts
- Handle refunds and adjustments
- Monitor payment status

### Reporting
- Sales reports
- Customer order reports
- Employee shift reports
- Profit margin analysis
- Transaction history reports
- Deleted records reports
- Custom report generation

### Data Management
- Soft deletion for all major entities
- Deleted records management
- Data restoration capabilities
- Comprehensive audit trail
- Backup and recovery options

### Help and Documentation
- Built-in help system
- User documentation
- System guides
- Troubleshooting assistance

## Database Design

The system uses MySQL with a well-structured database design. The Entity Relationship Diagram (ERD) can be found in `sql/infomdbreal.mwb`.

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

4. Update database connection details in `DatabaseConnection.java` (change the password!!)

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
