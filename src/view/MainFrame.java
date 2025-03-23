package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import controller.RestaurantController;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private RestaurantController controller;
    private JMenuBar menuBar;
    private JMenuItem viewDeletedRecordsItem;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DeletedRecordsPanel deletedRecordsPanel;

    public MainFrame() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Initialize controller
        controller = new RestaurantController();

        // Initialize layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize components
        initComponents();
        initMenuBar();

        // Add panels to card layout
        mainPanel.add(tabbedPane, "Main");
        
        deletedRecordsPanel = new DeletedRecordsPanel(controller, this);
        mainPanel.add(deletedRecordsPanel, "DeletedRecords");

        // Show the main panel
        add(mainPanel);
        cardLayout.show(mainPanel, "Main");
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Add main tabs
        tabbedPane.addTab("Customers", new CustomerPanel(controller));
        tabbedPane.addTab("Transactions", new TransactionsPanel(controller));  // This already contains Orders, Payments, and Shifts
        tabbedPane.addTab("Employees", new EmployeePanel(controller));
        tabbedPane.addTab("Suppliers", new SupplierPanel(controller));
        tabbedPane.addTab("Menu Items", new RecordsPanel(controller));  // Renamed from "Inventory" to "Menu Items" for clarity
        tabbedPane.addTab("Ingredients", new IngredientPanel(controller));  // Added new Ingredients tab
        tabbedPane.addTab("Reports", new ReportsPanel(controller));
    }

    private void initMenuBar() {
        menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewDeletedRecordsItem = new JMenuItem("View Deleted Records");
        viewDeletedRecordsItem.addActionListener(e -> showDeletedRecordsPanel());
        viewMenu.add(viewDeletedRecordsItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

    private void showDeletedRecordsPanel() {
        cardLayout.show(mainPanel, "DeletedRecords");
        if (deletedRecordsPanel != null) {
            deletedRecordsPanel.refreshAllTables();
        }
    }

    public void showMainPanel() {
        cardLayout.show(mainPanel, "Main");
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