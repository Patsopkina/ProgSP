package db;
import  model.Diet;
import model.User;
import  model.MenuItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DataBase {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "corporate_catering";
    private static final String LOGIN = "root";
    private static final String PASSWORD = "1234sql";


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

    public void setUserBlocked(int userId, int isBlocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE id = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, isBlocked);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("Статус блокировки изменён для пользователя с ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public void insertUserWithRole(String login, String password, String role) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.out.println("Ошибка хеширования пароля.");
            return;
        }

        String sql = "INSERT INTO users (login, password_hash, role, is_blocked) VALUES (?, ?, ?, ?)";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, role);
            prSt.setInt(4, 0);  // По умолчанию пользователь не заблокирован

            prSt.executeUpdate();
            System.out.println("Пользователь успешно добавлен: " + login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT id, login, password_hash, role, is_blocked FROM users";
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
    public boolean setUserBlocked(String login, int blocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, blocked);
            stmt.setString(2, login);
            stmt.executeUpdate();
            System.out.println("Статус блокировки изменён для пользователя: " + login);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
}