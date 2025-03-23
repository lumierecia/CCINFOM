package view.dialogs;

import controller.RestaurantController;
import model.Supplier;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditSupplierDialog extends JDialog {
    private final RestaurantController controller;
    private final Supplier supplier;
    private JTextField nameField;
    private JTextField contactPersonField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private boolean confirmed = false;

    public EditSupplierDialog(Frame owner, RestaurantController controller, Supplier supplier) {
        super(owner, "Edit Supplier", true);
        this.controller = controller;
        this.supplier = supplier;
        initComponents();
        loadSupplierData();
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

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        nameField = new JTextField(30);
        mainPanel.add(nameField, gbc);

        // Contact person field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Contact Person:"), gbc);
        
        gbc.gridx = 1;
        contactPersonField = new JTextField(30);
        mainPanel.add(contactPersonField, gbc);

        // Phone field
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        phoneField = new JTextField(30);
        mainPanel.add(phoneField, gbc);

        // Email field
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        emailField = new JTextField(30);
        mainPanel.add(emailField, gbc);

        // Address area
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 30);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        mainPanel.add(addressScroll, gbc);

        // Add main panel
        add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private void loadSupplierData() {
        nameField.setText(supplier.getName());
        contactPersonField.setText(supplier.getContactPerson());
        phoneField.setText(supplier.getPhone());
        emailField.setText(supplier.getEmail());
        addressArea.setText(supplier.getAddress());
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a supplier name.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String contactPerson = contactPersonField.getText().trim();
        if (contactPerson.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a contact person.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a phone number.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter an email address.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String address = addressArea.getText().trim();
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter an address.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public Supplier getUpdatedSupplier() {
        if (!confirmed) return null;

        supplier.setName(nameField.getText().trim());
        supplier.setContactPerson(contactPersonField.getText().trim());
        supplier.setPhone(phoneField.getText().trim());
        supplier.setEmail(emailField.getText().trim());
        supplier.setAddress(addressArea.getText().trim());

        return supplier;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 