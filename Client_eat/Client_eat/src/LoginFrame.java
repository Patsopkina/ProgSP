import db.DataBase;
import server.UserSender;
import javax.swing.*;
import java.awt.*;


public class LoginFrame extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginFrame (DataBase dbManager) {
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

        if (dbManager.isUserBlocked(login)) {
            JOptionPane.showMessageDialog(this, "Ваша учетная запись заблокирована. Обратитесь к администратору.");
            return;
        }

        String role = dbManager.getUserRoleByLogin(login);
        System.out.println(role);
        String command = commandType + ":" + login + ":" + password;
        String response = UserSender.sendCommand(command);
        System.out.println(command);
        System.out.println(response);

        if (response.startsWith("SUCCESS")) {
            JOptionPane.showMessageDialog(this, "Успешно!");
            switch (role) {
                case "EMPLOYEE":
                    new EmployeeFrame(login).setVisible(true);
                    break;
                case "ADMIN":
                    new AdminFrame().setVisible(true);
                    break;
                case "MANAGER":
                    new ManagerFrame().setVisible(true);
                    break;
                default: JOptionPane.showMessageDialog(this, "Неизвестная роль: " + role);
            }
            // Закрытие окна авторизации
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response);
        }
    }
    public static void main(String[] args) {
        DataBase dbManager = new DataBase();
        SwingUtilities.invokeLater(() -> new LoginFrame(dbManager).setVisible(true));
    }
}

