import java.sql.*;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.InputMismatchException;
import java.util.List;

public class RestaurantReports {

    Connection connection;
    Statement statement;

    public RestaurantReports(Connection connection) {
        this.connection = connection;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void showMenu() {

        // TODO: figure out how to troubleshoot queries

        boolean programRun = true;
        boolean inputRun;

        while (programRun){
            System.out.println("\nRMS Report Queries");
            System.out.println("[1] View Sales Report");
            System.out.println("[2] View Customer Order Report");
            System.out.println("[3] View Employee Shift Report");
            System.out.println("[4] View Profit Margin Report");
            System.out.println("[5] Go Back");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    inputRun = switch (choice) {
                        case 1 -> {
                            showSalesReport();
                            yield false;
                        }
                        case 2 -> {
                            showCustomerOrderReport();
                            yield false;
                        }
                        case 3 -> {
                            showEmployeeShiftReport();
                            yield false;
                        }
                        case 4 -> {
                            showProfitMarginReport();
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

private void showSalesReport() {
        boolean programRun = true;
        while (programRun) {
            try {
                String datePattern = "";
                boolean validInputs = false;

                // Collect user input for date filtering
                while (!validInputs) {
                    try {
                        int day = Utilities.getUserInput("Enter day (0 if skip): ");
                        int month = Utilities.getUserInput("Enter month (0 if skip): ");
                        int year = Utilities.getUserInput("Enter year (0 if skip): ");

                        if (day < 0 || month < 0 || year < 0)
                            throw new IllegalArgumentException("Day, month, and year must be non-negative.");

                        // Validate the year range (e.g., 1900–2100)
                        if (year > 0 && (year < 2014 || year > 2024))
                            throw new IllegalArgumentException("Invalid year. Please enter a value within the last 10 years (2014-2024) or 0 to skip.");


                        // Validate the month range
                        if ((month < 1 || month > 12) && month != 0)
                            throw new IllegalArgumentException("Invalid month. Please enter a value between 1 and 12, or 0 to skip.");

                        // Validate the day range
                        if ((day < 1 || day > 31) && day != 0)
                            throw new IllegalArgumentException("Invalid day. Please enter a value between 1 and 31, or 0 to skip.");

                        // Build the date pattern
                        String yearStr = (year == 0) ? "%" : String.format("%04d", year);
                        String monthStr = (month == 0) ? "%" : String.format("%02d", month);
                        String dayStr = (day == 0) ? "%" : String.format("%02d", day);

                        datePattern = String.format("%s-%s-%s", yearStr, monthStr, dayStr);

                        // Validate fully specified dates
                        if (day != 0 && month != 0 && year != 0) {
                            Date.valueOf(String.format("%04d-%02d-%02d", year, month, day));
                        }

                        validInputs = true;
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage()); // Print custom error messages
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter numeric values only.");
                    }
                }

                // Query to fetch sales report
                String query = """
                SELECT
                    DATE(o.order_datetime) AS sales_date,
                    SUM(oi.quantity * i.sell_price) AS total_sales,
                    AVG(oi.quantity * i.sell_price) AS average_sales,
                    i.product_name AS top_product,
                    SUM(oi.quantity) AS product_sold
                FROM Orders o
                JOIN Order_Item oi ON o.order_id = oi.order_id
                JOIN Inventory i ON oi.product_id = i.product_id
                WHERE DATE(o.order_datetime) LIKE ?
                GROUP BY sales_date, i.product_name
                ORDER BY sales_date, product_sold DESC;
                """;


                // Execute the query
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, datePattern); // Use the built date pattern

                    try (ResultSet result = statement.executeQuery()) {
                        System.out.println("\nSales Report");
                        System.out.println("-".repeat(120));
                        System.out.printf("%-15s %-15s %-15s %-25s %-15s\n",
                                "Sales Date", "Total Sales", "Average Sales", "Top Product", "Units Sold");
                        System.out.println("-".repeat(120));

                        boolean hasRecords = false;

                        // Process result set
                        while (result.next()) {
                            hasRecords = true;

                            String salesDate = result.getString("sales_date");
                            double totalSales = result.getDouble("total_sales");
                            double averageSales = result.getDouble("average_sales");
                            String topProduct = result.getString("top_product");
                            int unitsSold = result.getInt("product_sold");

                            System.out.printf("%-15s %-15.2f %-15.2f %-25s %-15d\n",
                                    salesDate, totalSales, averageSales, topProduct, unitsSold);
                        }

                        if (!hasRecords) {
                            System.out.println("No order records found for the specified date range.");
                        }

                        System.out.println("-".repeat(120));
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database query error. Please check your database connection and query.");
            }

            // Continue or exit
            boolean validChoice = false;
            while (!validChoice) {
                int choice = Utilities.getUserInput("Continue viewing sales report? (1 - yes, 2 - no): ");
                switch (choice) {
                    case 1 -> validChoice = true;
                    case 2 -> {
                        System.out.println("Exiting sales report menu...");
                        programRun = false;
                        validChoice = true;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }
    }

    private void showCustomerOrderReport() {
    boolean programRun = true;
    while (programRun) {
        try {
            String date = "";

            boolean validInputs = false;
            while (!validInputs) {
                int day = Utilities.getUserInput("Enter day (0 to skip): ");
                int month = Utilities.getUserInput("Enter month (0 to skip): ");
                int year = Utilities.getUserInput("Enter year (0 to skip): ");

                if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();
                if (day != 0 && month != 0 && year != 0) { // valid date
                    date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();
                } else { // partial date filter
                    String yearStr = (year == 0) ? "%" : String.format("%04d", year);
                    String monthStr = (month == 0) ? "%" : String.format("%02d", month);
                    String dayStr = (day == 0) ? "%" : String.format("%02d", day);

                    date = String.format("%s-%s-%s", yearStr, monthStr, dayStr);
                }
                validInputs = true;
            }

            String query = """
            SELECT
                COUNT(DISTINCT o.order_id) AS total_orders,
                SUM(oi.quantity * i.sell_price) AS total_amount_spent,
                i.product_name AS most_bought_product,
                SUM(oi.quantity) AS most_bought_quantity
            FROM Orders o
            JOIN Order_Item oi ON o.order_id = oi.order_id
            JOIN Inventory i ON oi.product_id = i.product_id
            WHERE DATE(o.order_datetime) LIKE ?
            GROUP BY i.product_name
            ORDER BY most_bought_quantity DESC
            LIMIT 1;
        """;


            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, date + "%");

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("\nCustomer Order Report");
                    System.out.println("-".repeat(80));
                    System.out.printf("%-20s %-30s %-20s %-20s\n", "Total Orders", "Total Amount Spent", "Most Bought Product", "Most Bought Quantity");
                    System.out.println("-".repeat(80));

                    boolean hasRecords = false;

                    if (resultSet.next()) {
                        hasRecords = true;

                        int totalOrders = resultSet.getInt("total_orders");
                        double totalAmountSpent = resultSet.getDouble("total_amount_spent");
                        String mostBoughtProduct = resultSet.getString("most_bought_product");
                        int mostBoughtQuantity = resultSet.getInt("most_bought_quantity");

                        System.out.printf("%-20d %-30.2f %-20s %-20d\n", totalOrders, totalAmountSpent, mostBoughtProduct, mostBoughtQuantity);
                    }

                    if (!hasRecords) {
                        System.out.println("No orders found for the specified date.");
                    }

                    System.out.println("-".repeat(80));
                }
            }

        } catch (SQLException e) {
            System.out.println("Query error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter valid numeric values.");
        } catch (IllegalArgumentException e) {
            System.out.println("Date entered is not valid. Please try again.");
        }

        boolean validChoice = false;
        while (!validChoice) {
            int choice = Utilities.getUserInput("Continue viewing customer order report? (1 - Yes, 2 - No): ");
            switch (choice) {
                case 1 -> validChoice = true;
                case 2 -> {
                    System.out.println("Exiting customer order report menu...");
                    programRun = false;
                    validChoice = true;
                }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
}

    private void showEmployeeShiftReport() {
        //TODO: Number of shifts per employee and total hours worked within a given year and month.
            boolean programRun = true;
            while (programRun) {

                int year, month;
                String date = "";
                boolean validInputs = false;

                    while (!validInputs) {
                        try {
                            year = Utilities.getUserInput("Enter year (0 if skip): ");
                            month = Utilities.getUserInput("Enter month (0 if skip): ");
                            if (year < 0 || month < 0 || month > 12) throw new InputMismatchException();

                            date = String.format("%s-%s-%%",
                                    year == 0 ? "%" : String.format("%04d", year),
                                    month == 0 ? "%" : String.format("%02d", month));

                            validInputs = true;
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please try again.");
                        }
                    }

                    String query = """
                    SELECT
                        e.employee_id,
                        e.first_name,
                        e.last_name,
                           COUNT(a.order_id) AS num_of_shifts,
                                SUM(
                                    TIMESTAMPDIFF(
                                        HOUR,
                                        ts.time_start,
                                        CASE
                                            WHEN ts.time_end < ts.time_start THEN ADDTIME(ts.time_end, '24:00:00')
                                            ELSE ts.time_end
                                        END
                                    )
                                ) AS total_hours_worked
                    FROM
                        Employee e
                    JOIN
                        Assigned_Employee_to_Order a ON e.employee_id = a.employee_id
                    JOIN
                        Orders o ON a.order_id = o.order_id
                    JOIN
                        TimeShift ts ON e.time_shiftid = ts.time_shiftid
                    WHERE
                        o.order_datetime LIKE ?
                    GROUP BY
                        e.employee_id, e.first_name, e.last_name, ts.time_start, ts.time_end
                    ORDER BY
                        total_hours_worked DESC;
                    """;

                        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                            pstmt.setString(1, date);

                            try (ResultSet resultSet = pstmt.executeQuery()) {
                                List<Integer> rows = new ArrayList<>();
                                System.out.println("Employee Shift Report (ordered by shifts): ");
                                System.out.printf("%-10s %-15s %-15s %-15s %-15s\n", "Emp ID", "First Name", "Last Name", "Shifts", "Total Hours");
                                System.out.println("-".repeat(100));

                                while (resultSet.next()) {
                                    int employeeId = resultSet.getInt("employee_id");
                                    String firstName = resultSet.getString("first_name");
                                    String lastName = resultSet.getString("last_name");
                                    int numOfShifts = resultSet.getInt("num_of_shifts");
                                    int totalHoursWorked = resultSet.getInt("total_hours_worked");

                                    rows.add(employeeId);
                                    System.out.printf("%-10d %-15s %-15s %-15d %-15d\n", employeeId, firstName, lastName, numOfShifts, totalHoursWorked);
                                }

                                if (rows.isEmpty()) {
                                    System.out.println("No shift records found for the specified period.");
                                }
                                System.out.println("-".repeat(100));

                                boolean validChoice = false;
                                while (!validChoice) {
                                    int choice = Utilities.getUserInput("Continue viewing shift report? (1 - yes, 2 - no): ");
                                    switch (choice) {
                                        case 1 -> validChoice = true;
                                        case 2 -> {
                                            System.out.println("Exiting shift report menu...");
                                            programRun = false;
                                            validChoice = true;
                                        }
                                        default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println("Query error, edit MySQL database and try again.");
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input. Please try again.");
                        }
            }
        }

   private void showProfitMarginReport() {
        // TODO: Profit margins for each menu item within a given day, month, or year

        boolean programRun = true;
        while (programRun) {

            try {
                String date = "";
                String displayDate = "";

                boolean validInputs = false;
                while (!validInputs) {
                    int day = Utilities.getUserInput("Enter day (0 if skip): ");
                    int month = Utilities.getUserInput("Enter month (0 if skip): ");
                    int year = Utilities.getUserInput("Enter year (0 if skip): ");


                    if (day < 0 || month < 0 || year < 0) throw new InputMismatchException();

                    if (day != 0 && month != 0 && year != 0) {
                        date = Date.valueOf(String.format("%d-%02d-%02d", year, month, day)).toString();
                        displayDate = date;

                    } else {
                        date = String.format("%s-%s-%s",
                                year == 0 ? "%" : String.format("%04d", year),
                                month == 0 ? "%" : String.format("%02d", month),
                                day == 0 ? "%" : String.format("%02d%%", day));
                        displayDate = String.format("%s-%s-%s",
                                year == 0 ? "XXXX" : String.format("%04d", year),
                                month == 0 ? "XX" : String.format("%02d", month),
                                day == 0 ? "XX" : String.format("%02d%%", day));
                    }
                    validInputs = true;
                }

                String query = """
                SELECT
                    i.product_id,
                    o.order_id,
                    COUNT(*) AS total_orders_with_item, 
                    SUM(oi.quantity) AS total_amount_ordered, 
                    SUM(oi.quantity) * i.sell_price AS total_revenue, 
                    SUM(oi.quantity) * i.make_price AS total_cost, 
                    (SUM(oi.quantity) * i.sell_price) - (SUM(oi.quantity) * i.make_price) AS total_profit, 
                    o.order_datetime 
                FROM Inventory i 
                JOIN Order_Item oi ON i.product_id = oi.product_id 
                JOIN Orders o ON o.order_id = oi.order_id 
                WHERE DATE(o.order_datetime) LIKE ? 
                GROUP BY i.product_id, o.order_id, o.order_datetime 
                ORDER BY total_profit DESC;
                """;


                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, date);

                    try (ResultSet result = statement.executeQuery()) {
                        System.out.println("\nProfit Margin Report for " + displayDate + " (sorted by profit):");
                        System.out.println("-".repeat(120));
                        System.out.printf("%-10s %-10s %-20s %-15s %-15s %-10s %-10s %-10s\n",
                                "Product ID", "Order ID", "Order Date", "Total Orders", "Total Quantity", "Revenue", "Cost", "Profit");
                        System.out.println("-".repeat(120));

                        boolean hasRecords = false;

                        while (result.next()) {
                            hasRecords = true;

                            int productID = result.getInt("product_id");
                            int orderId = result.getInt("order_id");
                            int totalOrdersWithItem = result.getInt("total_orders_with_item");
                            int totalAmountOrdered = result.getInt("total_amount_ordered");
                            double totalRevenue = result.getDouble("total_revenue");
                            double totalCost = result.getDouble("total_cost");
                            double totalProfit = result.getDouble("total_profit");
                            String orderDate = result.getString("order_datetime");

                            System.out.printf("%-10d %-10d %-20s %-15d %-15d %-10.2f %-10.2f %-10.2f\n",
                                    productID, orderId, orderDate, totalOrdersWithItem, totalAmountOrdered, totalRevenue, totalCost, totalProfit);
                        }

                        if (!hasRecords) {
                            System.out.println("No order records found for this product.");
                        }
                        System.out.println("-".repeat(120));
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error, edit MySQL database and try again.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            } catch (IllegalFormatException e) {
                System.out.println("Date entered not valid, try again.");
            }

            boolean validChoice = false;
            while (!validChoice) {
                int choice = Utilities.getUserInput("Continue viewing profit margin report? (1 - yes, 2 - no): ");
                switch (choice) {
                    case 1 -> validChoice = true;
                    case 2 -> {
                        System.out.println("Exiting profit margin report menu...");
                        programRun = false;
                        validChoice = true;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }
    }
}
