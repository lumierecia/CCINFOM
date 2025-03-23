package view;

import javax.swing.*;
import java.awt.*;

public class HelpDialog extends JDialog {
    private final JTabbedPane helpTabs;
    private final String panelName;

    public HelpDialog(Window parent, String panelName) {
        super(parent, "Help - " + panelName, Dialog.ModalityType.APPLICATION_MODAL);
        this.panelName = panelName;
        
        // Set up dialog properties
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Create tabbed pane for help content
        helpTabs = new JTabbedPane();
        helpTabs.setFont(new Font(helpTabs.getFont().getName(), Font.PLAIN, 12));
        
        // Add standard tabs
        addOverviewTab();
        addFeaturesTab();
        addQuickStartTab();
        addTroubleshootingTab();
        
        // Add to dialog
        add(helpTabs, BorderLayout.CENTER);
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addOverviewTab() {
        JTextArea textArea = createHelpTextArea();
        textArea.setText(getOverviewContent());
        helpTabs.addTab("Overview", new JScrollPane(textArea));
    }

    private void addFeaturesTab() {
        JTextArea textArea = createHelpTextArea();
        textArea.setText(getFeaturesContent());
        helpTabs.addTab("Features", new JScrollPane(textArea));
    }

    private void addQuickStartTab() {
        JTextArea textArea = createHelpTextArea();
        textArea.setText(getQuickStartContent());
        helpTabs.addTab("Quick Start", new JScrollPane(textArea));
    }

    private void addTroubleshootingTab() {
        JTextArea textArea = createHelpTextArea();
        textArea.setText(getTroubleshootingContent());
        helpTabs.addTab("Troubleshooting", new JScrollPane(textArea));
    }

    private JTextArea createHelpTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setBackground(new Color(252, 252, 252));
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        return textArea;
    }

    private String getOverviewContent() {
        switch (panelName) {
            case "Transactions":
                return """
                    Welcome to the Transactions Panel!
                    
                    This panel is your central hub for managing orders, payments, and employee shifts.
                    Each tab serves a specific purpose:
                    
                    Orders Tab:
                    • Create and manage customer orders
                    • Add or remove items from orders
                    • Track order status and details
                    
                    Payments Tab:
                    • Process payments for completed orders
                    • View payment history
                    • Handle different payment methods
                    
                    Shifts Tab:
                    • Manage employee work schedules
                    • Assign shifts to employees
                    • View and update shift assignments
                    """;
                    
            case "Orders":
                return """
                    Welcome to the Orders Panel!
                    
                    This panel helps you create and manage customer orders:
                    • Select items from the menu
                    • Specify quantities
                    • Add items to the order
                    • Calculate totals automatically
                    • Process customer information
                    • Track order status
                    """;
                    
            case "Payments":
                return """
                    Welcome to the Payments Panel!
                    
                    This panel helps you process and manage payments:
                    • View unpaid orders
                    • Process payments
                    • Handle different payment methods
                    • Track payment status
                    • Generate receipts
                    • View payment history
                    """;
                    
            case "Shift Management":
                return """
                    Welcome to the Shift Management Panel!
                    
                    This panel helps you manage employee shifts:
                    • Assign shifts to employees
                    • View current shift assignments
                    • Manage shift schedules
                    • Handle shift swaps
                    • Track employee availability
                    • Monitor shift coverage
                    """;
                    
            case "Employees":
                return """
                    Welcome to the Employee Management Panel!
                    
                    This panel helps you manage employee information:
                    • Add new employees
                    • Update employee details
                    • Assign roles and shifts
                    • Track employee status
                    • Manage employee records
                    • Monitor employee assignments
                    """;
                    
            case "Order History":
                return """
                    Welcome to the Order History Panel!
                    
                    This panel helps you track and manage past orders:
                    • View all orders
                    • Check order details
                    • Track order status
                    • Monitor payment status
                    • Review customer history
                    • Generate order reports
                    """;
                    
            default:
                return "Welcome to the " + panelName + " Panel!\n\n" +
                       "This panel provides tools and features to help you manage " +
                       "various aspects of the restaurant system.";
        }
    }

