package dao;

import model.Restaurant;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    private Connection connection;

    public RestaurantDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT * FROM restaurants";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Restaurant restaurant = new Restaurant(
                    rs.getInt("restaurant_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("cuisine"),
                    rs.getDouble("rating")
                );
                restaurants.add(restaurant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return restaurants;
    }

    public Restaurant getRestaurantById(int id) {
        String query = "SELECT * FROM restaurants WHERE restaurant_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Restaurant(
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("cuisine"),
                        rs.getDouble("rating")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRestaurant(Restaurant restaurant) {
        String query = "INSERT INTO restaurants (name, location, cuisine, rating) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, restaurant.getName());
            pstmt.setString(2, restaurant.getLocation());
            pstmt.setString(3, restaurant.getCuisine());
            pstmt.setDouble(4, restaurant.getRating());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRestaurant(Restaurant restaurant) {
        String query = "UPDATE restaurants SET name = ?, location = ?, cuisine = ?, rating = ? WHERE restaurant_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, restaurant.getName());
            pstmt.setString(2, restaurant.getLocation());
            pstmt.setString(3, restaurant.getCuisine());
            pstmt.setDouble(4, restaurant.getRating());
            pstmt.setInt(5, restaurant.getRestaurantID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRestaurant(int id) {
        String query = "DELETE FROM restaurants WHERE restaurant_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Restaurant> searchRestaurants(String searchTerm) {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT * FROM restaurants WHERE name LIKE ? OR location LIKE ? OR cuisine LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant restaurant = new Restaurant(
                        rs.getInt("restaurant_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("cuisine"),
                        rs.getDouble("rating")
                    );
                    restaurants.add(restaurant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return restaurants;
    }
} 