package model;

import java.io.Serializable;

public class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double price;
    private boolean available;
    private String description;


    public MenuItem(int id, String name, String description, double price, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

    public MenuItem( String name, String description, double price, boolean available) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
