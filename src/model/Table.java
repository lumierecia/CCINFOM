package model;

public class Table {
    private int tableId;
    private int tableNumber;
    private int capacity;
    private String status;
    private boolean isDeleted;

    public Table() {}

    public Table(int tableId, int tableNumber, int capacity, String status, boolean isDeleted) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    // Getters
    public int getTableId() { return tableId; }
    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public boolean isDeleted() { return isDeleted; }

    // Setters
    public void setTableId(int tableId) { this.tableId = tableId; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStatus(String status) { this.status = status; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "Table " + tableNumber + " (" + capacity + " seats) - " + status;
    }
} 