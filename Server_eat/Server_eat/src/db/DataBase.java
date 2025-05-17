package db;
import  model.*;
import session.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DataBase {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "corporate_catering";
    private static final String LOGIN = "root";
    private static final String PASSWORD = "1234sql";
    private static DataBase instance;
    public static synchronized DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    // Метод для получения соединения с базой данных
    public Connection getDbConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
        return DriverManager.getConnection(url, LOGIN, PASSWORD);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println(digest);
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean checkPassword(String inputPassword, String storedHash) {
        String hashedInputPassword = hashPassword(inputPassword);
        return hashedInputPassword != null && hashedInputPassword.equals(storedHash);
    }

    public boolean isUserBlocked(String login) {
        String sql = "SELECT is_blocked FROM users WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_blocked") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // по умолчанию считаем, что не заблокирован
    }


    public boolean doesLoginExist(String login) {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Метод для вставки пользователя в базу данных
    public void insertUserWithRole(String login, String password, String role) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.out.println("Ошибка хеширования пароля.");
            return;
        }

        String sql = "INSERT INTO `users` (login, password_hash, role, is_blocked) VALUES (?, ?, ?, ?)";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, role);
            prSt.setInt(4, 0);

            prSt.executeUpdate();
            System.out.println("Пользователь успешно добавлен: " + login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения всех пользователей из базы данных
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT id, login, password_hash, role, is_blocked FROM `users`";

        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql);
             ResultSet resultSet = prSt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String login = resultSet.getString("login");
                String role = resultSet.getString("role");
                boolean isBlocked = resultSet.getBoolean("is_blocked");
                users.add(new User(id, login, role, isBlocked));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public User getUserByLogin(String login) {
        String query = "SELECT id, login, role, is_blocked FROM users WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    boolean isBlocked = rs.getBoolean("is_blocked");
                    return new User(id, login, role, isBlocked);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Метод для вывода всех пользователей
    public void displayUsers() {
        ArrayList<User> users = getUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей для отображения.");
        } else {
            System.out.println("Список пользователей:");
            for (User user : users) {
                System.out.println("- " + user);
            }
        }
    }

    public String getUserRoleByLogin(String login) {
        String role = "EMPLOYEE";
        String query = "SELECT role FROM users WHERE login = ?";


        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметр в запросе
            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }

    public void updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, newRole);
            prSt.setInt(2, userId);
            prSt.executeUpdate();
            System.out.println("Роль пользователя обновлена. ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserById(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setInt(1, userId);
            prSt.executeUpdate();
            System.out.println("Пользователь удалён. ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserBlocked(String login, int blocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE login = ?";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, blocked);
            stmt.setString(2, login);
            stmt.executeUpdate();
            System.out.println("Статус блокировки изменён для пользователя: " + login);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTableData(JTable table, String sqlQuery) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // очищаем старые данные

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = stmt.executeQuery()) {

            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка загрузки данных: " + e.getMessage());
        }
    }

    public ArrayList<MenuItem> getAllMenuItems() {
        ArrayList<MenuItem> items =   new ArrayList<>();
        String sql = "SELECT * FROM menu_items";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                double price = rs.getDouble("price");
                boolean available = rs.getBoolean("available");
                items.add(new MenuItem(id, name, desc, price, available));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
    public boolean insertMenuItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (name, description, price, available, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isAvailable());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1)); // сохранить ID обратно
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE menu_items SET name = ?, description = ?, price = ?, available = ? WHERE id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isAvailable());
            stmt.setInt(5, item.getId());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteMenuItemById(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Сохранение рациона с его блюдами
    public boolean saveDietWithItems(Diet diet) {
        String insertDietSQL = "INSERT INTO diets (name) VALUES (?)";
        String insertDietItemSQL = "INSERT INTO diet_items (diet_id, menu_item_id) VALUES (?, ?)";
        String insertMenuItemSQL = "INSERT INTO menu_items (name, description, price, available) VALUES (?, ?, ?, ?)";

        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);

            // Вставка диеты
            int dietId;
            try (PreparedStatement ps = conn.prepareStatement(insertDietSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, diet.getName());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        dietId = rs.getInt(1);
                        diet.setId(dietId); // сохранить id обратно в объект
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Обработка каждого блюда
            for (MenuItem item : diet.getMenuItems()) {
                int menuItemId = item.getId();

                if (menuItemId <= 0) {
                    // Новое блюдо — вставить в menu_items
                    try (PreparedStatement ps = conn.prepareStatement(insertMenuItemSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, item.getName());
                        ps.setString(2, item.getDescription());
                        ps.setDouble(3, item.getPrice());
                        ps.setBoolean(4, item.isAvailable());
                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                menuItemId = rs.getInt(1);
                                item.setId(menuItemId); // сохранить новый ID в объект
                            } else {
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }

                // Связь диеты с блюдом
                try (PreparedStatement ps = conn.prepareStatement(insertDietItemSQL)) {
                    ps.setInt(1, dietId);
                    ps.setInt(2, menuItemId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public ArrayList<Diet> getAllDiets() {
        ArrayList<Diet> diets = new ArrayList<>();
        String sql = "SELECT d.id AS diet_id, d.name AS diet_name, " +
                "mi.id AS menu_item_id, mi.name AS item_name, " +
                "mi.description, mi.price, mi.available " +
                "FROM diets d " +
                "LEFT JOIN diet_items di ON d.id = di.diet_id " +
                "LEFT JOIN menu_items mi ON di.menu_item_id = mi.id";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int dietId = rs.getInt("diet_id");
                String dietName = rs.getString("diet_name");

                // Поиск уже добавленного рациона
                Diet existingDiet = null;
                for (Diet d : diets) {
                    if (d.getId() == dietId) {
                        existingDiet = d;
                        break;
                    }
                }

                // Если нет, добавляем новый
                if (existingDiet == null) {
                    existingDiet = new Diet(dietId, dietName);
                    diets.add(existingDiet);
                }

                // Если есть блюдо — добавляем в рацион
                int menuItemId = rs.getInt("menu_item_id");
                if (!rs.wasNull()) {
                    String itemName = rs.getString("item_name");
                    String description = rs.getString("description");
                    double price = rs.getDouble("price");
                    boolean available = rs.getBoolean("available");

                    MenuItem item = new MenuItem(menuItemId, itemName, description, price, available);
                    existingDiet.addMenuItem(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return diets;
    }

    public boolean deleteDiet(int id) {
        String deleteDietItemsSQL = "DELETE FROM diet_items WHERE diet_id = ?";
        String deleteDietSQL = "DELETE FROM diets WHERE id = ?";

        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(deleteDietItemsSQL);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteDietSQL)) {

                stmt1.setInt(1, id);
                stmt1.executeUpdate();

                stmt2.setInt(1, id);
                int affectedRows = stmt2.executeUpdate();

                conn.commit();
                return affectedRows > 0;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean updateDiet(Diet diet) {
        String updateDietSQL = "UPDATE diets SET name = ? WHERE id = ?";
        String deleteOldItemsSQL = "DELETE FROM diet_items WHERE diet_id = ?";
        String insertItemSQL = "INSERT INTO diet_items (diet_id, menu_item_id) VALUES (?, ?)";

        try (Connection conn = getDbConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateDietSQL);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteOldItemsSQL);
             PreparedStatement insertStmt = conn.prepareStatement(insertItemSQL)) {

            conn.setAutoCommit(false); // Начинаем транзакцию

            // Обновляем имя диеты
            updateStmt.setString(1, diet.getName());
            updateStmt.setInt(2, diet.getId());
            updateStmt.executeUpdate();

            // Удаляем старые блюда из рациона
            deleteStmt.setInt(1, diet.getId());
            deleteStmt.executeUpdate();

            // Добавляем новые блюда
            for (MenuItem item : diet.getMenuItems()) {
                insertStmt.setInt(1, diet.getId());
                insertStmt.setInt(2, item.getId());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();

            conn.commit(); // Подтверждаем транзакцию
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isKitchenOpen() {
        String sql = "SELECT is_open FROM kitchen_status WHERE id = 1";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean("is_open");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // по умолчанию считаем, что кухня открыта
    }

    public void updateKitchenStatus(boolean isOpen) {
        String sql = "UPDATE kitchen_status SET is_open = ? WHERE id = 1";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isOpen);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean saveSurvey(ArrayList<MenuItem> selectedItems) {
        String insertSurveySql = "INSERT INTO surveys () VALUES ()";
        String insertSurveyItemSql = "INSERT INTO survey_items (survey_id, menu_item_id) VALUES (?, ?)";

        try (Connection conn = getDbConnection();
             PreparedStatement surveyStmt = conn.prepareStatement(insertSurveySql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // 1. Вставляем запись опроса
            int surveyId = -1;
            surveyStmt.executeUpdate();

            try (ResultSet generatedKeys = surveyStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    surveyId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Ошибка получения ID опроса.");
                }
            }

            // 2. Вставляем все блюда опроса
            try (PreparedStatement itemStmt = conn.prepareStatement(insertSurveyItemSql)) {
                for (MenuItem item : selectedItems) {
                    itemStmt.setInt(1, surveyId);
                    itemStmt.setInt(2, item.getId());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean saveSurveyWithName(String name, ArrayList<MenuItem> items) {
        String insertSurvey = "INSERT INTO surveys (name) VALUES (?)";
        String insertSurveyItem = "INSERT INTO survey_items (survey_id, menu_item_id) VALUES (?, ?)";

        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement psSurvey = conn.prepareStatement(insertSurvey, PreparedStatement.RETURN_GENERATED_KEYS);
            psSurvey.setString(1, name);
            psSurvey.executeUpdate();
            ResultSet rs = psSurvey.getGeneratedKeys();
            if (rs.next()) {
                int surveyId = rs.getInt(1);
                PreparedStatement psItem = conn.prepareStatement(insertSurveyItem);
                for (MenuItem item : items) {
                    psItem.setInt(1, surveyId);
                    psItem.setInt(2, item.getId());
                    psItem.addBatch();
                }
                psItem.executeBatch();
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Survey> getAllSurveys() {
        ArrayList<Survey> surveys = new ArrayList<>();
        String query = "SELECT id, name FROM surveys";
        try (Connection conn = getDbConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                surveys.add(new Survey(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return surveys;
    }

    public ArrayList<MenuItem> getSurveyItemsBySurveyId(int surveyId) {
        ArrayList<MenuItem> items = new ArrayList<>();
        String query = "SELECT m.id, m.name, m.description, m.price, m.available FROM menu_items m " +
                "JOIN survey_items si ON m.id = si.menu_item_id WHERE si.survey_id = ?";
        try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, surveyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setPrice(rs.getDouble("price"));
                item.setAvailable(rs.getBoolean("available"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void updateSurveyName(int surveyId, String newName) {
        String update = "UPDATE surveys SET name = ? WHERE id = ?";
        try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, newName);
            ps.setInt(2, surveyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeItemFromSurvey(int surveyId, int menuItemId) {
        String delete = "DELETE FROM survey_items WHERE survey_id = ? AND menu_item_id = ?";
        try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(delete)) {
            ps.setInt(1, surveyId);
            ps.setInt(2, menuItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addItemToSurvey(int surveyId, int menuItemId) {
        String insert = "INSERT IGNORE INTO survey_items (survey_id, menu_item_id) VALUES (?, ?)";
        try (Connection conn = getDbConnection(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, surveyId);
            ps.setInt(2, menuItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean deleteSurvey(int surveyId) {
        String deleteItemsSQL = "DELETE FROM survey_items WHERE survey_id = ?";
        String deleteSurveySQL = "DELETE FROM surveys WHERE id = ?";

        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false); // Начинаем транзакцию

            // 1. Удаляем связанные записи из survey_items
            try (PreparedStatement psItems = conn.prepareStatement(deleteItemsSQL)) {
                psItems.setInt(1, surveyId);
                psItems.executeUpdate();
            }

            // 2. Удаляем сам опрос
            try (PreparedStatement psSurvey = conn.prepareStatement(deleteSurveySQL)) {
                psSurvey.setInt(1, surveyId);
                int affectedRows = psSurvey.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false; // Не нашли опрос для удаления
                }
            }

            conn.commit(); // Фиксируем изменения
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertSurvey(String surveyName, ArrayList<MenuItem> selectedItems) {
        String insertSurveySQL = "INSERT INTO surveys (name) VALUES (?)";
        String insertItemsSQL = "INSERT INTO survey_items (survey_id, menu_item_id) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement psSurvey = null;
        PreparedStatement psItems = null;

        try {
            conn = getDbConnection();
            conn.setAutoCommit(false); // Начинаем транзакцию

            // 1. Вставляем новый опрос
            psSurvey = conn.prepareStatement(insertSurveySQL, Statement.RETURN_GENERATED_KEYS);
            psSurvey.setString(1, surveyName);
            psSurvey.executeUpdate();

            // 2. Получаем ID созданного опроса
            int surveyId;
            try (ResultSet generatedKeys = psSurvey.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    surveyId = generatedKeys.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Добавляем связанные блюда
            psItems = conn.prepareStatement(insertItemsSQL);
            for (MenuItem item : selectedItems) {
                psItems.setInt(1, surveyId);
                psItems.setInt(2, item.getId());
                psItems.addBatch(); // Добавляем в пакет
            }
            psItems.executeBatch(); // Выполняем пакетную вставку

            conn.commit(); // Фиксируем транзакцию
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Откатываем при ошибке
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            // Закрываем ресурсы
            try {
                if (psItems != null) psItems.close();
                if (psSurvey != null) psSurvey.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean updateSurvey(int surveyId, String surveyName, ArrayList<MenuItem> items) {
        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false); // Начинаем транзакцию

            // 1. Обновляем название опроса
            String updateQuery = "UPDATE surveys SET name = ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, surveyName);
                updateStmt.setInt(2, surveyId);
                int rowsUpdated = updateStmt.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Если переданы блюда, обновляем их список
            if (items != null) {
                // Удаляем старые связи
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM survey_items WHERE survey_id = ?")) {
                    deleteStmt.setInt(1, surveyId);
                    deleteStmt.executeUpdate();
                }

                // Добавляем новые связи
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO survey_items (survey_id, menu_item_id) VALUES (?, ?)")) {
                    for (MenuItem item : items) {
                        insertStmt.setInt(1, surveyId);
                        insertStmt.setInt(2, item.getId());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            conn.commit(); // Фиксируем транзакцию
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void submitFeedback(String login, String message) {
        String sql = "INSERT INTO feedback (employee_login, message, timestamp) VALUES (?, ?, NOW())";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, message);
            stmt.executeUpdate();
            System.out.println("Отзыв успешно добавлен.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Feedback> getAllFeedback() {
        ArrayList<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT * FROM feedback";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String employeeLogin = rs.getString("employee_login");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                // Преобразуем Timestamp в LocalDateTime
                LocalDateTime dateTime = timestamp.toLocalDateTime();

                Feedback feedback = new Feedback(id, employeeLogin, message, dateTime);
                feedbackList.add(feedback);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении отзывов: " + e.getMessage());
            e.printStackTrace();
        }

        return feedbackList;
    }
    public boolean updateEmployeeCredentials(String currentLogin, String newLogin, String newPassword, String sessionToken) {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        ArrayList<String> updates = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();

        if (!newLogin.isEmpty()) {
            updates.add("login = ?");
            values.add(newLogin);
        }
        if (!newPassword.isEmpty()) {
            updates.add("password_hash = ?");
            values.add(hashPassword(newPassword)); // Хешируем новый пароль
        }

        if (updates.isEmpty()) return false;

        sql.append(String.join(", ", updates));
        sql.append(" WHERE login = ?");
        values.add(currentLogin);

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Если логин был изменен, обновляем сессию
                if (!newLogin.isEmpty()) {
                    SessionManager.updateSessionLogin(sessionToken, newLogin); // Обновляем логин в сессии
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void saveOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, status, order_time) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getDbConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUser().getId());
            stmt.setString(2, order.getStatus());
            stmt.setTimestamp(3, order.getOrderTime()); // без new Timestamp(...)

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                order.setId(generatedKeys.getInt(1));
            }

            // Сохраняем позиции заказа
            saveOrderItems(order);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для сохранения позиций заказа (order_items)
    private void saveOrderItems(Order order) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getDbConnection().prepareStatement(sql)) {
            for (OrderItem orderItem : order.getOrderedItems()) {
                stmt.setInt(1, order.getId());
                stmt.setInt(2, orderItem.getMenuItem().getId());
                stmt.setInt(3, orderItem.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для создания заказа, возвращает ID
    public int createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, status) VALUES (?, ?)";

        try (PreparedStatement stmt = getDbConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUser().getId());
            stmt.setString(2, "PENDING");
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Не удалось создать заказ, ID не был получен.");
            }
        }
    }

    // Метод для добавления блюд к заказу
    public void addOrderItems(int orderId, ArrayList<MenuItem> orderedItems) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getDbConnection().prepareStatement(sql)) {
            for (MenuItem item : orderedItems) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getId());
                stmt.setInt(3, 1); // фиксируем количество = 1
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    public OrderHistory getOrderHistoryByUser(User user) {
        ArrayList<Order> orders = new ArrayList<>();

        String query = "SELECT o.id, o.status, o.order_time, oi.menu_item_id, oi.quantity " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.user_id = ? " +
                "ORDER BY o.order_time DESC";  // Получаем заказы пользователя, отсортированные по времени

        try (PreparedStatement stmt = getDbConnection().prepareStatement(query)) {
            stmt.setInt(1, user.getId());  // Устанавливаем id пользователя в запрос

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Для каждого результата создаём объект Order и добавляем его в список
                int orderId = rs.getInt("id");
                String status = rs.getString("status");
                Timestamp orderTime = rs.getTimestamp("order_time");


                // Создаем заказ
                Order order = new Order();
                order.setId(orderId);
                order.setStatus(status);
                order.setOrderTime(orderTime);


                // Список заказанных блюд
                ArrayList<OrderItem> orderItems = new ArrayList<>();

                // Получаем все позиции в заказе
                do {
                    int menuItemId = rs.getInt("menu_item_id");
                    int quantity = rs.getInt("quantity");

                    // Создаем объект OrderItem
                    OrderItem orderItem = new OrderItem();
                    orderItem.setMenuItem(new MenuItem(menuItemId));  // Передаем id блюда в конструктор MenuItem
                    orderItem.setQuantity(quantity);

                    orderItems.add(orderItem);
                } while (rs.next() && rs.getInt("id") == orderId);  // Переходим к следующей строке с тем же заказом

                order.setOrderedItems(orderItems);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Возвращаем объект OrderHistory с найденными заказами
        return new OrderHistory(orders);
    }


}
