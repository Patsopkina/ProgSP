import javax.swing.*;

public class AdminFrame extends JFrame {
    public AdminFrame(String token) {
        setTitle("Admin Panel");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Welcome, Admin!");
        add(label);
        setVisible(true);
    }
}