    private String getFeaturesContent() {
        switch (panelName) {
            case "Transactions":
                return """
                    Key Features:
                    
                    1. Order Management
                       • Create new orders
                       • Modify existing orders
                       • Track order status
                       • View order history
                    
                    2. Payment Processing
                       • Accept multiple payment methods
                       • Process refunds
                       • Generate receipts
                       • Track payment status
                    
                    3. Shift Management
                       • Assign employee shifts
                       • View shift schedule
                       • Handle shift swaps
                       • Track attendance
                    """;
                    
            case "Orders":
                return """
                    Key Features:
                    
                    1. Order Creation
                       • Select from menu categories
                       • Choose menu items
                       • Set quantities
                       • Add items to order
                    
                    2. Order Management
                       • View order items
                       • Modify quantities
                       • Remove items
                       • Clear order
                    
                    3. Order Processing
                       • Calculate totals
                       • Handle customer info
                       • Choose order type
                       • Assign employees
                    """;
                    
            case "Payments":
                return """
                    Key Features:
                    
                    1. Payment Processing
                       • View unpaid orders
                       • Process payments
                       • Handle cash/card
                       • Calculate change
                    
                    2. Payment Management
                       • Track payment status
                       • Generate receipts
                       • View payment history
                       • Handle refunds
                    
                    3. Order Details
                       • View order items
                       • Check totals
                       • Verify customer info
                       • Track status
                    """;
                    
            case "Shift Management":
                return """
                    Key Features:
                    
                    1. Shift Assignment
                       • Assign shifts
                       • Remove assignments
                       • Swap shifts
                       • Set schedules
                    
                    2. Schedule Management
                       • View daily schedule
                       • Track coverage
                       • Handle breaks
                       • Monitor attendance
                    
                    3. Employee Management
                       • Check availability
                       • Track assignments
                       • Handle requests
                       • Manage changes
                    """;
                    
            case "Employees":
                return """
                    Key Features:
                    
                    1. Employee Records
                       • Add employees
                       • Update information
                       • Assign roles
                       • Set status
                    
                    2. Shift Management
                       • Assign shifts
                       • View schedules
                       • Track hours
                       • Monitor attendance
                    
                    3. Role Management
                       • Set roles
                       • Define duties
                       • Track assignments
                       • Update changes
                    """;
                    
            case "Order History":
                return """
                    Key Features:
                    
                    1. Order Tracking
                       • View all orders
                       • Search orders
                       • Filter by status
                       • Sort by date
                    
                    2. Order Details
                       • View items
                       • Check totals
                       • Track status
                       • Monitor payments
                    
                    3. Customer History
                       • View past orders
                       • Track preferences
                       • Monitor spending
                       • Check frequency
                    """;
                    
            default:
                return "Features for " + panelName + " Panel\n\n" +
                       "This section lists the key features and capabilities " +
                       "available in this panel.";
        }
    }

