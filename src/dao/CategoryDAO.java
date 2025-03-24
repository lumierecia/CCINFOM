package dao;

import model.Category;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public boolean createCategory(String name, String description) throws SQLException {
        String query = "INSERT INTO Categories (category_name, description) VALUES (?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateCategory(int categoryId, String name, String description) throws SQLException {
        String query = "UPDATE Categories SET category_name = ?, description = ? WHERE category_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        String query = "DELETE FROM Categories WHERE category_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        String query = "SELECT * FROM Categories WHERE category_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_deleted")
                    );
                }
            }
        }
        return null;
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM Categories ORDER BY category_name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_deleted")
                ));
            }
        }
        return categories;
    }

    public List<Category> getCategoriesByDishId(int dishId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = """
            SELECT c.* FROM Categories c
            JOIN Dishes d ON c.category_id = d.category_id
            WHERE d.dish_id = ? AND c.is_deleted = FALSE
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_deleted")
                    ));
                }
            }
        }
        return categories;
    }
} 