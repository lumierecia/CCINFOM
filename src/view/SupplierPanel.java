package view;

import controller.RestaurantController;
import model.Supplier;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class SupplierPanel extends JPanel {
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewIngredientsButton;
    private JButton refreshButton;
    private JButton helpButton;
    private List<Integer> supplierIds = new ArrayList<>();

    public SupplierPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadSuppliers();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = createStyledButton("Add Supplier", new Color(40, 167, 69));
        editButton = createStyledButton("Edit Supplier", new Color(255, 193, 7));
        deleteButton = createStyledButton("Delete Supplier", new Color(220, 53, 69));
        viewIngredientsButton = createStyledButton("View Ingredients", new Color(70, 130, 180));
        refreshButton = createStyledButton("Refresh", new Color(108, 117, 125));
        helpButton = createStyledButton("Help", new Color(23, 162, 184));

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(viewIngredientsButton);
        toolBar.add(refreshButton);
        toolBar.add(helpButton);

        // Create table
        String[] columnNames = {"Name", "Contact Person", "Email", "Phone", "Address", "Status"};
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
                int supplierId = supplierIds.get(selectedRow);
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
                int supplierId = supplierIds.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this supplier?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteSupplier(supplierId);
                    loadSuppliers();
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
                int supplierId = supplierIds.get(selectedRow);
                showSupplierIngredients(supplierId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a supplier to view ingredients.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        refreshButton.addActionListener(e -> loadSuppliers());
        helpButton.addActionListener(e -> showHelpDialog());
    }

    private void loadSuppliers() {
        tableModel.setRowCount(0);
        supplierIds.clear();
        List<Supplier> suppliers = controller.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierIds.add(supplier.getSupplierId());
            Object[] row = {
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.isDeleted() ? "Inactive" : "Active"
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
            statusCombo.setSelectedItem(supplier.isDeleted() ? "Inactive" : "Active");
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

            Supplier newSupplier = new Supplier();
            if (supplier != null) {
                newSupplier.setSupplierId(supplier.getSupplierId());
            }
            newSupplier.setName(name);
            newSupplier.setContactPerson(contactPerson);
            newSupplier.setEmail(email);
            newSupplier.setPhone(phone);
            newSupplier.setAddress(address);
            newSupplier.setDeleted(status.equals("Inactive"));

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
        String[] columnNames = {"Name", "Category", "Quantity", "Unit Price"};
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

    private void showHelpDialog() {
        String helpText = """
            Supplier Management Help
            
            Overview:
            The supplier management system allows you to manage your restaurant's suppliers and their associated ingredients.
            
            Features:
            1. Add Supplier: Create new supplier entries with contact details
            2. Edit Supplier: Modify existing supplier information
            3. Delete Supplier: Remove suppliers no longer in use
            4. View Ingredients: See all ingredients supplied by a specific supplier
            5. Refresh: Update the supplier list to show latest changes
            
            Tips:
            • Keep supplier contact information up to date
            • Regularly review supplier status
            • Check ingredient associations before deleting suppliers
            • Use the refresh button after making changes
            
            Note: Deleting a supplier will not remove their historical records
            but will mark them as inactive in the system.
            """;
        
        JDialog helpDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Supplier Help", true);
        helpDialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        helpDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> helpDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        helpDialog.setSize(500, 400);
        helpDialog.setLocationRelativeTo(this);
        helpDialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }
} 