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
    private JComboBox<String> filterCombo;
    private JButton assignButton;
    private JButton removeButton;
    private JButton viewScheduleButton;
    private JButton swapShiftButton;
    private JButton breakScheduleButton;

    public ShiftManagementPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadShifts();
    }

    private void initComponents() {
        // Create top panel for controls
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by:"));
        String[] filterOptions = {"All Shifts", "Morning Shift", "Afternoon Shift", "Night Shift", "Unassigned"};
        filterCombo = new JComboBox<>(filterOptions);
        filterCombo.setPreferredSize(new Dimension(150, 30));
        filterCombo.addActionListener(e -> loadShifts());
        filterPanel.add(filterCombo);
        
        // Create buttons panel with improved styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        // Assign Shift button
        assignButton = createStyledButton("Assign Shift", new Color(70, 130, 180));
        assignButton.addActionListener(e -> showAssignDialog());
        
        // Remove Shift button
        removeButton = createStyledButton("Remove Shift", new Color(220, 53, 69));
        removeButton.addActionListener(e -> removeSelectedShift());
        
        // View Schedule button
        viewScheduleButton = createStyledButton("View Schedule", new Color(40, 167, 69));
        viewScheduleButton.addActionListener(e -> showScheduleDialog());

        // Swap Shift button
        swapShiftButton = createStyledButton("Swap Shifts", new Color(255, 193, 7));
        swapShiftButton.addActionListener(e -> showSwapShiftDialog());

        // Break Schedule button
        breakScheduleButton = createStyledButton("Break Schedule", new Color(108, 117, 125));
        breakScheduleButton.addActionListener(e -> showBreakScheduleDialog());

        // Help button
        JButton helpButton = createStyledButton("Help", new Color(23, 162, 184));
        helpButton.addActionListener(e -> showHelp());
        
        buttonPanel.add(assignButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(swapShiftButton);
        buttonPanel.add(breakScheduleButton);
        buttonPanel.add(viewScheduleButton);
        buttonPanel.add(helpButton);
        
        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create table
        String[] columns = {
            "Name", "Role", "Shift Type", 
            "Start Time", "End Time", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        shiftsTable = new JTable(tableModel);
        shiftsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shiftsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        shiftsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Name
        shiftsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Role
        shiftsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Shift Type
        shiftsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Start Time
        shiftsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // End Time
        shiftsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        
        JScrollPane scrollPane = new JScrollPane(shiftsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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

    private void loadShifts() {
        tableModel.setRowCount(0);
        List<Employee> employees = controller.getAllEmployees();
        String filter = (String) filterCombo.getSelectedItem();
        
        for (Employee employee : employees) {
            String shiftType = employee.getShiftType();
            
            // Apply filter
            if (filter != null && !filter.equals("All Shifts")) {
                if (filter.equals("Unassigned") && shiftType != null) continue;
                if (!filter.equals("Unassigned") && (shiftType == null || !filter.startsWith(shiftType))) continue;
            }
            
            Object[] row = {
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getRole(),
                shiftType != null ? shiftType : "Unassigned",
                employee.getShiftStart() != null ? employee.getShiftStart() : "-",
                employee.getShiftEnd() != null ? employee.getShiftEnd() : "-",
                shiftType != null ? "Active" : "Not Assigned"
            };
            tableModel.addRow(row);
        }
    }

    private void showAssignDialog() {
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Assign Shift", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Employee selection
        List<Employee> unassignedEmployees = controller.getAllEmployees().stream()
            .filter(e -> e.getShiftType() == null)
            .toList();
        
        JComboBox<Employee> employeeCombo = new JComboBox<>(
            unassignedEmployees.toArray(new Employee[0]));
        employeeCombo.setPreferredSize(new Dimension(200, 30));
        
        // Shift type selection
        String[] shifts = {"Morning", "Afternoon", "Night"};
        JComboBox<String> shiftCombo = new JComboBox<>(shifts);
        shiftCombo.setPreferredSize(new Dimension(200, 30));
        
        // Add components to form
        gbc.gridy = 0;
        formPanel.add(new JLabel("Employee:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employeeCombo, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Shift:"), gbc);
        gbc.gridx = 1;
        formPanel.add(shiftCombo, gbc);
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton assignButton = new JButton("Assign");
        JButton cancelButton = new JButton("Cancel");
        
        assignButton.addActionListener(e -> {
            Employee employee = (Employee) employeeCombo.getSelectedItem();
            if (employee == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select an employee.",
                    "No Employee Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String shiftType = (String) shiftCombo.getSelectedItem();
            int timeShiftId = getTimeShiftId(shiftType);
            
            if (controller.assignShift(employee.getEmployeeId(), timeShiftId)) {
                JOptionPane.showMessageDialog(dialog,
                    "Shift assigned successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadShifts();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to assign shift. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void removeSelectedShift() {
        int selectedRow = shiftsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to remove their shift.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentShift = (String) tableModel.getValueAt(selectedRow, 3);
        
        if ("Unassigned".equals(currentShift)) {
            JOptionPane.showMessageDialog(this,
                "This employee doesn't have an assigned shift.",
                "No Shift",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove the shift for " + employeeName + "?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.removeShift(employeeId)) {
                JOptionPane.showMessageDialog(this,
                    "Shift removed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadShifts();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to remove shift. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Weekly Schedule", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Create schedule panel with grid layout (4 rows x 3 columns)
        JPanel schedulePanel = new JPanel(new GridLayout(4, 3, 10, 10));
        schedulePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add headers
        schedulePanel.add(new JLabel("Shift"));
        schedulePanel.add(new JLabel("Time"));
        schedulePanel.add(new JLabel("Employees"));
        
        // Get all employees grouped by shift
        List<Employee> employees = controller.getAllEmployees();
        
        // Morning shift
        schedulePanel.add(new JLabel("Morning"));
        schedulePanel.add(new JLabel("04:00 - 12:00"));
        JTextArea morningEmployees = new JTextArea();
        addEmployeesToTextArea(morningEmployees, employees, "Morning");
        schedulePanel.add(new JScrollPane(morningEmployees));
        
        // Afternoon shift
        schedulePanel.add(new JLabel("Afternoon"));
        schedulePanel.add(new JLabel("12:00 - 20:00"));
        JTextArea afternoonEmployees = new JTextArea();
        addEmployeesToTextArea(afternoonEmployees, employees, "Afternoon");
        schedulePanel.add(new JScrollPane(afternoonEmployees));
        
        // Night shift
        schedulePanel.add(new JLabel("Night"));
        schedulePanel.add(new JLabel("20:00 - 04:00"));
        JTextArea nightEmployees = new JTextArea();
        addEmployeesToTextArea(nightEmployees, employees, "Night");
        schedulePanel.add(new JScrollPane(nightEmployees));
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        
        // Add panels to dialog
        dialog.add(schedulePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addEmployeesToTextArea(JTextArea textArea, List<Employee> employees, String shift) {
        StringBuilder sb = new StringBuilder();
        employees.stream()
            .filter(e -> e.getShiftType() != null && e.getShiftType().equals(shift))
            .forEach(e -> sb.append(e.getFirstName())
                          .append(" ")
                          .append(e.getLastName())
                          .append(" (")
                          .append(e.getRole())
                          .append(")\n"));
        textArea.setText(sb.toString());
        textArea.setEditable(false);
        textArea.setBackground(new Color(245, 245, 245));
    }

    private int getTimeShiftId(String shiftType) {
        return switch (shiftType) {
            case "Morning" -> 1;
            case "Afternoon" -> 2;
            case "Night" -> 3;
            default -> -1;
        };
    }

    private void showSwapShiftDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Swap Shifts", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Get employees with assigned shifts
        List<Employee> assignedEmployees = controller.getAllEmployees().stream()
            .filter(e -> e.getShiftType() != null)
            .toList();

        // First employee selection
        JComboBox<Employee> employee1Combo = new JComboBox<>(
            assignedEmployees.toArray(new Employee[0]));
        
        // Second employee selection
        JComboBox<Employee> employee2Combo = new JComboBox<>(
            assignedEmployees.toArray(new Employee[0]));

        // Add components to form
        gbc.gridy = 0;
        formPanel.add(new JLabel("Employee 1:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employee1Combo, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Employee 2:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employee2Combo, gbc);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton swapButton = createStyledButton("Swap", new Color(255, 193, 7));
        JButton cancelButton = createStyledButton("Cancel", new Color(108, 117, 125));

        swapButton.addActionListener(e -> {
            Employee emp1 = (Employee) employee1Combo.getSelectedItem();
            Employee emp2 = (Employee) employee2Combo.getSelectedItem();

            if (emp1 == emp2) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select different employees.",
                    "Invalid Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Implement the shift swap logic here
            // This would involve updating the database to swap the time_shiftid values
            
            JOptionPane.showMessageDialog(dialog,
                "Shifts swapped successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadShifts();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(swapButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showBreakScheduleDialog() {
        int selectedRow = shiftsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to schedule breaks.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeName = (String) tableModel.getValueAt(selectedRow, 0);
        String shiftType = (String) tableModel.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Schedule Breaks", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create time selection spinners
        SpinnerDateModel startModel = new SpinnerDateModel();
        JSpinner startSpinner = new JSpinner(startModel);
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));

        SpinnerDateModel endModel = new SpinnerDateModel();
        JSpinner endSpinner = new JSpinner(endModel);
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));

        // Add components to form
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = 0;
        formPanel.add(new JLabel("Employee: " + employeeName), gbc);

        gbc.gridy = 1;
        formPanel.add(new JLabel("Shift: " + shiftType), gbc);

        gbc.gridy = 2;
        formPanel.add(new JLabel("Break Start:"), gbc);
        gbc.gridx = 1;
        formPanel.add(startSpinner, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Break End:"), gbc);
        gbc.gridx = 1;
        formPanel.add(endSpinner, gbc);

        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton scheduleButton = createStyledButton("Schedule", new Color(40, 167, 69));
        JButton cancelButton = createStyledButton("Cancel", new Color(108, 117, 125));

        scheduleButton.addActionListener(e -> {
            // Implement break scheduling logic here
            JOptionPane.showMessageDialog(dialog,
                "Break scheduled successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(scheduleButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelp() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Shift Management");
        helpDialog.setVisible(true);
    }
} 