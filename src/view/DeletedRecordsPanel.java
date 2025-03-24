package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeletedRecordsPanel extends JPanel {
    private final RestaurantController controller;
    private final JTabbedPane tabbedPane;
    private final MainFrame parentFrame;  // Add reference to parent frame
    
    // Tables for each type of deleted record
    private JTable deletedCustomersTable;
    private JTable deletedOrdersTable;
    private JTable deletedEmployeesTable;
    private JTable deletedSuppliersTable;
    private JTable deletedInventoryTable;

    public DeletedRecordsPanel(RestaurantController controller, MainFrame parentFrame) {
        this.controller = controller;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        
        // Create top panel with back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Main");
        backButton.addActionListener(e -> parentFrame.showMainPanel());
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize tabs
        initCustomersTab();
        initOrdersTab();
        initEmployeesTab();
        initSuppliersTab();
        initInventoryTab();
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add refresh button at the bottom
        JButton refreshAllButton = new JButton("Refresh All");
        refreshAllButton.addActionListener(e -> refreshAllTables());
        add(refreshAllButton, BorderLayout.SOUTH);
    }

    private void initCustomersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"ID", "Name", "Email", "Phone", "Address"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedCustomersTable = new JTable(model);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedCustomersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add restore button
        JButton restoreButton = new JButton("Restore Selected");
        restoreButton.addActionListener(e -> restoreSelectedCustomer());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Customers", panel);
    }

    private void initOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"Order ID", "Customer", "Type", "Status", "Total", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedOrdersTable = new JTable(model);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedOrdersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add restore button
        JButton restoreButton = new JButton("Restore Selected");
        restoreButton.addActionListener(e -> restoreSelectedOrder());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Orders", panel);
    }

    private void initEmployeesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"ID", "Name", "Role", "Shift"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedEmployeesTable = new JTable(model);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedEmployeesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add restore button
        JButton restoreButton = new JButton("Restore Selected");
        restoreButton.addActionListener(e -> restoreSelectedEmployee());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Employees", panel);
    }

    private void initSuppliersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"ID", "Name", "Contact Person", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedSuppliersTable = new JTable(model);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedSuppliersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add restore button
        JButton restoreButton = new JButton("Restore Selected");
        restoreButton.addActionListener(e -> restoreSelectedSupplier());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Suppliers", panel);
    }

    private void initInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"ID", "Name", "Category", "Status", "Last Updated"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deletedInventoryTable = new JTable(model);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedInventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add info label since soft deletion is not supported for inventory
        JLabel infoLabel = new JLabel("Note: Inventory items use direct deletion for better data consistency.");
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(infoLabel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Inventory", panel);
    }

    private void restoreSelectedCustomer() {
        int selectedRow = deletedCustomersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = (int) deletedCustomersTable.getValueAt(selectedRow, 0);
            if (controller.restoreCustomer(customerId)) {
                refreshCustomersTable();
                JOptionPane.showMessageDialog(this, "Customer restored successfully!");
            }
        }
    }

    private void restoreSelectedOrder() {
        int selectedRow = deletedOrdersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) deletedOrdersTable.getValueAt(selectedRow, 0);
            if (controller.restoreOrder(orderId)) {
                refreshOrdersTable();
                JOptionPane.showMessageDialog(this, "Order restored successfully!");
            }
        }
    }

    private void restoreSelectedEmployee() {
        int selectedRow = deletedEmployeesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int employeeId = (int) deletedEmployeesTable.getValueAt(selectedRow, 0);
            if (controller.restoreEmployee(employeeId)) {
                refreshEmployeesTable();
                JOptionPane.showMessageDialog(this, "Employee restored successfully!");
            }
        }
    }

    private void restoreSelectedSupplier() {
        int selectedRow = deletedSuppliersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int supplierId = (int) deletedSuppliersTable.getValueAt(selectedRow, 0);
            if (controller.restoreSupplier(supplierId)) {
                refreshSuppliersTable();
                JOptionPane.showMessageDialog(this, "Supplier restored successfully!");
            }
        }
    }

    /*private void restoreSelectedInventoryItem() {
        // Not implemented since inventory items use direct deletion
    }*/

    public void refreshAllTables() {
        refreshCustomersTable();
        refreshOrdersTable();
        refreshEmployeesTable();
        refreshSuppliersTable();
        refreshInventoryTable();
    }

    private void refreshCustomersTable() {
        DefaultTableModel model = (DefaultTableModel) deletedCustomersTable.getModel();
        model.setRowCount(0);
        List<Customer> deletedCustomers = controller.getDeletedCustomers();
        for (Customer customer : deletedCustomers) {
            model.addRow(new Object[]{
                customer.getCustomerId(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
            });
        }
    }

    private void refreshOrdersTable() {
        DefaultTableModel model = (DefaultTableModel) deletedOrdersTable.getModel();
        model.setRowCount(0);
        List<Order> deletedOrders = controller.getDeletedOrders();
        for (Order order : deletedOrders) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                order.getOrderType(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getOrderDateTime()
            });
        }
    }

    private void refreshEmployeesTable() {
        DefaultTableModel model = (DefaultTableModel) deletedEmployeesTable.getModel();
        model.setRowCount(0);
        List<Employee> deletedEmployees = controller.getDeletedEmployees();
        for (Employee employee : deletedEmployees) {
            model.addRow(new Object[]{
                employee.getEmployeeId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getRoleName(),
                employee.getShiftType()
            });
        }
    }

    private void refreshSuppliersTable() {
        DefaultTableModel model = (DefaultTableModel) deletedSuppliersTable.getModel();
        model.setRowCount(0);
        List<Supplier> deletedSuppliers = controller.getDeletedSuppliers();
        for (Supplier supplier : deletedSuppliers) {
            model.addRow(new Object[]{
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone()
            });
        }
    }

    private void refreshInventoryTable() {
        DefaultTableModel model = (DefaultTableModel) deletedInventoryTable.getModel();
        model.setRowCount(0);
        List<Inventory> deletedItems = controller.getDeletedInventoryItems();
        for (Inventory item : deletedItems) {
            model.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getCategoryName(),
                item.getStatus(),
                item.getLastUpdated()
            });
        }
    }
}