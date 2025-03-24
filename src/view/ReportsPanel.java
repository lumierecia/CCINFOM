package view;

import controller.ReportGenerator;
import controller.RestaurantController;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReportsPanel extends JPanel {
    private final ReportGenerator reportGenerator;
    private final JTextArea reportArea;
    private final JComboBox<String> reportTypeCombo;
    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;
    private final SimpleDateFormat dateFormat;

    public ReportsPanel(RestaurantController controller) throws Exception {
        this.reportGenerator = new ReportGenerator(controller);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Report type selection
        reportTypeCombo = new JComboBox<>(new String[]{
            "Sales Report",
            "Employee Performance Report",
            "Customer Insights Report",
            "Profit Margin Report"
        });
        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeCombo);

        // Date range selection
        Calendar calendar = Calendar.getInstance();
        startDateSpinner = new JSpinner(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH));
        endDateSpinner = new JSpinner(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH));
        
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        endDateSpinner.setEditor(endDateEditor);

        controlPanel.add(new JLabel("Start Date:"));
        controlPanel.add(startDateSpinner);
        controlPanel.add(new JLabel("End Date:"));
        controlPanel.add(endDateSpinner);

        // Generate button
        JButton generateButton = new JButton("Generate Report");
        generateButton.setBackground(new Color(40, 167, 69));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setBorderPainted(false);
        generateButton.setOpaque(true);
        generateButton.addActionListener(e -> generateReport());
        controlPanel.add(generateButton);

        // Export button
        JButton exportButton = new JButton("Export Report");
        exportButton.setBackground(new Color(23, 162, 184));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setBorderPainted(false);
        exportButton.setOpaque(true);
        exportButton.addActionListener(e -> exportReport());
        controlPanel.add(exportButton);

        // Report display area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));

        // Add components to panel
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void generateReport() {
        try {
            java.util.Date startDateUtil = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDateUtil = (java.util.Date) endDateSpinner.getValue();
            Date startDate = new Date(startDateUtil.getTime());
            Date endDate = new Date(endDateUtil.getTime());
            
            String report = switch (reportTypeCombo.getSelectedIndex()) {
                case 0 -> reportGenerator.generateSalesReport(startDate, endDate);
                case 1 -> reportGenerator.generateEmployeePerformanceReport(startDate, endDate);
                case 2 -> reportGenerator.generateCustomerInsightsReport(startDate, endDate);
                case 3 -> reportGenerator.generateProfitMarginReport(startDate, endDate);
                default -> throw new IllegalStateException("Invalid report type selected");
            };
            
            reportArea.setText(report);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating report: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReport() {
        if (reportArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please generate a report first.",
                "No Report to Export",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            // Set default filename based on report type and date range
            String defaultName = String.format("%s_%s_to_%s.txt",
                reportTypeCombo.getSelectedItem().toString().replace(" ", "_"),
                dateFormat.format(startDateSpinner.getValue()),
                dateFormat.format(endDateSpinner.getValue()));
            fileChooser.setSelectedFile(new java.io.File(defaultName));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new java.io.File(file.getAbsolutePath() + ".txt");
                }

                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.write(reportArea.getText());
                }

                JOptionPane.showMessageDialog(this,
                    "Report exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error exporting report: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 