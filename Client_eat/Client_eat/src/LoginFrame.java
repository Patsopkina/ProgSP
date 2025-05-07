import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import db.DataBase;
import  dto.AuthResponse;
import  server.UserSender;

public class LoginFrame extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginFrame(DataBase dbManager) {
        setTitle("Авторизация");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // по центру экрана
        setLayout(new GridLayout(4, 1));

        loginField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Войти");
        registerButton = new JButton("Зарегистрироваться");

        add(new JLabel("Логин:"));
        add(loginField);
        add(new JLabel("Пароль:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> handleAuth("LOGIN", dbManager));
        registerButton.addActionListener(e -> handleAuth("REGISTER", dbManager));
    }

    private void handleAuth(String commandType, DataBase dbManager) {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Поля не должны быть пустыми!");
            return;
        }

        // Используем новую архитектуру
        AuthResponse response = UserSender.sendAuthRequest(commandType, login, password);

        if (response != null && response.isSuccess()) {
            String role = response.getRole();
            JOptionPane.showMessageDialog(this, response.getMessage());

            switch (role) {
                case "EMPLOYEE": new EmployeeFrame().setVisible(true); break;
                case "ADMIN": new AdminFrame().setVisible(true); break;
                case "MANAGER": new ManagerFrame().setVisible(true); break;
                default: JOptionPane.showMessageDialog(this, "Неизвестная роль: " + role);
            }

            this.dispose(); // Закрываем окно авторизации
        } else if (response != null) {
            JOptionPane.showMessageDialog(this, response.getMessage());
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка соединения.");
        }
    }

    public static void main(String[] args) {
        DataBase dbManager = new DataBase();
        SwingUtilities.invokeLater(() -> new LoginFrame(dbManager).setVisible(true));
    }
}
