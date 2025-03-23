package view;

import controller.RestaurantController;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class RecordsPanel extends JPanel {
    private RestaurantController controller;
    private JTable table;
    private DefaultTableModel model;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton helpButton;
    private JButton viewRecipeButton;
    private List<Integer> itemIds = new ArrayList<>();

    public RecordsPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add buttons with consistent styling
        JButton addButton = createStyledButton("Add Item", new Color(40, 167, 69));
        addButton.addActionListener(e -> showAddDialog());
        
        JButton editButton = createStyledButton("Edit Item", new Color(255, 193, 7));
        editButton.addActionListener(e -> showEditDialog());
        
        JButton deleteButton = createStyledButton("Delete Item", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deleteSelectedItem());
        
        JButton viewRecipeButton = createStyledButton("View Recipe", new Color(70, 130, 180));
        viewRecipeButton.addActionListener(e -> showRecipeDialog());
        
        JButton helpButton = createStyledButton("Help", new Color(108, 117, 125));
        helpButton.addActionListener(e -> showHelpDialog());
        
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(viewRecipeButton);
        toolBar.add(helpButton);
        
        add(toolBar, BorderLayout.NORTH);

        // Initialize table with enhanced tooltip
        String[] columnNames = {"Name", "Category", "Make Price", "Sell Price", "Quantity", "Status"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("<html>Inventory items list<br><i>Click on an item to select it</i></html>");
        JScrollPane scrollPane = new JScrollPane(table);

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

    private void loadData() {
        model.setRowCount(0);
        itemIds.clear();
        List<Inventory> items = controller.getAllInventoryItems();
        for (Inventory item : items) {
            itemIds.add(item.getProductId());
            model.addRow(new Object[]{
                item.getProductName(),
                item.getCategoryName(),
                item.getMakePrice(),
                item.getSellPrice(),
                item.getQuantity(),
                item.getQuantity() == 0 ? "Unavailable" : "Available"
            });
        }
    }

    private void showHelpDialog() {
        // Create a custom panel with better formatting
        JPanel helpPanel = new JPanel(new BorderLayout(10, 10));
        helpPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create tabbed pane for different help sections
        JTabbedPane tabbedPane = new JTabbedPane();

        // Overview tab
        JTextArea overviewText = new JTextArea("""
            The Records Panel is your central hub for managing the restaurant's inventory.
            
            Key Features:
            • View all menu items and their details
            • Filter items by category
            • Add new menu items
            • Edit existing items
            • Delete items
            • View cooking recipes
            • Track stock levels
            """);
        setupHelpTextArea(overviewText);
        tabbedPane.addTab("Overview", new JScrollPane(overviewText));

        // How-To Guide tab
        JTextArea howToText = new JTextArea("""
            Step-by-Step Guides:
            
            1. Adding a New Menu Item:
               a) Click the "Add Item" button
               b) Fill in the item details:
                  - Name: The dish name (e.g., "Chicken Adobo")
                  - Category: Select appropriate category
                  - Make Price: Your cost (e.g., ₱150)
                  - Sell Price: Customer price (e.g., ₱250)
                  - Quantity: Initial stock level
                  - Recipe: Cooking instructions
               c) Click OK to save
            
            2. Viewing a Recipe:
               a) Select an item in the table
               b) Click the "View Recipe" button
               c) Read the cooking instructions
               d) Click Close when done
            
            3. Updating Stock:
               a) Find the item in the table
               b) Click "Edit Item"
               c) Update the quantity
               d) Click OK to save changes
            
            4. Filtering Items:
               a) Use the Category dropdown at the top
               b) Select a category to filter
               c) Select "All Categories" to show everything
            
            5. Searching Items:
               a) Type in the search box
               b) Results filter as you type
               c) Search works with item names
            """);
        setupHelpTextArea(howToText);
        tabbedPane.addTab("How-To Guide", new JScrollPane(howToText));

        // Tips & Best Practices tab
        JTextArea tipsText = new JTextArea("""
            Important Tips:
            
            Pricing:
            • Sell price must always be higher than make price
            • Consider food costs and market prices
            • Regular price reviews recommended
            
            Stock Management:
            • Update quantities regularly
            • Zero quantity marks items as "Unavailable"
            • Monitor low stock items
            
            Categories:
            • Use categories to organize menu items
            • Makes filtering and finding items easier
            • Helps with menu planning
            
            Recipes:
            • Keep recipes clear and detailed
            • Include cooking times and temperatures
            • List ingredients in order of use
            • Add special instructions if needed
            
            Best Practices:
            • Keep item names clear and consistent
            • Include detailed recipe instructions
            • Regular inventory checks
            • Update prices as costs change
            """);
        setupHelpTextArea(tipsText);
        tabbedPane.addTab("Tips & Best Practices", new JScrollPane(tipsText));

        // Field Descriptions tab
        JTextArea fieldsText = new JTextArea("""
            Table Fields Explained:
            
            Name:
            • Item name as shown on the menu
            • Should be clear and descriptive
            
            Category:
            • Type of dish (Main Course, Desserts, etc.)
            • Used for organization and filtering
            
            Make Price:
            • Your cost to prepare the item
            • Includes ingredients and preparation costs
            
            Sell Price:
            • Price charged to customers
            • Must be higher than make price
            
            Quantity:
            • Current stock level
            • Zero means item is unavailable
            
            Status:
            • Available: Item can be ordered
            • Unavailable: Item out of stock
            """);
        setupHelpTextArea(fieldsText);
        tabbedPane.addTab("Field Descriptions", new JScrollPane(fieldsText));

        helpPanel.add(tabbedPane, BorderLayout.CENTER);
        helpPanel.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this,
                helpPanel,
                "Help - Records Panel",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupHelpTextArea(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setBackground(new Color(252, 252, 252));
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(currentFont.getFontName(), Font.PLAIN, 12));
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField(20);
        JComboBox<String> categoryField = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        JTextField makePriceField = new JTextField(10);
        JTextField sellPriceField = new JTextField(10);
        JTextField quantityField = new JTextField(10);
        JTextArea recipeField = new JTextArea(5, 20);
        recipeField.setLineWrap(true);
        recipeField.setWrapStyleWord(true);
        JScrollPane recipeScrollPane = new JScrollPane(recipeField);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Make Price:"));
        panel.add(makePriceField);
        panel.add(new JLabel("Sell Price:"));
        panel.add(sellPriceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Recipe:"));
        panel.add(recipeScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add New Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a name for the item.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String category = (String) categoryField.getSelectedItem();
                if (category == null) {
                    JOptionPane.showMessageDialog(this,
                            "Please select a category.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double makePrice = Double.parseDouble(makePriceField.getText().trim());
                if (makePrice <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Make price must be greater than 0.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double sellPrice = Double.parseDouble(sellPriceField.getText().trim());
                if (sellPrice <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Sell price must be greater than 0.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (sellPrice <= makePrice) {
                    JOptionPane.showMessageDialog(this,
                            "Sell price must be greater than make price.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Quantity cannot be negative.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String recipe = recipeField.getText().trim();

                if (controller.addInventoryItem(name, category, makePrice, sellPrice, quantity, recipe)) {
                    loadData();
                    JOptionPane.showMessageDialog(this,
                            "Item added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to add item.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for prices and quantity.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = itemIds.get(selectedRow);
        Inventory item = controller.getInventoryItemById(id);
        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load item details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(item.getProductName(), 20);
        JComboBox<String> categoryField = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        categoryField.setSelectedItem(item.getCategoryName());
        JTextField makePriceField = new JTextField(String.valueOf(item.getMakePrice()), 10);
        JTextField sellPriceField = new JTextField(String.valueOf(item.getSellPrice()), 10);
        JTextField quantityField = new JTextField(String.valueOf(item.getQuantity()), 10);
        JTextArea recipeField = new JTextArea(item.getRecipeInstructions(), 5, 20);
        recipeField.setLineWrap(true);
        recipeField.setWrapStyleWord(true);
        JScrollPane recipeScrollPane = new JScrollPane(recipeField);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Make Price:"));
        panel.add(makePriceField);
        panel.add(new JLabel("Sell Price:"));
        panel.add(sellPriceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Recipe:"));
        panel.add(recipeScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String category = (String) categoryField.getSelectedItem();
                double makePrice = Double.parseDouble(makePriceField.getText());
                double sellPrice = Double.parseDouble(sellPriceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String recipe = recipeField.getText();

                if (controller.updateInventoryItem(id, name, category, makePrice, sellPrice, quantity, recipe)) {
                    loadData();
                    JOptionPane.showMessageDialog(this,
                            "Item updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update item.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for prices and quantity.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = itemIds.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteInventoryItem(id)) {
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Item deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete item.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRecipeDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to view its recipe.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = itemIds.get(selectedRow);
        Inventory item = controller.getInventoryItemById(id);
        if (item == null || item.getRecipeInstructions() == null || item.getRecipeInstructions().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No recipe available for this item.",
                    "No Recipe",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create a custom dialog for recipe display
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Recipe: " + item.getProductName(), true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create recipe text area
        JTextArea recipeArea = new JTextArea(item.getRecipeInstructions());
        recipeArea.setEditable(false);
        recipeArea.setLineWrap(true);
        recipeArea.setWrapStyleWord(true);
        recipeArea.setMargin(new Insets(10, 10, 10, 10));
        recipeArea.setBackground(new Color(252, 252, 252));
        Font currentFont = recipeArea.getFont();
        recipeArea.setFont(new Font(currentFont.getFontName(), Font.PLAIN, 14));

        // Add components to dialog
        JScrollPane scrollPane = new JScrollPane(recipeArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 