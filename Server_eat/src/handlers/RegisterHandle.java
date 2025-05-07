package handlers;
import db.DataBase;
import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegisterHandle implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Получаем данные из запроса
        InputStream is = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining());

        // Разбираем параметры из тела запроса (например, username, password, role)
        String[] params = body.split("&");
        String username = params[0].split("=")[1];
        String password = params[1].split("=")[1];
        String role = params[2].split("=")[1]; // Допустим, role может быть "ADMIN", "MANAGER", "EMPLOYEE"

        try (Connection conn = DataBase.getConnection()) {
            // Проверяем, существует ли уже такой пользователь
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                sendResponse(exchange, 400, "User already exists.");
            } else {
                // Добавляем нового пользователя
                stmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                stmt.setString(1, username);
                stmt.setString(2, password); // Здесь можно добавить шифрование пароля
                stmt.setString(3, role);
                stmt.executeUpdate();

                sendResponse(exchange, 200, "Registration successful!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during registration.");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
