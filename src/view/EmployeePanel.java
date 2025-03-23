package view;

import controller.RestaurantController;
import model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.ArrayList;

public class EmployeePanel extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton helpButton;
    private List<Integer> employeeIds = new ArrayList<>();

    public EmployeePanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadEmployees();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = createStyledButton("Add Employee", new Color(40, 167, 69));
        editButton = createStyledButton("Edit Employee", new Color(255, 193, 7));
        deleteButton = createStyledButton("Delete Employee", new Color(220, 53, 69));
        refreshButton = createStyledButton("Refresh", new Color(108, 117, 125));
        helpButton = createStyledButton("Help", new Color(23, 162, 184));

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);
        toolBar.add(helpButton);

        // Create table
        String[] columnNames = {"First Name", "Last Name", "Role", "Status", "Shift Type", "Shift Start", "Shift End"};
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
                int employeeId = employeeIds.get(selectedRow);
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
                int employeeId = employeeIds.get(selectedRow);
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
        refreshButton.addActionListener(e -> loadEmployees());
        helpButton.addActionListener(e -> showHelpDialog());
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        employeeIds.clear();
        List<Employee> employees = controller.getAllEmployees();
        for (Employee employee : employees) {
            employeeIds.add(employee.getEmployeeId());
            Object[] row = {
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole(),
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
        String[] roles = {"Manager", "Chef", "Waiter", "Cashier", "Cleaner"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        String[] statuses = {"Active", "Inactive"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        String[] shifts = {"None", "Morning", "Afternoon", "Night"};
        JComboBox<String> shiftCombo = new JComboBox<>(shifts);

        // If editing, populate fields
        if (employee != null) {
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            roleCombo.setSelectedItem(employee.getRole());
            statusCombo.setSelectedItem(employee.getStatus());
            shiftCombo.setSelectedItem(employee.getShiftType() != null ? employee.getShiftType() : "None");
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
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        formPanel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusCombo, gbc);

        // Add separator
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // Add shift selection
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Shift:"), gbc);
        gbc.gridx = 1;
        formPanel.add(shiftCombo, gbc);

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
            String role = (String) roleCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            String shiftType = (String) shiftCombo.getSelectedItem();

            if (firstName.isEmpty() || lastName.isEmpty()) {
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
                status
            );

            boolean success;
            if (employee == null) {
                success = controller.addEmployee(newEmployee);
                if (success && !shiftType.equals("None")) {
                    int employeeId = controller.getEmployeeByName(firstName, lastName).getEmployeeId();
                    success = controller.assignShift(employeeId, getTimeShiftId(shiftType));
                }
            } else {
                success = controller.updateEmployee(newEmployee);
                if (success) {
                    if (shiftType.equals("None")) {
                        success = controller.removeShift(employee.getEmployeeId());
                    } else {
                        success = controller.assignShift(employee.getEmployeeId(), getTimeShiftId(shiftType));
                    }
                }
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

    private void showHelpDialog() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Employees");
        helpDialog.setVisible(true);
    }

    private int getTimeShiftId(String shiftType) {
        return switch (shiftType) {
            case "Morning" -> 1;
            case "Afternoon" -> 2;
            case "Night" -> 3;
            default -> -1;
        };
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
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

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