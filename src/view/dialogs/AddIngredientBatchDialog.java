package view.dialogs;

import controller.RestaurantController;
import model.Ingredient;
import model.IngredientBatch;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AddIngredientBatchDialog extends JDialog {
    private final RestaurantController controller;
    private JComboBox<String> ingredientComboBox;
    private JComboBox<String> supplierComboBox;
    private JSpinner quantitySpinner;
    private JSpinner priceSpinner;
    private JSpinner purchaseDateSpinner;
    private JSpinner expiryDateSpinner;
    private Map<String, Integer> ingredientMap;
    private Map<String, Integer> supplierMap;
    private boolean confirmed = false;

    public AddIngredientBatchDialog(Frame owner, RestaurantController controller) {
        super(owner, "Add Ingredient Batch", true);
        this.controller = controller;
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ingredient combo box
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Ingredient:"), gbc);

        gbc.gridx = 1;
        ingredientComboBox = new JComboBox<>();
        loadIngredients();
        mainPanel.add(ingredientComboBox, gbc);

        // Supplier combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Supplier:"), gbc);

        gbc.gridx = 1;
        supplierComboBox = new JComboBox<>();
        loadSuppliers();
        mainPanel.add(supplierComboBox, gbc);

        // Quantity spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        quantitySpinner = new JSpinner(quantityModel);
        mainPanel.add(quantitySpinner, gbc);

        // Purchase price spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Purchase Price:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel priceModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        priceSpinner = new JSpinner(priceModel);
        mainPanel.add(priceSpinner, gbc);

        // Purchase date spinner
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Purchase Date:"), gbc);

        gbc.gridx = 1;
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerDateModel purchaseDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        purchaseDateSpinner = new JSpinner(purchaseDateModel);
        purchaseDateSpinner.setEditor(new JSpinner.DateEditor(purchaseDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(purchaseDateSpinner, gbc);

        // Expiry date spinner
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("Expiry Date:"), gbc);

        gbc.gridx = 1;
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1); // Default expiry date is one month from now
        SpinnerDateModel expiryDateModel = new SpinnerDateModel(calendar.getTime(), earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        expiryDateSpinner = new JSpinner(expiryDateModel);
        expiryDateSpinner.setEditor(new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(expiryDateSpinner, gbc);

        // Add main panel
        add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(addButton);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private void loadIngredients() {
        ingredientMap = new HashMap<>();
        String query = "SELECT ingredient_id, name FROM Ingredients WHERE is_deleted = 0 ORDER BY name";

        try {
            java.sql.Connection conn = controller.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("ingredient_id");
                ingredientMap.put(name, id);
                ingredientComboBox.addItem(name);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load ingredients: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSuppliers() {
        supplierMap = new HashMap<>();
        String query = "SELECT supplier_id, name FROM Suppliers WHERE is_deleted = 0 ORDER BY name";

        try {
            java.sql.Connection conn = controller.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("supplier_id");
                supplierMap.put(name, id);
                supplierComboBox.addItem(name);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load suppliers: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        if (ingredientComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an ingredient.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (supplierComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a supplier.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double quantity = (Double) quantitySpinner.getValue();
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be greater than 0.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double price = (Double) priceSpinner.getValue();
        if (price <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Purchase price must be greater than 0.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date purchaseDate = (Date) purchaseDateSpinner.getValue();
        Date expiryDate = (Date) expiryDateSpinner.getValue();

        if (expiryDate.before(purchaseDate)) {
            JOptionPane.showMessageDialog(this,
                    "Expiry date cannot be before purchase date.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public IngredientBatch getBatch() {
        if (!confirmed) return null;

        // Get selected items
        String selectedIngredient = (String) ingredientComboBox.getSelectedItem();
        String selectedSupplier = (String) supplierComboBox.getSelectedItem();

        // Validate selections
        if (selectedIngredient == null || selectedSupplier == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both an ingredient and a supplier.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Get the IDs from the maps
        int ingredientId = ingredientMap.get(selectedIngredient);
        int supplierId = supplierMap.get(selectedSupplier);

        // Get the ingredient to get its unit ID
        Ingredient ingredient = controller.getIngredientById(ingredientId);
        if (ingredient == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to get ingredient details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Get values from spinners
        double quantity = (Double) quantitySpinner.getValue();
        double purchasePrice = (Double) priceSpinner.getValue();
        Date purchaseDate = (Date) purchaseDateSpinner.getValue();
        Date expiryDate = (Date) expiryDateSpinner.getValue();

        // Validate values
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be greater than 0.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (purchasePrice <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Purchase price must be greater than 0.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (expiryDate.before(purchaseDate)) {
            JOptionPane.showMessageDialog(this,
                    "Expiry date cannot be before purchase date.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Create new batch with required parameters
        IngredientBatch batch = new IngredientBatch(
                0, // batchId will be set by database
                ingredientId,
                ingredient.getUnitId(), // Get unit ID from the ingredient
                quantity,
                quantity, // remaining quantity starts equal to quantity
                purchaseDate,
                expiryDate,
                purchasePrice,
                supplierId
        );

        // Set the ingredient name for display purposes
        batch.setIngredientName(selectedIngredient);
        batch.setUnitName(ingredient.getUnitName());

        return batch;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}