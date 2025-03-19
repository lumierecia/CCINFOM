import java.sql.*;
import java.util.InputMismatchException;

public class Main {

    // TODO: records management (SELECT items FROM specific_table)
    // TODO: transactions (inserting items into tables)
    // TODO: reports

    static Connection connection = null;

    public static void main(String[] args) {

        boolean programRun = true;
        boolean inputRun;

        while (programRun){
            System.out.println("Restaurant Management System");
            System.out.println("[1] Start Connection");
            System.out.println("[2] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            connectionsMenu();
                            break;
                        case 2:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting program...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    private static void connectionsMenu() {
        if (connection == null){
            connection = Utilities.setupConnection();
        }

        RestaurantReports reports = new RestaurantReports(connection);
        RestaurantTransactions transactions = new RestaurantTransactions(connection);
        RestaurantRecordsMgt recordsMgt = new RestaurantRecordsMgt(connection);

        boolean programRun = true;
        boolean inputRun;

        while (programRun){
            System.out.println("Restaurant Management System");
            System.out.println("[1] Records Management");
            System.out.println("[2] Transactions");
            System.out.println("[3] Reports");
            System.out.println("[4] Exit Program");

            inputRun = true;
            while (inputRun) {
                try {
                    int choice = Utilities.getUserInput("Choice: ");

                    switch (choice) {
                        case 1:
                            inputRun = false;
                            recordsMgt.showMenu();
                            break;
                        case 2:
                            inputRun = false;
                            transactions.showMenu();
                            break;
                        case 3:
                            inputRun = false;
                            reports.showMenu();
                            break;
                        case 4:
                            inputRun = false;
                            programRun = false;
                            System.out.println("Exiting to main menu...");
                            break;
                        default:
                            throw new InputMismatchException("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
