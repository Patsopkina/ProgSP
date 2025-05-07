package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.Serializable;
public class Diet implements Serializable  {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private List<MenuItem> menuItems;

    public Diet(String name) {
        this.name = name;
        this.menuItems = new ArrayList<>();
    }

    public Diet(int id, String name) {
        this.id = id;
        this.name = name;
        this.menuItems = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
    }

    public List<String> getMenuItemNames() {
        return menuItems.stream().map(MenuItem::getName).collect(Collectors.toList());
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItems = items;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}