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
│   ├── IngredientBatchDAO.java     # Data access for ingredient batches
│   ├── OrderDAO.java               # Data access for orders
│   ├── SupplierDAO.java            # Data access for suppliers
│   └── DishDAO.java                # Data access for dishes
├── model/
│   ├── Customer.java               # Customer entity
│   ├── Employee.java               # Employee entity
│   ├── Ingredient.java             # Ingredient entity
│   ├── IngredientBatch.java        # Ingredient batch entity
│   ├── Order.java                  # Order entity
│   ├── OrderItem.java              # Order item entity
│   ├── Supplier.java               # Supplier entity
│   └── Dish.java                   # Dish entity
├── util/
│   ├── DatabaseConnection.java     # Database connection utility
│   └── DatabaseErrorHandler.java   # Error handling utility
├── icons/                          # Application icons and images
└── view/
    ├── MainFrame.java              # Main application window
    ├── CustomerPanel.java          # Customer management interface
    ├── EmployeePanel.java          # Employee management interface
    ├── IngredientPanel.java        # Ingredient management interface
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

### Order Management
- Create new orders with real-time ingredient availability checking
- View detailed ingredient requirements for each dish
- Track order status (Pending, In Progress, Ready, Served, Completed, Cancelled)
- Process payments with multiple payment methods
- Assign employees to orders
- View comprehensive order history with ingredient details
- Track payment status and methods
- Generate order receipts
- Soft delete orders with restoration capability

### Dish Management
- Add new dishes with recipe instructions
- Associate ingredients with dishes and specify quantities
- Set selling prices and availability status
- Categorize dishes for easy organization
- View ingredient requirements for each dish
- Track dish popularity and sales
- Soft delete dishes with restoration capability

### Ingredient Management
- Track raw ingredients with units and stock levels
- Monitor minimum stock levels and reorder points
- Manage ingredient batches with expiry tracking
- Track supplier prices for ingredients
- View ingredient usage in dishes
- Soft delete ingredients with restoration capability
- Track ingredient costs and margins

### Batch Management
- Add new ingredient batches with expiry dates
- Track batch quantities and remaining amounts
- Monitor expiring batches
- Link batches to suppliers
- Track batch costs and pricing
- View batch history and usage

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
- Track supplier prices
- Soft delete supplier records
- Restore deleted supplier data

### Payment Processing
- Process multiple payment methods
- Track transaction history
- Generate payment receipts
- Handle refunds and adjustments
- Monitor payment status

### Reporting
- Sales reports with ingredient analysis
- Customer order reports
- Employee shift reports
- Profit margin analysis
- Transaction history reports
- Deleted records reports
- Custom report generation
- Ingredient usage reports
- Batch expiry reports

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
- Context-sensitive help buttons

## Database Design

The system uses MySQL with a well-structured database design. The Entity Relationship Diagram (ERD) can be found in `sql/infomdbreal.mwb`.

### Main Tables
- `Dishes`: Stores menu items
  - Tracks prices, availability, and recipe instructions
  - Links to categories and ingredients
- `DishIngredients`: Links dishes to ingredients
  - Specifies required quantities per dish
- `Ingredients`: Raw materials
  - Tracks stock levels, units, and costs
  - Links to suppliers and batches
- `IngredientBatches`: Tracks ingredient batches
  - Records expiry dates and quantities
  - Links to suppliers
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
  - Organizes dishes
- `OrderItems`: Order line items
  - Links orders to dishes
  - Records quantities and prices

### Constraints and Validations
- Check constraints for prices and quantities
- Foreign key relationships for data integrity
- ENUM types for status fields
- Unique constraints where appropriate
- Soft delete flags for all major entities

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
- Ingredient availability checks
- Batch expiry warnings
- Stock level alerts
