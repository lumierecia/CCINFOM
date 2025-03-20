package view;

import controller.RestaurantController;
import model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShiftManagementPanel extends JPanel {
    private final RestaurantController controller;
    private JTable shiftsTable;
    private DefaultTableModel tableModel;
    private JComboBox<Employee> employeeCombo;
    private JComboBox<String> shiftTypeCombo;
    private JTextField dateField;

    public ShiftManagementPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadShifts();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Employee selection
        toolBar.add(new JLabel("Employee:"));
        employeeCombo = new JComboBox<>(controller.getAllEmployees().toArray(new Employee[0]));
        toolBar.add(employeeCombo);

        // Shift type selection
        toolBar.add(new JLabel("Shift Type:"));
        String[] shiftTypes = {"Morning", "Afternoon", "Night"};
        shiftTypeCombo = new JComboBox<>(shiftTypes);
        toolBar.add(shiftTypeCombo);

        // Date field
        toolBar.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        toolBar.add(dateField);

        // Buttons
        JButton assignButton = new JButton("Assign Shift");
        assignButton.addActionListener(e -> assignShift());
        JButton removeButton = new JButton("Remove Shift");
        removeButton.addActionListener(e -> removeShift());

        toolBar.add(assignButton);
        toolBar.add(removeButton);
        add(toolBar, BorderLayout.NORTH);

        // Create shifts table
        String[] columns = {"Employee", "Shift Type", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        shiftsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(shiftsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadShifts() {
        tableModel.setRowCount(0);
        List<Employee> employees = controller.getAllEmployees();
        
        for (Employee employee : employees) {
            String currentShift = controller.getCurrentShift(employee.getEmployeeId());
            if (currentShift != null) {
                Object[] row = {
                    employee.getFirstName() + " " + employee.getLastName(),
                    currentShift,
                    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    "Active"
                };
                tableModel.addRow(row);
            }
        }
    }

    private void assignShift() {
        Employee employee = (Employee) employeeCombo.getSelectedItem();
        if (employee == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee.",
                "No Employee Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String shiftType = (String) shiftTypeCombo.getSelectedItem();
        int timeShiftId = getTimeShiftId(shiftType);

        if (controller.assignShift(employee.getEmployeeId(), timeShiftId)) {
            JOptionPane.showMessageDialog(this,
                "Shift assigned successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadShifts();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to assign shift.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getTimeShiftId(String shiftType) {
        return switch (shiftType) {
            case "Morning" -> 1;
            case "Afternoon" -> 2;
            case "Night" -> 3;
            default -> -1;
        };
    }

    private void removeShift() {
        int selectedRow = shiftsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a shift to remove.",
                "No Shift Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeName = (String) tableModel.getValueAt(selectedRow, 0);
        List<Employee> employees = controller.getAllEmployees();
        Employee employee = employees.stream()
            .filter(e -> (e.getFirstName() + " " + e.getLastName()).equals(employeeName))
            .findFirst()
            .orElse(null);

        if (employee != null) {
            if (controller.removeShift(employee.getEmployeeId())) {
                JOptionPane.showMessageDialog(this,
                    "Shift removed successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadShifts();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to remove shift.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 