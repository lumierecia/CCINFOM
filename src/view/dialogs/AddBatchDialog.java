package view.dialogs;

import controller.RestaurantController;
import model.Ingredient;
import model.Supplier;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;

public class AddBatchDialog extends JDialog {
    private final RestaurantController controller;
    private final Ingredient ingredient;
    private JSpinner quantitySpinner;
    private JSpinner purchaseDateSpinner;
    private JSpinner expiryDateSpinner;
    private JComboBox<Supplier> supplierCombo;
    private JTextField priceField;
    private boolean success = false;

    public AddBatchDialog(Frame owner, Ingredient ingredient, RestaurantController controller) {
        super(owner, "Add Batch for " + ingredient.getName(), true);
        this.controller = controller;
        this.ingredient = ingredient;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create input panel
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Quantity input
        inputPanel.add(new JLabel("Quantity (" + ingredient.getUnitName() + "):"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10000.0, 0.1));
        inputPanel.add(quantitySpinner);
        
        // Purchase date input
        inputPanel.add(new JLabel("Purchase Date:"));
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerDateModel purchaseDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        purchaseDateSpinner = new JSpinner(purchaseDateModel);
        purchaseDateSpinner.setEditor(new JSpinner.DateEditor(purchaseDateSpinner, "yyyy-MM-dd"));
        inputPanel.add(purchaseDateSpinner);
        
        // Expiry date input
        inputPanel.add(new JLabel("Expiry Date:"));
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1); // Default expiry is one month from now
        initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        latestDate = calendar.getTime();
        SpinnerDateModel expiryDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        expiryDateSpinner = new JSpinner(expiryDateModel);
        expiryDateSpinner.setEditor(new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd"));
        inputPanel.add(expiryDateSpinner);
        
        // Supplier selection
        inputPanel.add(new JLabel("Supplier:"));
        supplierCombo = new JComboBox<>();
        for (Supplier supplier : controller.getAllSuppliers()) {
            if (!supplier.isDeleted()) {
                supplierCombo.addItem(supplier);
            }
        }
        inputPanel.add(supplierCombo);
        
        // Price input
        inputPanel.add(new JLabel("Purchase Price:"));
        priceField = new JTextField();
        inputPanel.add(priceField);
        
        // Add input panel
        add(inputPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            if (validateInput()) {
                success = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        pack();
        setLocationRelativeTo(getOwner());
    }

    private boolean validateInput() {
        // Validate quantity
        double quantity = (Double) quantitySpinner.getValue();
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this,
                "Quantity must be greater than 0",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate dates
        Date purchaseDate = (Date) purchaseDateSpinner.getValue();
        Date expiryDate = (Date) expiryDateSpinner.getValue();
        if (expiryDate.before(purchaseDate)) {
            JOptionPane.showMessageDialog(this,
                "Expiry date must be after purchase date",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate supplier
        if (supplierCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate price
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid price",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getQuantity() {
        return (Double) quantitySpinner.getValue();
    }

    public Date getPurchaseDate() {
        return (Date) purchaseDateSpinner.getValue();
    }

    public Date getExpiryDate() {
        return (Date) expiryDateSpinner.getValue();
    }

    public Supplier getSelectedSupplier() {
        return (Supplier) supplierCombo.getSelectedItem();
    }

    public double getPurchasePrice() {
        return Double.parseDouble(priceField.getText());
    }
} 