import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utilities {

    static Scanner sc = new Scanner(System.in);

    @SuppressWarnings("Nullability")
    public static Connection setupConnection(){

        Connection connection = null;
        while (connection == null){
            System.out.print("Connection URL ('host':'portnumber'): ");
            String url = sc.nextLine().trim();

            System.out.print("Username: ");
            String root = sc.nextLine().trim();

            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + url + "/s17_group8",
                        root, //username of db
                        password); //password of db
                System.out.println("Connection successful!\n");

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return connection;
    }

    public static int getUserInput(String prompt) {
        int choice = -1;
        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print(prompt);
                choice = Integer.parseInt(sc.nextLine().trim());
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
        return choice;
    }

    public static String getStringInput(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();  // Read the whole line as a string
    }

// helper functions for transactions/assignshift method
    public static void removeEmployeeFromShift(Connection connection) {
        boolean continueFunction = true;
        while (continueFunction) {
            try {
                String query = """
                SELECT e.employee_id, e.first_name, e.last_name, ts.shift_type
                FROM Employee e
                JOIN TimeShift ts ON e.time_shiftid = ts.time_shiftid
                ORDER BY e.employee_id;
                """;

                List<List<Object>> employeesWithShift = new ArrayList<>();
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    System.out.println("\nEmployees with assigned shifts:");
                    while (resultSet.next()) {
                        int id = resultSet.getInt("employee_id");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String shiftType = resultSet.getString("shift_type");

                        employeesWithShift.add(List.of(id, firstName, lastName, shiftType));
                        System.out.printf("[%d] %s %s (Shift: %s)\n", id, firstName, lastName, shiftType);
                    }
                    System.out.println("\n[0] Cancel");

                    if (employeesWithShift.isEmpty()) {
                        System.out.println("No employees with assigned shifts found. Returning to menu...");
                        return;
                    }
                }

                boolean validInput = false;
                int selectedEmployeeId = -1;

                while (!validInput) {
                    selectedEmployeeId = Utilities.getUserInput("Enter the Employee ID to remove their shift: ");
                    if (selectedEmployeeId == 0) {
                        System.out.println("Canceling shift removal...");
                        return;
                    }

                    for (List<Object> employee : employeesWithShift) {
                        if ((int) employee.getFirst() == selectedEmployeeId) {
                            validInput = true;
                            break;
                        }
                    }

                    if (!validInput) {
                        System.out.println("Invalid Employee ID. Please try again.");
                    }
                }

                System.out.println("Are you sure you want to remove this employee's shift?");
                int confirmation = Utilities.getUserInput("Confirm (1 - Yes, 2 - No): ");
                if (confirmation != 1) {
                    System.out.println("Shift removal canceled.");
                    return;
                }

                String updateQuery = "UPDATE Employee SET time_shiftid = NULL WHERE employee_id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, selectedEmployeeId);
                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Shift successfully removed.");
                    } else {
                        System.out.println("Failed to remove shift. Please try again.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                boolean validChoice = false;
                while (!validChoice) {
                    int choice = Utilities.getUserInput("Error occurred. Continue? (1 - Yes, 2 - No): ");
                    switch (choice) {
                        case 1 -> validChoice = true;
                        case 2 -> {
                            System.out.println("Exiting function...");
                            continueFunction = false;
                            validChoice = true;
                        }
                        default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                    }
                }
            }
        }
    }

    public static void addShiftToEmployee(Connection connection) {
        try {
            String query = """
                    SELECT e.employee_id, e.first_name, e.last_name, r.role_name
                    FROM Employee e
                    JOIN Roles r ON e.role_id = r.role_id
                    WHERE e.time_shiftid IS NULL
                    ORDER BY e.employee_id;
                    """;

            List<List<Object>> employeesWithoutShift = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                System.out.println("\nEmployees without an assigned shift:");
                while (resultSet.next()) {
                    int id = resultSet.getInt("employee_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String roleName = resultSet.getString("role_name");

                    employeesWithoutShift.add(List.of(id, firstName, lastName, roleName));
                    System.out.printf("[%d] %s %s (%s)\n", id, firstName, lastName, roleName);
                }
                System.out.println("\n[0] Cancel");

                if (employeesWithoutShift.isEmpty()) {
                    System.out.println("No employees without a shift found. Returning to menu...");
                    return;
                }
            }

            int selectedEmployeeId = Utilities.getUserInput("Enter the Employee ID to assign a shift: ");
            if (selectedEmployeeId == 0) {
                System.out.println("Canceling shift assignment...");
                return;
            }

            List<Object> selectedEmployee = employeesWithoutShift.stream()
                    .filter(emp -> (int) emp.getFirst() == selectedEmployeeId)
                    .findFirst()
                    .orElse(null);

            if (selectedEmployee == null) {
                System.out.println("Invalid Employee ID. Returning to menu...");
                return;
            }

            String employeeRole = (String) selectedEmployee.get(3);

            query = """
                    SELECT ts.time_shiftid, ts.shift_type, ts.time_start, ts.time_end
                    FROM TimeShift ts
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM Employee e
                        JOIN Roles r ON e.role_id = r.role_id
                        WHERE e.time_shiftid = ts.time_shiftid
                        AND r.role_name = ?
                    )
                    LIMIT 1;
                    """;

            try (PreparedStatement shiftStmt = connection.prepareStatement(query)) {
                shiftStmt.setString(1, employeeRole);

                try (ResultSet shiftResult = shiftStmt.executeQuery()) {
                    if (!shiftResult.next()) {
                        System.out.println("No available shifts for the selected employee's role. Returning to menu...");
                        return;
                    }

                    int shiftId = shiftResult.getInt("time_shiftid");
                    String shiftType = shiftResult.getString("shift_type");
                    Time startTime = shiftResult.getTime("time_start");
                    Time endTime = shiftResult.getTime("time_end");

                    System.out.printf("Assign shift [%d] %s (%s - %s) to %s %s (%s)? (1 - Yes, 2 - No)\n",
                            shiftId, shiftType, startTime, endTime, selectedEmployee.get(1), selectedEmployee.get(2), employeeRole);

                    int choice = Utilities.getUserInput("Confirm choice: ");
                    if (choice == 1) {
                        String updateQuery = "UPDATE Employee SET time_shiftid = ? WHERE employee_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, shiftId);
                            updateStmt.setInt(2, selectedEmployeeId);
                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Shift successfully assigned.");
                            } else {
                                System.out.println("Failed to assign shift. Please try again.");
                            }
                        }
                    } else {
                        System.out.println("Shift assignment canceled.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }


     public static void emptyAllShifts(Connection connection) {
        boolean continueFunction = true;
        while (continueFunction) {
            try {
                // Check if there are any shifts assigned
                String checkQuery = "SELECT COUNT(*) FROM Employee WHERE time_shiftid IS NOT NULL";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                     ResultSet resultSet = checkStmt.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        System.out.println("All shifts are already empty. Exiting...");
                        return;
                    }
                }

                System.out.println("Are you sure you want to remove all shifts for all employees?");
                int confirmation = Utilities.getUserInput("Confirm (1 - Yes, 2 - No): ");
                if (confirmation != 1) {
                    System.out.println("Action canceled. No shifts were removed.");
                    return;
                }

                String query = "UPDATE Employee SET time_shiftid = NULL";
                try (PreparedStatement updateStmt = connection.prepareStatement(query)) {
                    int rowsAffected = updateStmt.executeUpdate();
                    System.out.println("Shifts successfully cleared.");
                    return; // Exit after successfully clearing shifts
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                boolean validChoice = false;
                while (!validChoice) {
                    int choice = Utilities.getUserInput("Error occurred. Continue? (1 - Yes, 2 - No): ");
                    switch (choice) {
                        case 1 -> validChoice = true;
                        case 2 -> {
                            System.out.println("Exiting function...");
                            continueFunction = false;
                            validChoice = true;
                        }
                        default -> System.out.println("Invalid choice. Please enter 1 or 2.");
                    }
                }
            }
        }
    }

}
