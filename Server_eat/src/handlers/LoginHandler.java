package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DataBase;
import session.SessionManager;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendJson(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        // Читаем тело запроса
        String body;
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            body = reader.lines().collect(Collectors.joining());
        }
        System.out.println("Body: " + body);  // для отладки

        // Разбираем параметры
        String[] params = body.split("&");
        String username = "";
        String password = "";

        for (String param : params) {
            int idx = param.indexOf('=');
            if (idx != -1) {
                String key = URLDecoder.decode(param.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(param.substring(idx + 1), StandardCharsets.UTF_8);

                if (key.equals("username")) {
                    username = value;
                } else if (key.equals("password")) {
                    password = value;
                }
            }
        }

        // Проверяем пользователя в БД
        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT role FROM users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String token = SessionManager.createSession(username);

                // Отправляем JSON-ответ с токеном и ролью
                String jsonResponse = String.format("{ \"token\": \"%s\", \"role\": \"%s\" }", token, role);
                sendJson(exchange, 200, jsonResponse);
            } else {
                sendJson(exchange, 401, "{\"error\": \"Invalid credentials\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, "{\"error\": \"Server error\"}");
        }
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
