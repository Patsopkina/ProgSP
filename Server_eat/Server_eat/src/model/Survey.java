package model;

import java.util.ArrayList;
import java.util.List;

public class Survey {
    private int id;
    private String name;
    private List<MenuItem> items; // Список блюд, связанных с опросом

    public Survey() {
        this.items = new ArrayList<>();
    }

    public Survey(int id, String name) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    // Добавление блюда в опрос
    public void addItem(MenuItem item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    // Удаление блюда из опроса
    public void removeItem(MenuItem item) {
        items.remove(item);
    }


}
