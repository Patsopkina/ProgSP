package model;

import java.util.ArrayList;
import java.util.List;

public class OrderHistory {

    private List<Order> orders;  // Список заказов

    public OrderHistory(List<Order> orders) {
        this.orders = new ArrayList<>();
    }

    // Метод для добавления заказа в историю
    public void addOrder(Order order) {
        orders.add(order);
    }

    // Метод для получения всех заказов
    public List<Order> getOrders() {
        return orders;
    }

    // Метод для отображения истории заказов в строковом формате
    public String displayOrders() {
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
            sb.append("Заказ №").append(order.getId()).append("\n");  // Используем метод getId
            sb.append("Статус: ").append(order.getStatus()).append("\n");
            sb.append("Дата: ").append(order.getOrderTime()).append("\n");
            sb.append("Время приготовления: ").append(order.getPreparationTime()).append("\n");
            sb.append("Заказанные блюда:\n");
            for (OrderItem item : order.getOrderedItems()) {
                sb.append("- ").append(item.getMenuItem().getName()).append(" (x").append(item.getQuantity()).append(")\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
