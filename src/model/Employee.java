package model;

import java.sql.Time;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private String shiftType;
    private Time shiftStart;
    private Time shiftEnd;
    private int timeShiftId;
    private String roleName;
    private int roleId;
    private boolean isDeleted;

    public Employee() {
    }

    public Employee(int employeeId, String firstName, String lastName, String role) {
        this(employeeId, firstName, lastName, role, "Active");
    }

    public Employee(int employeeId, String firstName, String lastName, String role, String status) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
    }

    public Employee(int employeeId, String firstName, String lastName, int roleId) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roleId = roleId;
    }

    // Getters
    public int getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getShiftType() { return shiftType; }
    public Time getShiftStart() { return shiftStart; }
    public Time getShiftEnd() { return shiftEnd; }
    public int getTimeShiftId() { return timeShiftId; }
    public String getRoleName() { return roleName; }
    public int getRoleId() { return roleId; }
    public boolean isDeleted() { return isDeleted; }

    // Setters
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }
    public void setShiftStart(Time shiftStart) { this.shiftStart = shiftStart; }
    public void setShiftEnd(Time shiftEnd) { this.shiftEnd = shiftEnd; }
    public void setTimeShiftId(int timeShiftId) { this.timeShiftId = timeShiftId; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}