package view.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTablePanel extends JPanel {
    protected final JTable table;
    protected final DefaultTableModel tableModel;
    protected final List<Integer> itemIds;
    protected final JToolBar toolbar;
    protected final JButton addButton;
    protected final JButton editButton;
    protected final JButton deleteButton;
    protected final JButton refreshButton;
    protected final JButton helpButton;

    public BaseTablePanel(String[] columnNames) {
        setLayout(new BorderLayout(10, 10));
        
        // Initialize table
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        itemIds = new ArrayList<>();

        // Initialize toolbar
        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Initialize buttons
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        helpButton = new JButton("Help");

        // Add buttons to toolbar
        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.addSeparator();
        toolbar.add(refreshButton);
        toolbar.add(helpButton);

        // Add components to panel
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Add selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelectionChanged();
            }
        });
    }

    protected abstract void onSelectionChanged();
    protected abstract void refreshData();
    protected abstract void showAddDialog();
    protected abstract void showEditDialog();
    protected abstract void showDeleteDialog();
    protected abstract void showHelpDialog();

    protected void setupButtonListeners() {
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> showDeleteDialog());
        refreshButton.addActionListener(e -> refreshData());
        helpButton.addActionListener(e -> showHelpDialog());
    }

    protected int getSelectedRow() {
        return table.getSelectedRow();
    }

    protected int getSelectedId() {
        int row = getSelectedRow();
        return row >= 0 ? itemIds.get(row) : -1;
    }

    protected void clearTable() {
        tableModel.setRowCount(0);
        itemIds.clear();
    }

    protected void addRow(Object[] data, int id) {
        tableModel.addRow(data);
        itemIds.add(id);
    }
} 