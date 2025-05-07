import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public RegisterFrame() {
        setTitle("Регистрация");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"EMPLOYEE", "MANAGER", "ADMIN"});
        panel.add(roleBox);

        JButton registerButton = new JButton("Зарегистрироваться");
        registerButton.addActionListener(e -> attemptRegister());

        panel.add(new JLabel()); // Пустая ячейка
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }

    private void attemptRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Поля не должны быть пустыми.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "username=" + username + "&password=" + password + "&role=" + role;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                JOptionPane.showMessageDialog(this, "Регистрация успешна!");
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка регистрации. Код: " + responseCode);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка подключения.");
        }
    }
}
