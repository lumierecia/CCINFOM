package view;

import controller.RestaurantController;
import model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeePanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton assignShiftButton;

    public EmployeePanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadEmployees();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Employee");
        editButton = new JButton("Edit Employee");
        deleteButton = new JButton("Delete Employee");
        assignShiftButton = new JButton("Assign Shift");
        refreshButton = new JButton("Refresh");

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(assignShiftButton);
        toolBar.add(refreshButton);

        // Create table
        String[] columnNames = {"ID", "First Name", "Last Name", "Role", "Phone", "Status", "Shift Type", "Shift Start", "Shift End"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        addButton.addActionListener(e -> showEmployeeDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow != -1) {
                int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
                Employee employee = controller.getEmployeeById(employeeId);
                if (employee != null) {
                    showEmployeeDialog(employee);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an employee to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow != -1) {
                int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this employee?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.deleteEmployee(employeeId)) {
                        loadEmployees();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete employee.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an employee to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        assignShiftButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow != -1) {
                int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
                showShiftAssignmentDialog(employeeId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an employee to assign a shift.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        refreshButton.addActionListener(e -> loadEmployees());
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        List<Employee> employees = controller.getAllEmployees();
        for (Employee employee : employees) {
            Object[] row = {
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole(),
                employee.getPhone(),
                employee.getStatus(),
                employee.getShiftType(),
                employee.getShiftStart(),
                employee.getShiftEnd()
            };
            tableModel.addRow(row);
        }
    }

    private void showEmployeeDialog(Employee employee) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            employee == null ? "Add Employee" : "Edit Employee",
            true);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        String[] roles = {"Manager", "Chef", "Waiter", "Cashier", "Cleaner"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        String[] statuses = {"Active", "Inactive"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);

        // If editing, populate fields
        if (employee != null) {
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            phoneField.setText(employee.getPhone());
            roleCombo.setSelectedItem(employee.getRole());
            statusCombo.setSelectedItem(employee.getStatus());
        }

        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        formPanel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusCombo, gbc);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton(employee == null ? "Add" : "Save");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners
        saveButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill in all required fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Employee newEmployee = new Employee(
                employee != null ? employee.getEmployeeId() : 0,
                firstName,
                lastName,
                role,
                phone,
                status
            );

            boolean success;
            if (employee == null) {
                success = controller.addEmployee(newEmployee);
            } else {
                success = controller.updateEmployee(newEmployee);
            }

            if (success) {
                loadEmployees();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to " + (employee == null ? "add" : "update") + " employee.",
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

    private void showShiftAssignmentDialog(int employeeId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Assign Shift",
            true);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        String[] shifts = {"Morning", "Afternoon", "Night"};
        JComboBox<String> shiftCombo = new JComboBox<>(shifts);

        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Shift:"), gbc);
        gbc.gridx = 1;
        formPanel.add(shiftCombo, gbc);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton assignButton = new JButton("Assign");
        JButton removeButton = new JButton("Remove Shift");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(assignButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners
        assignButton.addActionListener(e -> {
            String shiftType = (String) shiftCombo.getSelectedItem();
            int timeShiftId = getTimeShiftId(shiftType);
            if (timeShiftId != -1 && controller.assignShift(employeeId, timeShiftId)) {
                loadEmployees();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to assign shift.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        removeButton.addActionListener(e -> {
            if (controller.removeShift(employeeId)) {
                loadEmployees();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to remove shift.",
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

    private int getTimeShiftId(String shiftType) {
        switch (shiftType) {
            case "Morning": return 1;
            case "Afternoon": return 2;
            case "Night": return 3;
            default: return -1;
        }
    }
} 