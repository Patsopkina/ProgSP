package handlers;
import db.DataBase;
import session.SessionManager;
import java.io.*;
import java.sql.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EmployeeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");

        // Проверка роли сотрудника
        if (!isEmployee(token)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        switch (exchange.getRequestMethod()) {
            case "POST":
                if (path.contains("/createOrder")) {
                    createOrder(exchange);
                } else if (path.contains("/updateOrder")) {
                    updateOrder(exchange);
                } else if (path.contains("/register")) {
                    registerUser(exchange);
                } else if (path.contains("/updateCredentials")) {
                    updateCredentials(exchange);
                } else if (path.contains("/submitReview")) {
                    submitReview(exchange);
                } else if (path.contains("/setPreferences")) {
                    setPreferences(exchange);
                }
                break;

            case "GET":
                if (path.contains("/getMenu")) {
                    getMenu(exchange);
                } else if (path.contains("/getOrderHistory")) {
                    getOrderHistory(exchange);
                }
                break;

            default:
                sendResponse(exchange, 405, "Method Not Allowed");
                break;
        }
    }

    private boolean isEmployee(String token) {
        String username = SessionManager.getUsernameByToken(token);
        return username != null && "EMPLOYEE".equals(getUserRole(username));
    }

    private String getUserRole(String username) {
        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }
    private void registerUser(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        String[] params = body.split("&");
        String username = params[0].split("=")[1];
        String password = params[1].split("=")[1];

        try (Connection conn = DataBase.getConnection()) {
            // Проверка на уникальность логина
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                sendResponse(exchange, 400, "Username already exists.");
                return;
            }

            // Создание нового пользователя
            stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)"
            );
            stmt.setString(1, username);
            stmt.setString(2, password); // добавь хеширование!
            stmt.setString(3, "EMPLOYEE"); // Роль сотрудника
            stmt.executeUpdate();

            sendResponse(exchange, 200, "User registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during registration.");
        }
    }
    private void updateCredentials(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        String[] params = body.split("&");
        String newUsername = params[0].split("=")[1];
        String newPassword = params[1].split("=")[1];
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String username = SessionManager.getUsernameByToken(token);

        try (Connection conn = DataBase.getConnection()) {
            // Обновление логина и пароля
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ?, password = ? WHERE username = ?");
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword); // В реальной системе пароль должен быть зашифрован
            stmt.setString(3, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Credentials updated successfully.");
            } else {
                sendResponse(exchange, 400, "User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during credential update.");
        }
    }
    private void createOrder(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String username = SessionManager.getUsernameByToken(token);
        int userId = getUserIdByUsername(username);

        String body = getRequestBody(exchange);
        String[] items = body.split("&");

        try (Connection conn = DataBase.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Создаём заказ
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO orders (user_id, status) VALUES (?, 'pending')", Statement.RETURN_GENERATED_KEYS
            );
            stmt.setInt(1, userId);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int orderId = rs.getInt(1);

                // 2. Добавляем позиции в заказ
                PreparedStatement itemStmt = conn.prepareStatement(
                        "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)"
                );
                for (String item : items) {
                    String[] parts = item.split("=");
                    int menuItemId = Integer.parseInt(parts[0]);
                    int qty = Integer.parseInt(parts[1]);
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, menuItemId);
                    itemStmt.setInt(3, qty);
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            sendResponse(exchange, 200, "Order created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Error while creating order.");
        }
    }

    private void updateOrder(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        String[] params = body.split("&");
        int orderId = Integer.parseInt(params[0].split("=")[1]);
        int newMenuItemId = Integer.parseInt(params[1].split("=")[1]);

        try (Connection conn = DataBase.getConnection()) {
            // Обновление заказа
            PreparedStatement stmt = conn.prepareStatement("UPDATE orders SET menu_item_id = ? WHERE id = ?");
            stmt.setInt(1, newMenuItemId);
            stmt.setInt(2, orderId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Order updated successfully.");
            } else {
                sendResponse(exchange, 400, "Order not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during order update.");
        }
    }
    private void getOrderHistory(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String username = SessionManager.getUsernameByToken(token);
        int userId = getUserIdByUsername(username);

        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT o.id, o.status, o.order_time, mi.name, oi.quantity " +
                            "FROM orders o " +
                            "JOIN order_items oi ON o.id = oi.order_id " +
                            "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                            "WHERE o.user_id = ? ORDER BY o.order_time DESC"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder response = new StringBuilder();
            while (rs.next()) {
                response.append("Order ID: ").append(rs.getInt("id"))
                        .append(", Time: ").append(rs.getTimestamp("order_time"))
                        .append(", Dish: ").append(rs.getString("name"))
                        .append(", Qty: ").append(rs.getInt("quantity"))
                        .append(", Status: ").append(rs.getString("status")).append("\n");
            }

            sendResponse(exchange, 200, response.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during fetching order history.");
        }
    }
    private void getMenu(HttpExchange exchange) throws IOException {
        try (Connection conn = DataBase.getConnection()) {
            // Получение меню с сортировкой
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM menu_items ORDER BY name");
            ResultSet rs = stmt.executeQuery();

            StringBuilder response = new StringBuilder();
            while (rs.next()) {
                response.append("ID: ").append(rs.getInt("id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Price: ").append(rs.getDouble("price")).append("\n");
            }

            sendResponse(exchange, 200, response.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during fetching menu.");
        }
    }
    private void submitReview(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        String[] params = body.split("&");
        int menuItemId = Integer.parseInt(params[0].split("=")[1]);
        String review = params[1].split("=")[1];

        try (Connection conn = DataBase.getConnection()) {
            // Добавление отзыва
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO reviews (menu_item_id, review) VALUES (?, ?)");
            stmt.setInt(1, menuItemId);
            stmt.setString(2, review);
            stmt.executeUpdate();

            sendResponse(exchange, 200, "Review submitted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during review submission.");
        }
    }
    private void setPreferences(HttpExchange exchange) throws IOException {
        String body = getRequestBody(exchange);
        String[] params = body.split("&");
        String preference = params[0].split("=")[1];

        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String username = SessionManager.getUsernameByToken(token);

        try (Connection conn = DataBase.getConnection()) {
            // Установка предпочтений
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET preferences = ? WHERE username = ?");
            stmt.setString(1, preference);
            stmt.setString(2, username);
            stmt.executeUpdate();

            sendResponse(exchange, 200, "Preferences updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during updating preferences.");
        }
    }
    private void searchAndSortMenu(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery(); // Пример: ?search=pizza&sort=price
        String search = "";
        String sort = "name"; // default

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("search=")) search = param.split("=")[1];
                if (param.startsWith("sort=")) sort = param.split("=")[1];
            }
        }

        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM menu_items WHERE name LIKE ? ORDER BY " + sort
            );
            stmt.setString(1, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();

            StringBuilder response = new StringBuilder();
            while (rs.next()) {
                response.append("ID: ").append(rs.getInt("id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Price: ").append(rs.getBigDecimal("price"))
                        .append(", Available: ").append(rs.getBoolean("available")).append("\n");
            }

            sendResponse(exchange, 200, response.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Error fetching menu.");
        }
    }
    private int getUserIdByUsername(String username) {
        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    private String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
}