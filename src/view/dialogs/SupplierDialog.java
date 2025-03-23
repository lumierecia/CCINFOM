package view.dialogs;

import controller.RestaurantController;
import model.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SupplierDialog extends JDialog {
    private final RestaurantController controller;
    private JTable supplierTable;
    private DefaultTableModel tableModel;

    public SupplierDialog(Frame owner, RestaurantController controller) {
        super(owner, "Supplier Management", true);
        this.controller = controller;
        initComponents();
        loadSuppliers();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(800, 400));

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton addButton = new JButton("Add Supplier");
        JButton editButton = new JButton("Edit Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton refreshButton = new JButton("Refresh");
        
        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.addSeparator();
        toolbar.add(refreshButton);
        
        add(toolbar, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"ID", "Name", "Contact Person", "Phone", "Email", "Address", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        supplierTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        supplierTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        supplierTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        supplierTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        supplierTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        supplierTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        addButton.addActionListener(e -> addSupplier());
        editButton.addActionListener(e -> editSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        refreshButton.addActionListener(e -> loadSuppliers());

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void loadSuppliers() {
        tableModel.setRowCount(0);
        try {
            List<Supplier> suppliers = controller.getAllSuppliers();
            for (Supplier supplier : suppliers) {
                Object[] row = {
                    supplier.getSupplierId(),
                    supplier.getName(),
                    supplier.getContactPerson(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getAddress(),
                    supplier.isDeleted() ? "Inactive" : "Active"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to load suppliers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSupplier() {
        AddSupplierDialog dialog = new AddSupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), controller);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Supplier supplier = dialog.getSupplier();
            try {
                controller.addSupplier(supplier);
                loadSuppliers();
                JOptionPane.showMessageDialog(this,
                    "Supplier added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Failed to add supplier: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplierId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String contactPerson = (String) tableModel.getValueAt(selectedRow, 2);
        String phone = (String) tableModel.getValueAt(selectedRow, 3);
        String email = (String) tableModel.getValueAt(selectedRow, 4);
        String address = (String) tableModel.getValueAt(selectedRow, 5);
        boolean isDeleted = tableModel.getValueAt(selectedRow, 6).equals("Inactive");

        Supplier supplier = new Supplier();
        supplier.setSupplierId(supplierId);
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        supplier.setDeleted(isDeleted);

        EditSupplierDialog dialog = new EditSupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), controller, supplier);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Supplier updatedSupplier = dialog.getUpdatedSupplier();
            try {
                controller.updateSupplier(updatedSupplier);
                loadSuppliers();
                JOptionPane.showMessageDialog(this,
                    "Supplier updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Failed to update supplier: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplierId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete supplier '" + supplierName + "'?\n" +
            "This will only mark the supplier as inactive.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteSupplier(supplierId);
                loadSuppliers();
                JOptionPane.showMessageDialog(this,
                    "Supplier deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Failed to delete supplier: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 