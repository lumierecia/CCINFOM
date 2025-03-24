package controller;

import dao.EmployeeDAO;
import model.Employee;

import java.sql.SQLException;
import java.util.List;

public class EmployeeController {
    private EmployeeDAO employeeDAO;

    public EmployeeController() {
        try {
            this.employeeDAO = new EmployeeDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addEmployee(String firstName, String lastName, int roleId, int timeShiftId) {
        if (firstName == null || lastName == null || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            return false;
        }

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setRoleId(roleId);
        employee.setTimeShiftId(timeShiftId);

        return employeeDAO.addEmployee(employee);
    }

    public boolean updateEmployee(int employeeId, String firstName, String lastName, int roleId, int timeShiftId) {
        if (firstName == null || lastName == null || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            return false;
        }

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setRoleId(roleId);
        employee.setTimeShiftId(timeShiftId);

        return employeeDAO.updateEmployee(employee);
    }

    public boolean deleteEmployee(int employeeId) {
        return employeeDAO.deleteEmployee(employeeId);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeById(int employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public boolean isEmployeeOnShift(int employeeId) {
        // Get the employee's current shift status from the database
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }
        return employee.getTimeShiftId() > 0;
    }

    public boolean recordShift(int employeeId, int timeShiftId, String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Update employee's shift information in the database
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }
        employee.setTimeShiftId(timeShiftId);
        return employeeDAO.updateEmployee(employee);
    }

    public boolean canEmployeeTakeOrder(int employeeId) {
        return isEmployeeOnShift(employeeId);
    }
} 