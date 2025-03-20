package model;

public class Supplier {
    private int supplierId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String status;

    public Supplier(int supplierId, String name, String contactPerson, String email, String phone, String address) {
        this(supplierId, name, contactPerson, email, phone, address, "Active");
    }

    public Supplier(int supplierId, String name, String contactPerson, String email, String phone, String address, String status) {
        this.supplierId = supplierId;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    // Getters
    public int getSupplierId() { return supplierId; }
    public String getName() { return name; }
    public String getContactPerson() { return contactPerson; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }

    // Setters
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setName(String name) { this.name = name; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + contactPerson + ")";
    }
} 