    private String getQuickStartContent() {
        switch (panelName) {
            case "Transactions":
                return """
                    Quick Start Guide:
                    
                    1. Creating a New Order
                       a. Click "New Order"
                       b. Select customer or add new
                       c. Add items to order
                       d. Confirm order details
                    
                    2. Processing Payments
                       a. Select unpaid order
                       b. Click "Process Payment"
                       c. Choose payment method
                       d. Complete transaction
                    
                    3. Managing Shifts
                       a. Select date
                       b. Assign employees
                       c. Set shift times
                       d. Save assignments
                    """;
                    
            case "Orders":
                return """
                    Quick Start Guide:
                    
                    1. Creating an Order
                       a. Select category
                       b. Choose product
                       c. Set quantity
                       d. Click "Add to Order"
                    
                    2. Managing Items
                       a. Review order table
                       b. Adjust quantities
                       c. Remove items
                       d. Check total
                    
                    3. Completing Order
                       a. Verify items
                       b. Click "Place Order"
                       c. Enter customer info
                       d. Choose order type
                    """;
                    
            case "Payments":
                return """
                    Quick Start Guide:
                    
                    1. Processing Payment
                       a. Select unpaid order
                       b. Click "Process"
                       c. Choose method
                       d. Complete payment
                    
                    2. Handling Cash
                       a. Enter amount
                       b. Check change
                       c. Confirm payment
                       d. Print receipt
                    
                    3. Card Payments
                       a. Enter card info
                       b. Process transaction
                       c. Get approval
                       d. Print receipt
                    """;
                    
            case "Shift Management":
                return """
                    Quick Start Guide:
                    
                    1. Assigning Shifts
                       a. Click "Assign"
                       b. Select employee
                       c. Choose shift
                       d. Save assignment
                    
                    2. Managing Schedule
                       a. View current shifts
                       b. Check coverage
                       c. Make adjustments
                       d. Update schedule
                    
                    3. Handling Changes
                       a. Select employee
                       b. Modify shift
                       c. Handle swaps
                       d. Update system
                    """;
                    
            case "Employees":
                return """
                    Quick Start Guide:
                    
                    1. Adding Employee
                       a. Click "Add"
                       b. Enter details
                       c. Assign role
                       d. Set shift
                    
                    2. Updating Records
                       a. Select employee
                       b. Click "Edit"
                       c. Make changes
                       d. Save updates
                    
                    3. Managing Status
                       a. Select employee
                       b. Change status
                       c. Update role
                       d. Confirm changes
                    """;
                    
            case "Order History":
                return """
                    Quick Start Guide:
                    
                    1. Viewing Orders
                       a. Browse list
                       b. Use filters
                       c. Sort by date
                       d. Check details
                    
                    2. Order Details
                       a. Select order
                       b. View items
                       c. Check status
                       d. Track payment
                    
                    3. Customer History
                       a. Select customer
                       b. View orders
                       c. Check patterns
                       d. Monitor trends
                    """;
                    
            default:
                return "Quick Start Guide for " + panelName + "\n\n" +
                       "This section provides step-by-step instructions for " +
                       "common tasks in this panel.";
        }
    }

    private String getTroubleshootingContent() {
        switch (panelName) {
            case "Transactions":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Order Not Saving
                       • Check all required fields
                       • Verify customer details
                       • Ensure items are in stock
                    
                    2. Payment Processing Errors
                       • Verify payment amount
                       • Check payment method
                       • Confirm order status
                    
                    3. Shift Assignment Problems
                       • Check employee availability
                       • Verify shift times
                       • Confirm schedule conflicts
                    """;
                    
            case "Orders":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Items Not Adding
                       • Check stock availability
                       • Verify quantity input
                       • Refresh product list
                    
                    2. Order Placement Fails
                       • Verify customer info
                       • Check required fields
                       • Confirm total amount
                    
                    3. Product Issues
                       • Check category selection
                       • Verify product status
                       • Refresh inventory
                    """;
                    
            case "Payments":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Payment Not Processing
                       • Verify order selection
                       • Check payment method
                       • Confirm amount
                    
                    2. Receipt Problems
                       • Check printer connection
                       • Verify order details
                       • Try reprinting
                    
                    3. Status Not Updating
                       • Refresh payment list
                       • Check connection
                       • Verify transaction
                    """;
                    
            case "Shift Management":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Assignment Failures
                       • Check shift availability
                       • Verify employee status
                       • Confirm schedule
                    
                    2. Schedule Conflicts
                       • Check existing shifts
                       • Verify time slots
                       • Review coverage
                    
                    3. Swap Problems
                       • Verify both employees
                       • Check shift compatibility
                       • Confirm availability
                    """;
                    
            case "Employees":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Record Updates Fail
                       • Check required fields
                       • Verify role assignment
                       • Confirm changes
                    
                    2. Shift Assignment Issues
                       • Check availability
                       • Verify schedule
                       • Confirm conflicts
                    
                    3. Status Problems
                       • Verify current status
                       • Check permissions
                       • Confirm changes
                    """;
                    
            case "Order History":
                return """
                    Troubleshooting Guide:
                    
                    Common Issues:
                    
                    1. Orders Not Showing
                       • Refresh the list
                       • Check date range
                       • Verify filters
                    
                    2. Details Not Loading
                       • Check order selection
                       • Verify connection
                       • Try refreshing
                    
                    3. Search Problems
                       • Check search terms
                       • Verify filters
                       • Clear and retry
                    """;
                    
            default:
                return "Troubleshooting Guide for " + panelName + "\n\n" +
                       "This section helps you resolve common issues and " +
                       "problems you might encounter while using this panel.";
        }
    }
} 