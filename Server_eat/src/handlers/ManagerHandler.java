package handlers;
import db.DataBase;
import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;
import session.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class ManagerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");

        // Проверка авторизации
        if (!isValidUser(token)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        switch (exchange.getRequestMethod()) {
            case "POST":
                if (path.contains("/updateMenu")) {
                    updateMenuItem(exchange);
                } else if (path.contains("/createSurvey")) {
                    createSurvey(exchange);
                } else if (path.contains("/createDiet")) {
                    createDiet(exchange);
                } else if (path.contains("/assignMenuToDiet")) {
                    assignMenuToDiet(exchange);
                } else if (path.contains("/addMenuItem")) {
                    addMenuItem(exchange);
                }
                break;

            case "GET":
                if (path.contains("/getMenu")) {
                    getMenuItems(exchange);
                } else if (path.contains("/getSurveys")) {
                    getSurveys(exchange);
                }
                break;
        }
    }

    private boolean isValidUser(String token) {
        return SessionManager.getUsernameByToken(token) != null;
    }


    private boolean isManager(String token) {
        String username = SessionManager.getUsernameByToken(token);
        return username != null && "MANAGER".equals(getUserRole(username));
    }

    private void updateMenuItem(HttpExchange exchange) throws IOException {
    InputStream is = exchange.getRequestBody();
    String body = new BufferedReader(new InputStreamReader(is))
            .lines().collect(Collectors.joining());

    String[] params = body.split("&");
    int id = Integer.parseInt(params[0].split("=")[1]);
    String name = params[1].split("=")[1];
    double price = Double.parseDouble(params[2].split("=")[1]);

    try (Connection conn = DataBase.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET name = ?, price = ? WHERE id = ?");
        stmt.setString(1, name);
        stmt.setDouble(2, price);
        stmt.setInt(3, id);
        int rowsUpdated = stmt.executeUpdate();

        if (rowsUpdated > 0) {
            sendResponse(exchange, 200, "Menu item updated successfully!");
        } else {
            sendResponse(exchange, 404, "Menu item not found.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sendResponse(exchange, 500, "Server error during updating menu item.");
    }
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
        return null;
    }
    private void createSurvey(HttpExchange exchange) throws IOException {
    InputStream is = exchange.getRequestBody();
    String body = new BufferedReader(new InputStreamReader(is))
            .lines().collect(Collectors.joining());

    String[] params = body.split("&");
    String question = params[0].split("=")[1];
    String[] options = params[1].split("=")[1].split(",");

    try (Connection conn = DataBase.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO surveys (question) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, question);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            int surveyId = rs.getInt(1);
            for (String option : options) {
                PreparedStatement optionStmt = conn.prepareStatement("INSERT INTO survey_options (survey_id, option) VALUES (?, ?)");
                optionStmt.setInt(1, surveyId);
                optionStmt.setString(2, option);
                optionStmt.executeUpdate();
            }
            sendResponse(exchange, 200, "Survey created successfully!");
        } else {
            sendResponse(exchange, 500, "Failed to create survey.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        sendResponse(exchange, 500, "Server error during creating survey.");
    }
}

    private void getSurveys(HttpExchange exchange) throws IOException {
    try (Connection conn = DataBase.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement("SELECT id, question FROM surveys");
        ResultSet rs = stmt.executeQuery();

        StringBuilder response = new StringBuilder();
        while (rs.next()) {
            response.append("Survey ID: ").append(rs.getInt("id"))
                    .append(", Question: ").append(rs.getString("question")).append("\n");
        }

        sendResponse(exchange, 200, response.toString());
    } catch (SQLException e) {
        e.printStackTrace();
        sendResponse(exchange, 500, "Server error during getting surveys.");
    }
}

    private void createDiet(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining());

        String[] params = body.split("&");
        String name = params[0].split("=")[1];
        String description = params[1].split("=")[1];

        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO diets (name, description) VALUES (?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();

            sendResponse(exchange, 200, "Diet created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during creating diet.");
        }
    }

    private void assignMenuToDiet(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining());

        String[] params = body.split("&");
        int dietId = Integer.parseInt(params[0].split("=")[1]);
        int menuItemId = Integer.parseInt(params[1].split("=")[1]);

        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO diet_menu (diet_id, menu_item_id) VALUES (?, ?)");
            stmt.setInt(1, dietId);
            stmt.setInt(2, menuItemId);
            stmt.executeUpdate();

            sendResponse(exchange, 200, "Menu item assigned to diet successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error during assigning menu item to diet.");
        }
    }

    private void addMenuItem(HttpExchange exchange) throws IOException {
        try {
            String body = getRequestBody(exchange);
            String[] params = body.split("&");

            String name = null;
            String description = null;
            double price = 0;

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length < 2) continue;
                String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");

                switch (key) {
                    case "name":
                        name = value;
                        break;
                    case "description":
                        description = value;
                        break;
                    case "price":
                        price = Double.parseDouble(value);
                        break;
                }
            }

            if (name == null || description == null) {
                sendResponse(exchange, 400, "Missing required parameters: name or description.");
                return;
            }

            try (Connection conn = DataBase.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO menu_items (name, description, price, available) VALUES (?, ?, ?, ?)"
                );
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setDouble(3, price);
                stmt.setBoolean(4, true);

                stmt.executeUpdate();

                sendResponse(exchange, 200, "Menu item added successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Database error while adding menu item.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid request parameters.");
        }
    }
    private void getMenuItems(HttpExchange exchange) throws IOException {
        try (Connection conn = DataBase.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, name, price FROM menu_items");
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
            sendResponse(exchange, 500, "Server error during getting menu items.");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
    }
    private String getRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}





