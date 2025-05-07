package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    private static final String URL = "jdbc:mysql://localhost:3306/corporate_catering";
    private static final String USER = "root";
    private static final String PASSWORD = "1234sql";

    private static Connection connection;

    // Метод для получения подключения
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    // Метод для подключения к БД
    private static void connect() throws SQLException {
        try {
            // Регистрируем драйвер
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Успешное подключение к базе данных!");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC драйвер не найден.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных.");
            e.printStackTrace();
            throw e;
        }
    }

    // Метод для закрытия подключения
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Подключение закрыто.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
