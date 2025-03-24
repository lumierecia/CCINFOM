package util;

import java.sql.SQLException;

public class DatabaseErrorHandler {
    public static String getUserFriendlyMessage(SQLException e) {
        String errorMessage = e.getMessage();
        
        // Handle unique constraint violations
        if (errorMessage.contains("Duplicate entry")) {
            if (errorMessage.contains("product_name")) {
                return "An item with this name already exists. Please use a different name.";
            } else if (errorMessage.contains("email")) {
                return "This email address is already registered. Please use a different email.";
            } else if (errorMessage.contains("phone")) {
                return "This phone number is already registered. Please use a different phone number.";
            }
        }
        
        // Handle check constraint violations
        if (errorMessage.contains("check constraint")) {
            if (errorMessage.contains("make_price")) {
                return "Make price must be greater than 0.";
            } else if (errorMessage.contains("sell_price")) {
                return "Sell price must be greater than 0.";
            } else if (errorMessage.contains("quantity")) {
                return "Quantity cannot be negative.";
            }
        }
        
        // Handle foreign key violations
        if (errorMessage.contains("foreign key constraint")) {
            if (errorMessage.contains("category_id")) {
                return "Invalid category selected. Please choose a valid category.";
            } else if (errorMessage.contains("employee_id")) {
                return "Invalid employee selected. Please choose a valid employee.";
            } else if (errorMessage.contains("unit_id")) {
                return "Invalid unit selected. Please choose a valid unit.";
            }
        }
        
        // Handle not null violations
        if (errorMessage.contains("cannot be null")) {
            if (errorMessage.contains("product_name")) {
                return "Product name is required.";
            } else if (errorMessage.contains("email")) {
                return "Email address is required.";
            } else if (errorMessage.contains("phone")) {
                return "Phone number is required.";
            }
        }
        
        // Default message for unknown errors
        return "An error occurred while processing your request. Please try again.";
    }
} 