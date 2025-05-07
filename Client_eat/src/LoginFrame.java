import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 220);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        panel.add(loginButton);

        JButton registerButton = new JButton("Регистрация");
        registerButton.addActionListener(e -> new RegisterFrame());
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите логин и пароль.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String postData = "username=" + URLEncoder.encode(username, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }
                String response = responseBuilder.toString();

                String[] loginData = parseLoginResponse(response);
                if (loginData != null) {
                    String token = loginData[0];
                    String role = loginData[1];

                    this.dispose();
                    switch (role.toUpperCase()) {
                        case "ADMIN":
                            new AdminFrame(token);
                            break;
                        case "MANAGER":
                            new ManagerFrame(token);
                            break;
                        case "EMPLOYEE":
                            new EmployeeFrame(token);
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Неизвестная роль: " + role);
                            break;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Неверный формат ответа сервера: " + response);
                }
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                JOptionPane.showMessageDialog(this, "Неверные учетные данные.");
            } else {
                JOptionPane.showMessageDialog(this, "Login failed. Code: " + responseCode);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка подключения: " + ex.getMessage());
        }
    }

    private String[] parseLoginResponse(String json) {
        // Получаем значения токена и роли из JSON вручную
        String token = null;
        String role = null;

        try {
            int tokenStart = json.indexOf("\"token\":\"") + 9;
            int tokenEnd = json.indexOf("\"", tokenStart);
            if (tokenStart >= 0 && tokenEnd > tokenStart) {
                token = json.substring(tokenStart, tokenEnd);
            }

            int roleStart = json.indexOf("\"role\":\"") + 8;
            int roleEnd = json.indexOf("\"", roleStart);
            if (roleStart >= 0 && roleEnd > roleStart) {
                role = json.substring(roleStart, roleEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // В случае ошибки возвращаем null
        }

        if (token != null && role != null) {
            return new String[]{token, role};
        } else {
            return null;
        }
    }

}