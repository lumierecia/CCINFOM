package view;

import controller.RestaurantController;
import model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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
        
        // Add selection listener to enable/disable buttons
        shiftsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = shiftsTable.getSelectedRow() != -1;
                removeButton.setEnabled(hasSelection);
                swapShiftButton.setEnabled(hasSelection);
            }
        });
        
        // Set initial button states
        removeButton.setEnabled(false);
        swapShiftButton.setEnabled(false);
        
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
            if (employee.isDeleted()) continue;
            
            String shiftType = employee.getShiftType();
            if (shiftType == null) shiftType = "Unassigned";
            
            // Apply filter
            if (!"All Shifts".equals(filter)) {
                if ("Unassigned".equals(filter) && !"Unassigned".equals(shiftType)) continue;
                if (!"Unassigned".equals(filter) && !filter.equals(shiftType + " Shift")) continue;
            }
            
            String[] timeRange = getShiftTimeRange(shiftType);
            String status = getShiftStatus(employee);
            
            tableModel.addRow(new Object[]{
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getRoleName(),
                shiftType,
                timeRange[0],
                timeRange[1],
                status
            });
        }
    }

    private String[] getShiftTimeRange(String shiftType) {
        switch (shiftType) {
            case "Morning":
                return new String[]{"06:00 AM", "02:00 PM"};
            case "Afternoon":
                return new String[]{"02:00 PM", "10:00 PM"};
            case "Night":
                return new String[]{"10:00 PM", "06:00 AM"};
            default:
                return new String[]{"--:--", "--:--"};
        }
    }
    
    private String getShiftStatus(Employee employee) {
        if (employee.getShiftType() == null) return "Unassigned";
        
        // Get count of employees in same shift
        long shiftCount = controller.getAllEmployees().stream()
            .filter(e -> !e.isDeleted() && 
                   employee.getShiftType().equals(e.getShiftType()))
            .count();
            
        return shiftCount > 5L ? "Overstaffed" : "Active";
    }

    private void showAssignDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Assign Shift", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(400, 200));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Get unassigned employees
        List<Employee> unassignedEmployees = controller.getAllEmployees().stream()
            .filter(e -> !e.isDeleted() && e.getShiftType() == null)
            .toList();
            
        if (unassignedEmployees.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No unassigned employees available.",
                "No Employees",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Employee selection
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

    private int getTimeShiftId(String shiftType) {
        return switch (shiftType) {
            case "Morning" -> 1;
            case "Afternoon" -> 2;
            case "Night" -> 3;
            default -> -1;
        };
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
        
        String employeeName = (String) tableModel.getValueAt(selectedRow, 0);
        String currentShift = (String) tableModel.getValueAt(selectedRow, 2); // Column index 2 is shift type
        
        if ("Unassigned".equals(currentShift)) {
            JOptionPane.showMessageDialog(this,
                "This employee doesn't have an assigned shift.",
                "No Shift",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Find employee ID from the name
        String[] nameParts = employeeName.split(" ");
        Employee employee = controller.getAllEmployees().stream()
            .filter(e -> e.getFirstName().equals(nameParts[0]) && e.getLastName().equals(nameParts[1]))
            .findFirst()
            .orElse(null);
            
        if (employee == null) {
            JOptionPane.showMessageDialog(this,
                "Could not find employee information.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove the shift for " + employeeName + "?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.removeShift(employee.getEmployeeId())) {
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

    private void showSwapShiftDialog() {
        int selectedRow = shiftsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to swap shifts with.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get selected employee info
        String selectedName = (String) tableModel.getValueAt(selectedRow, 0);
        String currentShift = (String) tableModel.getValueAt(selectedRow, 2);
        
        if ("Unassigned".equals(currentShift)) {
            JOptionPane.showMessageDialog(this,
                "Selected employee doesn't have a shift to swap.",
                "No Shift",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Swap Shifts", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Get employees with different shifts
        List<Employee> otherEmployees = controller.getAllEmployees().stream()
            .filter(e -> e.getShiftType() != null && 
                        !e.getFirstName().equals(selectedName.split(" ")[0]) &&
                        !e.getLastName().equals(selectedName.split(" ")[1]))
            .toList();

        if (otherEmployees.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No other employees with shifts available for swapping.",
                "No Available Employees",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Employee selection combo box
        DefaultComboBoxModel<String> employeeModel = new DefaultComboBoxModel<>();
        for (Employee emp : otherEmployees) {
            employeeModel.addElement(emp.getFirstName() + " " + emp.getLastName() + 
                                   " (" + emp.getShiftType() + ")");
        }
        JComboBox<String> employeeCombo = new JComboBox<>(employeeModel);

        // Add components to form
        gbc.gridy = 0;
        formPanel.add(new JLabel("Selected Employee:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JLabel(selectedName + " (" + currentShift + ")"), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Swap with:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employeeCombo, gbc);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton swapButton = new JButton("Swap");
        JButton cancelButton = new JButton("Cancel");

        swapButton.addActionListener(action -> {
            String targetName = ((String) employeeCombo.getSelectedItem()).split(" \\(")[0];
            String[] targetNameParts = targetName.split(" ");
            
            // Find both employees
            Employee emp1 = controller.getAllEmployees().stream()
                .filter(emp -> emp.getFirstName().equals(selectedName.split(" ")[0]) && 
                             emp.getLastName().equals(selectedName.split(" ")[1]))
                .findFirst()
                .orElse(null);
                
            Employee emp2 = controller.getAllEmployees().stream()
                .filter(emp -> emp.getFirstName().equals(targetNameParts[0]) && 
                             emp.getLastName().equals(targetNameParts[1]))
                .findFirst()
                .orElse(null);

            if (emp1 == null || emp2 == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Could not find one or both employees. Please check the names and try again.",
                    "Employee Not Found",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate both employees have shifts assigned
            if (emp1.getShiftType() == null || emp2.getShiftType() == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Both employees must have shifts assigned to swap.",
                    "Invalid Swap",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate employees are not the same
            if (emp1.getEmployeeId() == emp2.getEmployeeId()) {
                JOptionPane.showMessageDialog(dialog,
                    "Cannot swap shifts with the same employee.",
                    "Invalid Swap",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirm swap with user
            int confirm = JOptionPane.showConfirmDialog(dialog,
                String.format("Are you sure you want to swap shifts?\n\n%s: %s Shift\n%s: %s Shift",
                    emp1.getFirstName() + " " + emp1.getLastName(), emp1.getShiftType(),
                    emp2.getFirstName() + " " + emp2.getLastName(), emp2.getShiftType()),
                "Confirm Shift Swap",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                if (controller.swapEmployeeShifts(emp1.getEmployeeId(), emp2.getEmployeeId())) {
                    JOptionPane.showMessageDialog(dialog,
                        String.format("Successfully swapped shifts between:\n%s and %s",
                            emp1.getFirstName() + " " + emp1.getLastName(),
                            emp2.getFirstName() + " " + emp2.getLastName()),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadShifts();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to swap shifts. The database operation was unsuccessful.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog,
                    "An error occurred while swapping shifts: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });

        cancelButton.addActionListener(event -> dialog.dispose());

        buttonPanel.add(swapButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showBreakScheduleDialog() {
        int selectedRow = shiftsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to view/edit break schedule.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeName = (String) tableModel.getValueAt(selectedRow, 0);
        String shiftType = (String) tableModel.getValueAt(selectedRow, 2);
        
        if ("Unassigned".equals(shiftType)) {
            JOptionPane.showMessageDialog(this,
                "Employee doesn't have an assigned shift.",
                "No Shift",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Break Schedule", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Employee: " + employeeName));
        infoPanel.add(new JLabel("Shift: " + shiftType));
        
        // Add break time info based on shift type
        String breakInfo;
        if (shiftType.startsWith("Morning")) {
            breakInfo = "Standard Break Time: 10:00 AM - 10:30 AM";
        } else if (shiftType.startsWith("Afternoon")) {
            breakInfo = "Standard Break Time: 3:00 PM - 3:30 PM";
        } else {
            breakInfo = "Standard Break Time: 8:00 PM - 8:30 PM";
        }
        infoPanel.add(new JLabel(breakInfo));

        // Create note
        JTextArea noteArea = new JTextArea(
            "Note: Break times are standardized for each shift to ensure proper coverage.\n" +
            "Please coordinate with your supervisor for any special arrangements."
        );
        noteArea.setEditable(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBackground(dialog.getBackground());
        noteArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add components
        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(noteArea, BorderLayout.CENTER);
        
        // Add OK button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelp() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Shift Management");
        helpDialog.setVisible(true);
    }
}