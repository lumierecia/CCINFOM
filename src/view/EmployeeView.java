package view;

import controller.EmployeeController;
import model.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeView extends JFrame {
    private EmployeeController employeeController;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> roleComboBox;
    private JComboBox<String> shiftComboBox;

    public EmployeeView() {
        this.employeeController = new EmployeeController();
        initializeUI();
        loadEmployees();
    }

    private void initializeUI() {
        setTitle("Employee Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // First Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        formPanel.add(firstNameField, gbc);

        // Last Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        formPanel.add(lastNameField, gbc);

        // Role ComboBox
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Role:"), gbc);
        
        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"Waiter", "Chef", "Cleaner", "Manager", "Cashier"});
        formPanel.add(roleComboBox, gbc);

        // Shift ComboBox
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Shift:"), gbc);
        
        gbc.gridx = 1;
        shiftComboBox = new JComboBox<>(new String[]{"Morning", "Afternoon", "Night"});
        formPanel.add(shiftComboBox, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Employee");
        JButton updateButton = new JButton("Update Employee");
        JButton deleteButton = new JButton("Delete Employee");
        
        addButton.addActionListener(e -> handleAddEmployee());
        updateButton.addActionListener(e -> handleUpdateEmployee());
        deleteButton.addActionListener(e -> handleDeleteEmployee());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add form and buttons to main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Create table
        String[] columns = {"ID", "First Name", "Last Name", "Role", "Shift", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Add selection listener to table
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    firstNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                    lastNameField.setText((String) tableModel.getValueAt(selectedRow, 2));
                    roleComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
                    shiftComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 4));
                }
            }
        });

        add(mainPanel);
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        List<Employee> employees = employeeController.getAllEmployees();
        
        for (Employee employee : employees) {
            Object[] row = {
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRoleName(),
                employee.getShiftType(),
                employeeController.isEmployeeOnShift(employee.getEmployeeId()) ? "On Shift" : "Off Shift"
            };
            tableModel.addRow(row);
        }
    }

    private void handleAddEmployee() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String role = (String) roleComboBox.getSelectedItem();
        String shift = (String) shiftComboBox.getSelectedItem();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roleId = roleComboBox.getSelectedIndex() + 1;
        int shiftId = shiftComboBox.getSelectedIndex() + 1;

        if (employeeController.addEmployee(firstName, lastName, roleId, shiftId)) {
            JOptionPane.showMessageDialog(this, "Employee added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add employee", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        int roleId = roleComboBox.getSelectedIndex() + 1;
        int shiftId = shiftComboBox.getSelectedIndex() + 1;

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (employeeController.updateEmployee(employeeId, firstName, lastName, roleId, shiftId)) {
            JOptionPane.showMessageDialog(this, "Employee updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update employee", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this employee?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (employeeController.deleteEmployee(employeeId)) {
                JOptionPane.showMessageDialog(this, "Employee deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadEmployees();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete employee", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        roleComboBox.setSelectedIndex(0);
        shiftComboBox.setSelectedIndex(0);
    }
} 