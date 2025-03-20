import java.sql.*;
import java.util.*;

public class RestaurantRecordsMgt {
    Connection connection;

    public RestaurantRecordsMgt(Connection connection) {
        this.connection = connection;
    }


    public void showMenu() {

        boolean programRun = true;
        boolean inputRun;

        while (programRun) {
            System.out.println("\nRMS Records Management Queries");
            System.out.println("[1] View a product record and the list of orders containing that product");
            System.out.println("[2] View a customer record and their order history");
            System.out.println("[3] View an employee and the shift assigned to them");
            System.out.println("[4] View an order and the inventory it affected");
            System.out.println("[5] Exit Records Management");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    inputRun = switch (choice) {
                        case 1 -> {
                            viewProduct();
                            yield false;
                        }
                        case 2 -> {
                            viewCustomer();
                            yield false;
                        }
                        case 3 -> {
                            viewEmployee();
                            yield false;
                        }
                        case 4 -> {
                            viewOrder();
                            yield false;
                        }
                        case 5 -> {
                            programRun = false;
                            yield false;
                        }
                        default -> throw new InputMismatchException("Invalid input.");
                    };
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    private void viewProduct() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT product_id, product_name, quantity FROM Inventory;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> rows = new ArrayList<>();

                System.out.println("List of all items in the inventory: ");
                while (resultSet.next()) {
                    int id = resultSet.getInt("product_id");
                    String name = resultSet.getString("product_name");
                    int qty = resultSet.getInt("quantity");

                    rows.add(id);

                    System.out.printf("[%d] %s (Stock: %d)\n", id, name, qty);
                }

                try {
                    int id = Utilities.getUserInput("Product ID of item to view (Enter 0 to go back): ");

                    if (id == 0) {  // Back option
                        System.out.println("Returning to previous menu...");
                        programRun = false;  // Exit the loop to go back
                    } else if (rows.contains(id)) {
                        query = """
                            SELECT o.order_id, oi.quantity AS quantity_ordered, o.order_datetime
                            FROM Inventory i
                            JOIN Order_Item oi ON i.product_id = oi.product_id
                            JOIN Orders o ON o.order_id = oi.order_id
                            WHERE i.product_id = ?
                            ORDER BY oi.order_item_id;
                           """;

                        try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                            detailStmt.setInt(1, id);
                            try (ResultSet detailResult = detailStmt.executeQuery()) {

                                System.out.println("\nOrder Details for Product ID: " + id);
                                System.out.println("-".repeat(100));
                                System.out.printf("%-10s %-20s %-15s\n", "Order ID", "Quantity Ordered", "Order Timestamp");
                                System.out.println("-".repeat(100));

                                boolean hasRecords = false;

                                while (detailResult.next()) {
                                    hasRecords = true;

                                    int orderId = detailResult.getInt("order_id");
                                    int quantityOrdered = detailResult.getInt("quantity_ordered");
                                    String orderDate = detailResult.getString("order_datetime");

                                    System.out.printf("%-10d %-20d %-20s\n", orderId, quantityOrdered, orderDate);
                                }

                                if (!hasRecords) {
                                    System.out.println("No order records found for this product.");
                                }

                                System.out.println("-".repeat(100));
                            }
                        }

                        boolean validChoice = false;
                        while (!validChoice) {
                            int choice = Utilities.getUserInput("Continue viewing product records? (1 - yes, 2 - no, 0 - back): ");

                            switch (choice) {
                                case 1 -> validChoice = true;  // Continue viewing
                                case 2 -> {
                                    System.out.println("Exiting view products menu...");
                                    programRun = false;  // Exit the loop
                                    validChoice = true;
                                }
                                case 0 -> {
                                    System.out.println("Returning to previous menu...");
                                    programRun = false;  // Exit the loop and go back
                                    validChoice = true;
                                }
                                default -> System.out.println("Invalid choice. Please enter 1, 2, or 0.");
                            }
                        }

                    } else {
                        throw new InputMismatchException("Product ID not found.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }

            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }
        }
    }


    private void viewCustomer() {
        boolean programRun = true;

        while (programRun) {
            String query = "SELECT customer_id, first_name, last_name FROM Customers;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> customerIds = new ArrayList<>();

                System.out.println("List of all customers:");
                System.out.println("-".repeat(50));
                System.out.printf("%-10s %-20s\n", "Customer ID", "\tName");
                System.out.println("-".repeat(50));

                boolean hasCustomers = false;

                while (resultSet.next()) {
                    hasCustomers = true;
                    int id = resultSet.getInt("customer_id");
                    String name = "\t\t" + resultSet.getString("first_name") + " " + resultSet.getString("last_name");

                    customerIds.add(id);
                    System.out.printf("%-10d %-20s\n", id, name);
                }

                if (!hasCustomers) {
                    System.out.println("No customers found.");
                    break; // Exit the outer loop if no customers are found.
                }

                System.out.println("-".repeat(50));

                boolean inputRun = true;

                while (inputRun) {
                    try {
                        int id = Utilities.getUserInput("Enter Customer ID to view (Enter 0 to go back): ");

                        if (id == 0) {
                            System.out.println("Returning to previous menu...");
                            inputRun = false;  // Exit inner loop
                            programRun = false;  // Exit outer loop to stop the whole process
                        } else if (customerIds.contains(id)) {
                            query = "SELECT * FROM Customers WHERE customer_id = ?;";

                            try (PreparedStatement customerStmt = connection.prepareStatement(query)) {
                                customerStmt.setInt(1, id);

                                try (ResultSet customerDetails = customerStmt.executeQuery()) {
                                    if (customerDetails.next()) {
                                        System.out.println("\nCustomer Details:");
                                        System.out.println("-".repeat(50));
                                        System.out.printf("ID: %d\n", customerDetails.getInt("customer_id"));
                                        System.out.printf("Name: %s %s\n",
                                                customerDetails.getString("first_name"),
                                                customerDetails.getString("last_name"));
                                        System.out.printf("Email: %s\n", customerDetails.getString("email"));
                                        System.out.printf("Phone: %s\n", customerDetails.getString("phonenumber"));
                                        System.out.printf("Address: %s\n", customerDetails.getString("address"));
                                        System.out.println("-".repeat(50));
                                    }
                                }
                            }

                            query = """
                                SELECT o.order_id, o.order_type, o.order_datetime
                                FROM Orders o
                                WHERE o.customer_id = ?
                                ORDER BY o.order_datetime DESC;
                                """;

                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, id);

                                try (ResultSet detailResult = detailStmt.executeQuery()) {
                                    System.out.println("\nOrder History:");
                                    System.out.println("-".repeat(70));
                                    System.out.printf("%-10s %-15s %-20s\n", "Order ID", "Order Type", "Order Date");
                                    System.out.println("-".repeat(70));

                                    boolean hasRecords = false;

                                    while (detailResult.next()) {
                                        hasRecords = true;

                                        int orderId = detailResult.getInt("order_id");
                                        String orderType = detailResult.getString("order_type");
                                        String orderDate = detailResult.getString("order_datetime");

                                        System.out.printf("%-10d %-15s %-20s\n", orderId, orderType, orderDate);
                                    }

                                    if (!hasRecords) {
                                        System.out.println("No order history found for this customer.");
                                    }

                                    System.out.println("-".repeat(70));
                                }
                            }

                            boolean validChoice = false;
                            while (!validChoice) {
                                int choice = Utilities.getUserInput("Continue viewing customer records? (1 - yes, 2 - no): ");

                                switch (choice) {
                                    case 1 -> validChoice = true;
                                    case 2 -> {
                                        System.out.println("Exiting view customer menu...");
                                        programRun = false;
                                        inputRun = false;
                                        validChoice = true;
                                    }
                                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                }
                            }
                        } else {
                            throw new InputMismatchException("Customer ID not found.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database error: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
            }
        }
    }


    private void viewEmployee() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT employee_id, first_name, last_name FROM Employee;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> employeeIds = new ArrayList<>();

                System.out.println("List of all employees: ");
                while (resultSet.next()) {
                    int id = resultSet.getInt("employee_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    employeeIds.add(id);

                    System.out.printf("[%d] %s %s\n", id, firstName, lastName);
                }

                int id;
                boolean validInput = false;
                while (!validInput) {
                    try {
                        id = Utilities.getUserInput("Employee ID of employee to view (Enter 0 to go back): ");

                        if (id == 0) {
                            System.out.println("Returning to previous menu...");
                            programRun = false;
                            break;
                        }

                        if (employeeIds.contains(id)) {
                            validInput = true;

                            query = """
                            SELECT e.first_name, e.last_name, r.role_name, t.shift_type, t.time_start, t.time_end
                            FROM Employee e
                            JOIN Roles r ON e.role_id = r.role_id
                            LEFT JOIN TimeShift t ON e.time_shiftid = t.time_shiftid
                            WHERE e.employee_id = ?
                            ORDER BY e.employee_id;
                            """;

                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, id);
                                try (ResultSet detailResult = detailStmt.executeQuery()) {

                                    System.out.println("\nEmployee Details for Employee ID: " + id);
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-10s %-15s %-15s %-20s %-20s %-20s %-20s\n",
                                            "Employee ID", "First Name", "Last Name", "Role",
                                            "Shift Type", "Start Time", "End Time");
                                    System.out.println("-".repeat(100));

                                    boolean hasRecords = false;

                                    while (detailResult.next()) {
                                        hasRecords = true;

                                        String firstName = detailResult.getString("first_name");
                                        String lastName = detailResult.getString("last_name");
                                        String roleName = detailResult.getString("role_name");
                                        String shiftType = detailResult.getString("shift_type");
                                        Time startTime = detailResult.getTime("time_start");
                                        Time endTime = detailResult.getTime("time_end");

                                        System.out.printf(" %-10d %-15s %-15s %-20s %-20s %-20s %-20s\n",
                                                id,
                                                firstName != null ? firstName : "N/A",
                                                lastName != null ? lastName : "N/A",
                                                roleName != null ? roleName : "N/A",
                                                shiftType != null ? shiftType : "N/A",
                                                startTime != null ? startTime : "N/A",
                                                endTime != null ? endTime : "N/A");
                                    }

                                    if (!hasRecords) {
                                        System.out.println("No records found for this employee.");
                                    }

                                    System.out.println("-".repeat(100));
                                }
                            }

                        } else {
                            System.out.println("Employee ID not found.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
                boolean validChoice = false;
                while (!validChoice) {
                    int choice = Utilities.getUserInput("Query error occurred. Continue viewing employee records? (1 - yes, 2 - no): ");

                    switch (choice) {
                        case 1 -> validChoice = true;
                        case 2 -> {
                            System.out.println("Exiting view employees menu...");
                            programRun = false;
                            validChoice = true;
                        }
                        default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                    }
                }
            }

            if (programRun) {
                boolean validChoice = false;
                while (!validChoice) {
                    int choice = Utilities.getUserInput("Continue viewing employee records? (1 - yes, 2 - no): ");

                    switch (choice) {
                        case 1 -> validChoice = true;
                        case 2 -> {
                            System.out.println("Exiting view employees menu...");
                            programRun = false;
                            validChoice = true;
                        }
                        default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                    }
                }
            }
        }
    }
    

    private void viewOrder() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT order_id, order_type, order_status, order_datetime FROM Orders;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> rows = new ArrayList<>();

                System.out.println("List of all orders: ");

                while (resultSet.next()) {
                    int orderID = resultSet.getInt("order_id");
                    String orderType = resultSet.getString("order_type");
                    String orderStatus = resultSet.getString("order_status");
                    String orderDateTime = resultSet.getString("order_datetime");

                    rows.add(orderID);

                    System.out.printf("[%d] %s (%s) made on %s\n", orderID, orderType, orderStatus, orderDateTime);
                }

                boolean inputRun = true;
                while (inputRun) {
                    try {
                        int orderID = Utilities.getUserInput("Order ID to view (Enter 0 to go back): ");

                        // If the user enters 0, exit to the previous menu
                        if (orderID == 0) {
                            System.out.println("Returning to previous menu...");
                            programRun = false;  // Set programRun to false to exit the loop and go back
                            inputRun = false;    // Exit the inner loop as well
                            break;               // Exit the inner loop to avoid further processing
                        }

                        // If the orderID is valid, show details
                        if (rows.contains(orderID)) {
                            // Query to get the details of the selected order and its affected inventory
                            query = """
                                SELECT o.order_id, i.product_name, oi.quantity AS "quantity_ordered",
                                       i.quantity AS "current_stock"
                                FROM Orders o
                                JOIN Order_Item oi ON o.order_id = oi.order_id
                                JOIN Inventory i ON i.product_id = oi.product_id
                                WHERE o.order_id = ?
                                ORDER BY o.order_id, i.product_name;
                                """;

                            try (PreparedStatement detailStmt = connection.prepareStatement(query)) {
                                detailStmt.setInt(1, orderID);  // Set the orderID in the query
                                try (ResultSet detailResult = detailStmt.executeQuery()) {

                                    System.out.println("\nProducts in Order ID: " + orderID);
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-30s %-20s %-50s\n",
                                            "Product Name", "Quantity Ordered", "Current Stock (after order was made)");
                                    System.out.println("-".repeat(100));

                                    boolean hasRecords = false;

                                    while (detailResult.next()) {
                                        hasRecords = true;

                                        String productName = detailResult.getString("product_name");
                                        int quantityOrdered = detailResult.getInt("quantity_ordered");
                                        int currentStock = detailResult.getInt("current_stock");

                                        System.out.printf("%-30s %-20s %-10s\n",
                                                productName, quantityOrdered, currentStock);
                                    }

                                    if (!hasRecords) {
                                        System.out.println("No order records found for this order.");
                                    }

                                    System.out.println("-".repeat(100));
                                }
                            }

                            boolean validChoice = false;
                            while (!validChoice) {
                                int choice = Utilities.getUserInput("Continue viewing order records? (1 - yes, 2 - no): ");

                                switch (choice) {
                                    case 1 -> validChoice = true;
                                    case 2 -> {
                                        System.out.println("Exiting view order menu...");
                                        programRun = false;
                                        inputRun = false;
                                        validChoice = true;
                                    }
                                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                }
                            }
                        } else {  
                            throw new InputMismatchException("Order ID not found.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            }
        }
    }
}
