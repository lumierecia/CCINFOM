package view.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseFormDialog extends JDialog {
    protected final List<JComponent> formFields;
    protected final JButton saveButton;
    protected final JButton cancelButton;
    protected boolean confirmed;

    public BaseFormDialog(Frame owner, String title) {
        super(owner, title, true);
        this.formFields = new ArrayList<>();
        this.confirmed = false;

        // Initialize buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set up button listeners
        saveButton.addActionListener(e -> {
            if (validateForm()) {
                saveFormData();
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        // Set dialog properties
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    protected void addFormField(String label, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = formFields.size();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        fieldPanel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(field, gbc);

        formFields.add(field);
        ((JPanel) getContentPane().getComponent(0)).add(fieldPanel);
    }

    protected JTextField createTextField() {
        return new JTextField(20);
    }

    protected JTextArea createTextArea() {
        return new JTextArea(5, 20);
    }

    protected JComboBox<String> createComboBox(String[] items) {
        return new JComboBox<>(items);
    }

    protected JSpinner createSpinner(SpinnerNumberModel model) {
        return new JSpinner(model);
    }

    protected JCheckBox createCheckBox() {
        return new JCheckBox();
    }

    protected String getTextFieldValue(JTextField field) {
        return field.getText().trim();
    }

    protected String getTextAreaValue(JTextArea area) {
        return area.getText().trim();
    }

    protected String getComboBoxValue(JComboBox<String> combo) {
        return (String) combo.getSelectedItem();
    }

    protected int getSpinnerValue(JSpinner spinner) {
        return (Integer) spinner.getValue();
    }

    protected boolean getCheckBoxValue(JCheckBox check) {
        return check.isSelected();
    }

    protected void setTextFieldValue(JTextField field, String value) {
        field.setText(value);
    }

    protected void setTextAreaValue(JTextArea area, String value) {
        area.setText(value);
    }

    protected void setComboBoxValue(JComboBox<String> combo, String value) {
        combo.setSelectedItem(value);
    }

    protected void setSpinnerValue(JSpinner spinner, int value) {
        spinner.setValue(value);
    }

    protected void setCheckBoxValue(JCheckBox check, boolean value) {
        check.setSelected(value);
    }

    protected abstract boolean validateForm();
    protected abstract void saveFormData();

    public boolean isConfirmed() {
        return confirmed;
    }
} 