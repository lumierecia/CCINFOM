package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderPanel extends JPanel {
    private final RestaurantController controller;
    private JTable orderItemsTable;
    private DefaultTableModel itemsModel;
    private JComboBox<String> categoryCombo;
    private JComboBox<Dish> productCombo;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;
    private double total = 0.0;
    private List<OrderItem> orderItems = new ArrayList<>();

    public OrderPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Create main panels
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Setup category selection
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        categoryCombo.setToolTipText("<html>Select a food category to filter available items</html>");
        categoryCombo.addActionListener(e -> updateProductCombo());
        categoryPanel.add(categoryCombo);

        // Add help button for category selection
        JButton categoryHelpBtn = createHelpButton("""
            Category Selection:
            • Choose a category to filter the menu items
            • This helps you find items more quickly
            • The product list will update automatically
            """);
        categoryPanel.add(categoryHelpBtn);

        // Setup product selection
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.add(new JLabel("Product:"));
        productCombo = new JComboBox<>();
        productCombo.setToolTipText("<html>Select the menu item to add to the order</html>");
        updateProductCombo();
        productPanel.add(productCombo);

        // Add help button for product selection
        JButton productHelpBtn = createHelpButton("""
            Product Selection:
            • Choose the item you want to order
            • Only available items are shown
            • Price and stock are automatically tracked
            """);
        productPanel.add(productHelpBtn);

        // Setup quantity selection
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setToolTipText("<html>Set how many of this item to order</html>");
        quantityPanel.add(quantitySpinner);

        // Add help button for quantity
        JButton quantityHelpBtn = createHelpButton("""
            Quantity Selection:
            • Choose how many items to order
            • Cannot exceed available stock
            • Minimum is 1, maximum is 100
            """);
        quantityPanel.add(quantityHelpBtn);

        // Create action buttons with consistent styling
        JButton addButton = createStyledButton("Add to Order", new Color(40, 167, 69));
        addButton.addActionListener(e -> addItemToOrder());

        JButton removeButton = createStyledButton("Remove Item", new Color(220, 53, 69));
        removeButton.addActionListener(e -> removeSelectedItem());

        JButton clearButton = createStyledButton("Clear All", new Color(255, 193, 7));
        clearButton.addActionListener(e -> clearOrder());

        JButton placeOrderButton = createStyledButton("Place Order", new Color(70, 130, 180));
        placeOrderButton.addActionListener(e -> placeOrder());

        JButton helpButton = createStyledButton("Help", new Color(108, 117, 125));
        helpButton.addActionListener(e -> showHelp());

        // Add buttons to panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(placeOrderButton);
        buttonPanel.add(helpButton);

        // Combine top components
        topPanel.add(categoryPanel, BorderLayout.WEST);
        topPanel.add(productPanel, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightPanel.add(quantityPanel);
        rightPanel.add(buttonPanel);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Setup order items table with tooltip
        String[] columns = {
            "Product Name", "Category", "Quantity", 
            "Unit Price", "Total"
        };
        itemsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderItemsTable = new JTable(itemsModel);
        orderItemsTable.setToolTipText("<html>List of items in your current order</html>");
        orderItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderItemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        orderItemsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Product Name
        orderItemsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Category
        orderItemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Quantity
        orderItemsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Unit Price
        orderItemsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Total

        JScrollPane scrollPane = new JScrollPane(orderItemsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Add help button for order table
        JButton tableHelpBtn = createHelpButton("""
            Order Table:
            • Shows all items in your current order
            • Displays quantity and prices
            • Select an row to remove item
            • Subtotals are calculated automatically
            """);
        centerPanel.add(tableHelpBtn, BorderLayout.NORTH);

        // Setup bottom panel with total and buttons
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: $0.00");
        totalPanel.add(totalLabel);

        bottomPanel.add(totalPanel, BorderLayout.CENTER);
        
        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateProductCombo() {
        String category = (String) categoryCombo.getSelectedItem();
        productCombo.removeAllItems();
        if (category != null) {
            List<Dish> dishes = controller.getDishesByCategory(category);
            for (Dish dish : dishes) {
                if (dish.isAvailable()) {
                    productCombo.addItem(dish);
                }
            }
        }
    }

    private void addItemToOrder() {
        Dish selectedDish = (Dish) productCombo.getSelectedItem();
        if (selectedDish == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a dish to add to the order.",
                "No Dish Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show ingredients dialog before adding to order
        if (!showIngredientDetailsDialog(selectedDish)) {
            return; // User cancelled or not enough ingredients
        }

        int quantity = (int) quantitySpinner.getValue();

        // Check if the item is already in the order
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            String dishName = (String) itemsModel.getValueAt(i, 0);
            if (dishName.equals(selectedDish.getName())) {
                int currentQty = (int) itemsModel.getValueAt(i, 2);
                itemsModel.setValueAt(currentQty + quantity, i, 2);
                double subtotal = (currentQty + quantity) * selectedDish.getSellingPrice();
                itemsModel.setValueAt(String.format("$%.2f", subtotal), i, 4);
                
                // Update the OrderItem in our list
                for (OrderItem item : orderItems) {
                    if (item.getDishId() == selectedDish.getDishId()) {
                        item.setQuantity(currentQty + quantity);
                        break;
                    }
                }
                
                updateTotal();
                return;
            }
        }

        // Add new item to the table
        double subtotal = quantity * selectedDish.getSellingPrice();
        Object[] row = {
            selectedDish.getName(),
            selectedDish.getCategoryName(),
            quantity,
            String.format("$%.2f", selectedDish.getSellingPrice()),
            String.format("$%.2f", subtotal)
        };
        itemsModel.addRow(row);

        // Add to order items list
        OrderItem newItem = new OrderItem(
            0, // Temporary orderId, will be set when order is saved <------
            selectedDish.getDishId(),
            quantity,
            selectedDish.getSellingPrice(),
            selectedDish.getName()
        );
        orderItems.add(newItem);

        updateTotal();
    }

    private boolean showIngredientDetailsDialog(Dish dish) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Dish Ingredients", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel nameLabel = new JLabel("Dish: " + dish.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(nameLabel);
        
        JLabel priceLabel = new JLabel(String.format("Price: $%.2f", dish.getSellingPrice()));
        infoPanel.add(priceLabel);
        
        JLabel categoryLabel = new JLabel("Category: " + dish.getCategoryName());
        infoPanel.add(categoryLabel);

        // Create ingredients table
        String[] columns = {"Ingredient", "Required Amount", "Available Stock", "Status"};
        DefaultTableModel ingredientModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable ingredientTable = new JTable(ingredientModel);

        // Get ingredients for this dish
        Map<Integer, Double> requiredIngredients = controller.getDishIngredients(dish.getDishId());
        boolean hasEnoughIngredients = true;

        for (Map.Entry<Integer, Double> entry : requiredIngredients.entrySet()) {
            Ingredient ingredient = controller.getIngredientById(entry.getKey());
            if (ingredient != null) {
                String status = ingredient.getQuantityInStock() >= entry.getValue() ? 
                    "Available" : "Insufficient Stock";
                if (status.equals("Insufficient Stock")) {
                    hasEnoughIngredients = false;
                }
                Object[] row = {
                    ingredient.getName(),
                    String.format("%.2f %s", entry.getValue(), ingredient.getUnitName()),
                    String.format("%.2f %s", ingredient.getQuantityInStock(), ingredient.getUnitName()),
                    status
                };
                ingredientModel.addRow(row);
            }
        }

        // Add recipe instructions if available
        if (dish.getRecipeInstructions() != null && !dish.getRecipeInstructions().trim().isEmpty()) {
            JTextArea recipeArea = new JTextArea(dish.getRecipeInstructions());
            recipeArea.setEditable(false);
            recipeArea.setLineWrap(true);
            recipeArea.setWrapStyleWord(true);
            recipeArea.setBackground(new Color(250, 250, 250));
            recipeArea.setBorder(BorderFactory.createTitledBorder("Recipe Instructions"));
            
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.add(infoPanel, BorderLayout.NORTH);
            contentPanel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
            contentPanel.add(new JScrollPane(recipeArea), BorderLayout.SOUTH);
            dialog.add(contentPanel, BorderLayout.CENTER);
        } else {
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.add(infoPanel, BorderLayout.NORTH);
            contentPanel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
            dialog.add(contentPanel, BorderLayout.CENTER);
        }

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton(hasEnoughIngredients ? "Add to Order" : "Cannot Add - Insufficient Stock");
        addButton.setEnabled(hasEnoughIngredients);
        JButton cancelButton = new JButton("Cancel");

        final boolean[] confirmed = {false};
        
        addButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setSize(new Dimension(
            Math.max(dialog.getWidth(), 600),
            Math.max(dialog.getHeight(), 400)
        ));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return confirmed[0];
    }

    private void removeSelectedItem() {
        int selectedRow = orderItemsTable.getSelectedRow();
        if (selectedRow != -1) {
            orderItems.remove(selectedRow);
            itemsModel.removeRow(selectedRow);
            updateTotal();
        }
    }

    private void clearOrder() {
        itemsModel.setRowCount(0);
        orderItems.clear();
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void placeOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add items to the order before placing it.",
                "Empty Order",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create customer selection dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Customer", true);
        dialog.setLayout(new BorderLayout());
        
        // Create customer selection panel
        JPanel customerPanel = new JPanel(new BorderLayout());
        List<Customer> customers = controller.getAllCustomers();
        JComboBox<Customer> customerCombo = new JComboBox<>(customers.toArray(new Customer[0]));
        customerPanel.add(new JLabel("Select Customer:"), BorderLayout.WEST);
        customerPanel.add(customerCombo, BorderLayout.CENTER);
        
        // Create employee selection panel
        JPanel employeePanel = new JPanel(new BorderLayout());
        List<Employee> employees = controller.getAllEmployees();
        JComboBox<Employee> employeeCombo = new JComboBox<>(employees.toArray(new Employee[0]));
        employeePanel.add(new JLabel("Assign Employee:"), BorderLayout.WEST);
        employeePanel.add(employeeCombo, BorderLayout.CENTER);
        
        // Create order type panel
        JPanel typePanel = new JPanel(new BorderLayout());
        String[] orderTypes = {"Dine In", "Take Out", "Delivery"};
        JComboBox<String> typeCombo = new JComboBox<>(orderTypes);
        typePanel.add(new JLabel("Order Type:"), BorderLayout.WEST);
        typePanel.add(typeCombo, BorderLayout.CENTER);
        
        // Combine panels
        JPanel selectionPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        selectionPanel.add(customerPanel);
        selectionPanel.add(employeePanel);
        selectionPanel.add(typePanel);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("Place Order");
        JButton cancelButton = new JButton("Cancel");
        
        confirmButton.addActionListener(e -> {
            Customer selectedCustomer = (Customer) customerCombo.getSelectedItem();
            Employee selectedEmployee = (Employee) employeeCombo.getSelectedItem();
            String orderType = (String) typeCombo.getSelectedItem();
            
            if (selectedCustomer == null || selectedEmployee == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select both a customer and an employee.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create the order
            Order order = new Order();
            order.setCustomerId(selectedCustomer.getCustomerId());
            order.setOrderType(orderType);
            order.setOrderStatus("Pending");
            order.setPaymentStatus("Pending");
            
            // Create list of employee IDs
            List<Integer> employeeIds = new ArrayList<>();
            employeeIds.add(selectedEmployee.getEmployeeId());
            
            // Try to create the order
            if (controller.createOrder(order, orderItems, employeeIds)) {
                JOptionPane.showMessageDialog(this,
                    "Order placed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                clearOrder();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to place order. Please check ingredient availability.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(selectionPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JButton createHelpButton(String helpText) {
        JButton helpBtn = new JButton("?");
        helpBtn.setFont(new Font(helpBtn.getFont().getName(), Font.BOLD, 10));
        helpBtn.setMargin(new Insets(1, 4, 1, 4));
        helpBtn.setToolTipText("Click for help");
        helpBtn.addActionListener(e -> {
            JTextArea textArea = new JTextArea(helpText);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setMargin(new Insets(10, 10, 10, 10));
            textArea.setBackground(new Color(252, 252, 252));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 200));
            
            JOptionPane.showMessageDialog(this,
                scrollPane,
                "Help",
                JOptionPane.INFORMATION_MESSAGE);
        });
        return helpBtn;
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

    private void showHelp() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Orders");
        helpDialog.setVisible(true);
    }
} 