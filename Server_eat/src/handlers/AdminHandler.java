package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DataBase;

import java.io.*;
import java.net.URI;
import java.sql.*;
import java.util.*;

public class AdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            handleGet(exchange);
        } else if ("POST".equals(method)) {
            handlePost(exchange);
        } else if ("DELETE".equals(method)) {
            handleDelete(exchange);
        } else {
            sendError(exchange, "Метод не поддерживается.");
        }
    }

    // 1. Обработка GET-запросов для поиска и сортировки
    private void handleGet(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = queryToMap(uri.getQuery());

        String type = params.get("type"); // users or menu
        String sortBy = params.getOrDefault("sortBy", "id");
        String filter = params.get("filter"); // пример: role=employee

        StringBuilder response = new StringBuilder();

        try (Connection conn = DataBase.getConnection()) {
            if ("users".equalsIgnoreCase(type)) {
                String query = "SELECT id, username, role, is_blocked FROM users";
                if (filter != null) {
                    String[] f = filter.split("=");
                    query += " WHERE " + f[0] + "='" + f[1] + "'";
                }
                query += " ORDER BY " + sortBy;

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    response.append("ID: ").append(rs.getInt("id"))
                            .append(", Username: ").append(rs.getString("username"))
                            .append(", Role: ").append(rs.getString("role"))
                            .append(", Blocked: ").append(rs.getBoolean("is_blocked"))
                            .append("\n");
                }
            } else if ("menu".equalsIgnoreCase(type)) {
                String query = "SELECT id, name, price FROM menu ORDER BY " + sortBy;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    response.append("ID: ").append(rs.getInt("id"))
                            .append(", Name: ").append(rs.getString("name"))
                            .append(", Price: ").append(rs.getDouble("price"))
                            .append("\n");
                }
            } else if ("report".equals(type)) {
                String query = "SELECT u.username, o.date, o.details FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.date DESC";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    response.append("User: ").append(rs.getString("username"))
                            .append(", Date: ").append(rs.getString("date"))
                            .append(", Order: ").append(rs.getString("details")).append("\n");
                }
            } else {
                response.append("Тип не указан или некорректный.");
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();

        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, "Ошибка базы данных.");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) map.put(parts[0], parts[1]);
        }
        return map;
    }

    private void sendError(HttpExchange exchange, String message) throws IOException {
        exchange.sendResponseHeaders(500, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }


    private void handlePost(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = queryToMap(new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().reduce("", (acc, line) -> acc + line));

        String action = params.get("action");

        try (Connection conn = DataBase.getConnection()) {
            if ("assign_manager".equals(action)) {
                int userId = Integer.parseInt(params.get("userId"));
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET role = 'manager' WHERE id = ?");
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();

                sendTextResponse(exchange, rows > 0 ? "Назначение успешно." : "Пользователь не найден.");
            } else if ("block_user".equals(action)) {
                int userId = Integer.parseInt(params.get("userId"));
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET is_blocked = true WHERE id = ?");
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();

                sendTextResponse(exchange, rows > 0 ? "Пользователь заблокирован." : "Ошибка при блокировке.");
            } else if ("notify_closure".equals(action)) {
                String message = params.get("message");
                PreparedStatement ps = conn.prepareStatement("INSERT INTO notifications (message, date) VALUES (?, NOW())");
                ps.setString(1, message);
                ps.executeUpdate();

                sendTextResponse(exchange, "Уведомление добавлено.");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, "Ошибка базы данных.");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().reduce("", (acc, line) -> acc + line));

        try (Connection conn = DataBase.getConnection()) {
            int userId = Integer.parseInt(params.get("userId"));
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            sendTextResponse(exchange, rows > 0 ? "Пользователь удалён." : "Ошибка удаления.");

        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, "Ошибка базы данных.");
        }
    }

    private void sendTextResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

