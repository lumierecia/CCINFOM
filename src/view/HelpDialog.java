package view;

import javax.swing.*;
import java.awt.*;

public class HelpDialog extends JDialog {
    private JTextArea helpTextArea;

    public HelpDialog(Window owner, String panelName) {
        super(owner, "Help - " + panelName, ModalityType.APPLICATION_MODAL);
        initComponents(panelName);
    }

    private void initComponents(String panelName) {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(getOwner());

        helpTextArea = new JTextArea();
        helpTextArea.setEditable(false);
        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        helpTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        helpTextArea.setText(getHelpText(panelName));
        helpTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(helpTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String getHelpText(String panelName) {
        return switch (panelName) {
            case "Orders" -> """
                Welcome to the Order Management Panel!
                
                This panel helps you manage customer orders:
                • Create new orders by selecting dishes
                • Specify quantities and special instructions
                • Track order status (In Progress, Ready, Served)
                • Calculate total amounts
                • Process payments
                • View order details and history
                """;
                
            case "Payments" -> """
                Welcome to the Payment Processing Panel!
                
                Here you can manage all payment-related tasks:
                • Process payments for orders
                • Handle multiple payment methods
                • View payment status
                • Generate receipts
                • Track transaction history
                • Process refunds when necessary
                """;
                
            case "Ingredients" -> """
                Welcome to the Ingredients Management Panel!
                
                This panel helps you manage your ingredients:
                • View all ingredients in stock
                • Add new ingredients to the system
                • Update ingredient quantities
                • Track expiry dates
                • Monitor low stock levels
                • Manage supplier relationships
                • View ingredient usage history
                """;
                
            case "Inventory" -> """
                Welcome to the Inventory Management Panel!
                
                Here you can track your ingredient stock:
                • Monitor current stock levels
                • View expiring ingredients
                • Track ingredient batches
                • Generate inventory reports
                • Set low stock alerts
                • View stock movement history
                """;
                
            case "Dishes" -> """
                Welcome to the Dish Management Panel!
                
                This panel allows you to manage your menu:
                • View all dishes and their details
                • Add new dishes to the menu
                • Update dish information and prices
                • Manage dish categories
                • Track dish availability
                • View recipe instructions
                • Monitor ingredient requirements
                """;
                
            case "Employees" -> """
                Welcome to the Employee Management Panel!
                
                Here you can manage your staff:
                • View all employees and their roles
                • Add new employees to the system
                • Update employee information
                • Manage shift assignments
                • Track employee performance
                • Handle employee schedules
                • View attendance records
                """;
                
            case "Order History" -> """
                Welcome to the Order History Panel!
                
                This panel shows past orders:
                • View all completed orders
                • Search orders by date or customer
                • Check order details and items
                • Track payment status
                • Generate order reports
                • Export order data
                """;
                
            case "Customers" -> """
                Welcome to the Customer Management Panel!
                
                Here you can manage customer information:
                • View all registered customers
                • Add new customers
                • Update customer details
                • Track order history
                • Manage customer preferences
                • View customer statistics
                """;
                
            case "Suppliers" -> """
                Welcome to the Supplier Management Panel!
                
                This panel helps you manage suppliers:
                • View all active suppliers
                • Add new supplier contacts
                • Update supplier information
                • Track ingredient deliveries
                • Manage supplier relationships
                • Monitor supply quality
                """;
                
            case "Reports" -> """
                Welcome to the Reports Panel!
                
                Generate various reports:
                • Sales and revenue reports
                • Inventory status reports
                • Employee performance reports
                • Customer order statistics
                • Supplier delivery reports
                • Custom report generation
                """;
                
            case "Shift Management" -> """
                Welcome to the Shift Management Panel!
                
                Manage employee shifts:
                • View current shift schedule
                • Assign employees to shifts
                • Handle shift swaps
                • Track break schedules
                • Monitor shift coverage
                • Generate schedule reports
                """;
                
            case "Transactions" -> """
                Welcome to the Transactions Panel!
                
                Track all financial transactions:
                • View daily transactions
                • Process payments and refunds
                • Track payment methods
                • Generate transaction reports
                • Monitor cash flow
                • Export transaction data
                """;
                
            default -> """
                Welcome to the Restaurant Management System!
                
                Please select a panel to view specific help content.
                If you need additional assistance, please contact support.
                """;
        };
    }
} 