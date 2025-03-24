package model;

import java.sql.Timestamp;

public class UserCredentials {
    private int userId;
    private int employeeId;
    private String username;
    private String passwordHash;
    private Timestamp lastLogin;
    private boolean isActive;
    private int roleId;

    public UserCredentials() {
    }

    public UserCredentials(int userId, int employeeId, String username, String passwordHash, Timestamp lastLogin, boolean isActive, int roleId) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
} 