package view;

import javax.swing.*;
import java.awt.*;
import controller.RestaurantController;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private RestaurantController controller;

    public MainFrame() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        // Initialize controller
        controller = new RestaurantController();

        // Initialize components
        initComponents();

        // Set layout
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Inventory", new RecordsPanel(controller));
        tabbedPane.addTab("Orders", new TransactionsPanel(controller));
        tabbedPane.addTab("Customers", new CustomerPanel(controller));
        tabbedPane.addTab("Employees", new EmployeePanel(controller));
        tabbedPane.addTab("Suppliers", new SupplierPanel(controller));
        tabbedPane.addTab("Reports", new ReportsPanel(controller));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
} 