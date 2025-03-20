package view;

import controller.RestaurantController;
import model.Supplier;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierPanel extends JPanel {
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewIngredientsButton;
    private JButton refreshButton;

    public SupplierPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadSuppliers();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Supplier");
        editButton = new JButton("Edit Supplier");
        deleteButton = new JButton("Delete Supplier");
        viewIngredientsButton = new JButton("View Ingredients");
        refreshButton = new JButton("Refresh");

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(viewIngredientsButton);
        toolBar.add(refreshButton);

        // Create table
        String[] columnNames = {"ID", "Name", "Contact Person", "Email", "Phone", "Address", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(supplierTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        addButton.addActionListener(e -> showSupplierDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = supplierTable.getSelectedRow();
            if (selectedRow != -1) {
                int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
                Supplier supplier = controller.getSupplierById(supplierId);
                if (supplier != null) {
                    showSupplierDialog(supplier);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a supplier to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = supplierTable.getSelectedRow();
            if (selectedRow != -1) {
                int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this supplier?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.deleteSupplier(supplierId)) {
                        loadSuppliers();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete supplier.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a supplier to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        viewIngredientsButton.addActionListener(e -> {
            int selectedRow = supplierTable.getSelectedRow();
            if (selectedRow != -1) {
                int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
                showSupplierIngredients(supplierId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a supplier to view ingredients.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        refreshButton.addActionListener(e -> loadSuppliers());
    }

    private void loadSuppliers() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = controller.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            Object[] row = {
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void showSupplierDialog(Supplier supplier) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            supplier == null ? "Add Supplier" : "Edit Supplier",
            true);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField nameField = new JTextField(20);
        JTextField contactPersonField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        String[] statuses = {"Active", "Inactive"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);

        // If editing, populate fields
        if (supplier != null) {
            nameField.setText(supplier.getName());
            contactPersonField.setText(supplier.getContactPerson());
            emailField.setText(supplier.getEmail());
            phoneField.setText(supplier.getPhone());
            addressField.setText(supplier.getAddress());
            statusCombo.setSelectedItem(supplier.getStatus());
        }

        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 1;
        formPanel.add(contactPersonField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(addressField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusCombo, gbc);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton(supplier == null ? "Add" : "Save");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (name.isEmpty() || contactPerson.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill in all required fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Supplier newSupplier = new Supplier(
                supplier != null ? supplier.getSupplierId() : 0,
                name,
                contactPerson,
                email,
                phone,
                address,
                status
            );

            boolean success;
            if (supplier == null) {
                success = controller.addSupplier(newSupplier);
            } else {
                success = controller.updateSupplier(newSupplier);
            }

            if (success) {
                loadSuppliers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to " + (supplier == null ? "add" : "update") + " supplier.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSupplierIngredients(int supplierId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Supplier Ingredients", true);
        dialog.setLayout(new BorderLayout());

        // Create table
        String[] columnNames = {"ID", "Name", "Category", "Quantity", "Unit Price"};
        DefaultTableModel productsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable productsTable = new JTable(productsTableModel);
        JScrollPane scrollPane = new JScrollPane(productsTable);

        // Load products
        List<Inventory> products = controller.getSupplierIngredients(supplierId);
        for (Inventory product : products) {
            Object[] row = {
                product.getProductId(),
                product.getProductName(),
                product.getCategoryId(),
                product.getQuantity(),
                product.getSellPrice()
            };
            productsTableModel.addRow(row);
        }

        // Add components to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 