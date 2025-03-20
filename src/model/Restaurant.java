package model;

public class Restaurant {
    private int restaurantID;
    private String name;
    private String location;
    private String cuisine;
    private double rating;

    public Restaurant(int restaurantID, String name, String location, String cuisine, double rating) {
        this.restaurantID = restaurantID;
        this.name = name;
        this.location = location;
        this.cuisine = cuisine;
        this.rating = rating;
    }

    // Getters and setters
    public int getRestaurantID() { return restaurantID; }
    public void setRestaurantID(int restaurantID) { this.restaurantID = restaurantID; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public String toString() {
        return name + " (" + cuisine + ") - " + location;
    }
} 