package model;

import java.sql.Timestamp;
import java.util.List;

public class Order {

    private int id;
    private User user;  // Пользователь, который сделал заказ
    private List<OrderItem> orderedItems;  // Список заказанных блюд с количеством
    private String status;  // Статус заказа
    private Timestamp orderTime;  // Время оформления заказа (Timestamp)
    private Timestamp preparationTime;  // Время на приготовление (Timestamp)

    public Order() {}

    public Order(User user, List<OrderItem> orderedItems, Timestamp preparationTime) {
        this.user = user;
        this.orderedItems = orderedItems;
        this.status = "Pending";
        this.orderTime = new Timestamp(System.currentTimeMillis());  // Устанавливаем текущее время оформления
        this.preparationTime = preparationTime;  // Время на приготовление
    }
    public int getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public List<OrderItem> getOrderedItems() {
        return orderedItems;
    }
    public String getStatus() {
        return status;
    }
    public Timestamp getOrderTime() {
        return orderTime;
    }
    public Timestamp getPreparationTime() {
        return preparationTime;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOrderedItems(List<OrderItem> orderedItems) {
        this.orderedItems = orderedItems;
    }
    // Метод для установки времени оформления заказа
    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    // Метод для установки времени на приготовление
    public void setPreparationTime(Timestamp preparationTime) {
        this.preparationTime = preparationTime;
    }
}
