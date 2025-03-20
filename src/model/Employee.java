package model;

import java.sql.Time;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String role;
    private String phone;
    private String status;
    private String shiftType;
    private Time shiftStart;
    private Time shiftEnd;

    public Employee() {
    }

    public Employee(int employeeId, String firstName, String lastName, String role) {
        this(employeeId, firstName, lastName, role, "", "Active");
    }

    public Employee(int employeeId, String firstName, String lastName, String role, String phone, String status) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.phone = phone;
        this.status = status;
    }

    // Getters
    public int getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public String getShiftType() { return shiftType; }
    public Time getShiftStart() { return shiftStart; }
    public Time getShiftEnd() { return shiftEnd; }

    // Setters
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStatus(String status) { this.status = status; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }
    public void setShiftStart(Time shiftStart) { this.shiftStart = shiftStart; }
    public void setShiftEnd(Time shiftEnd) { this.shiftEnd = shiftEnd; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
} 