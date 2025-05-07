import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeFrame extends JFrame {
    private final String token;

    public EmployeeFrame(String token) {
        this.token = token;
        setTitle("Меню Сотрудника");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton viewMenuButton = new JButton("Просмотреть меню");
        JButton placeOrderButton = new JButton("Сделать заказ");
        JButton viewOrdersButton = new JButton("Мои заказы");
        JButton participateSurveyButton = new JButton("Пройти опрос");

        viewMenuButton.addActionListener(e -> viewMenu());
        placeOrderButton.addActionListener(e -> placeOrder());
        viewOrdersButton.addActionListener(e -> viewOrders());
        participateSurveyButton.addActionListener(e -> participateInSurvey());

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(viewMenuButton);
        panel.add(placeOrderButton);
        panel.add(viewOrdersButton);
        panel.add(participateSurveyButton);

        add(panel);
        setVisible(true);
    }

    private void viewMenu() {
        try {
            URL url = new URL("http://localhost:8080/getMenu");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining("\n"));
            in.close();

            JTextArea textArea = new JTextArea(response);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Текущее меню", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при получении меню: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void placeOrder() {
        String menuItemId = JOptionPane.showInputDialog(this, "Введите ID блюда для заказа:");
        if (menuItemId == null || menuItemId.trim().isEmpty()) return;

        try {
            URL url = new URL("http://localhost:8080/placeOrder");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            // Формируем JSON вручную
            String json = "{\"menuItemId\":" + menuItemId.trim() + ",\"quantity\":1}";

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            JOptionPane.showMessageDialog(this, response, "Результат", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при заказе: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewOrders() {
        try {
            URL url = new URL("http://localhost:8080/myOrders");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining("\n"));
            in.close();

            JTextArea textArea = new JTextArea(response);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Мои заказы", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при получении заказов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void participateInSurvey() {
        try {
            URL url = new URL("http://localhost:8080/getSurvey");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            if (response.isEmpty() || response.equals("{}")) {
                JOptionPane.showMessageDialog(this, "Опросов нет", "Информация", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Простое ручное парсирование
            String question = extractValue(response, "question");
            List<String> options = extractArray(response, "options");

            if (question == null || options.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ошибка разбора данных опроса", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] choices = options.toArray(new String[0]);
            String answer = (String) JOptionPane.showInputDialog(this, question, "Опрос", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);

            if (answer == null) return;

            // Отправка ответа
            URL answerUrl = new URL("http://localhost:8080/submitSurvey");
            HttpURLConnection answerConn = (HttpURLConnection) answerUrl.openConnection();
            answerConn.setRequestMethod("POST");
            answerConn.setDoOutput(true);
            answerConn.setRequestProperty("Content-Type", "application/json");
            answerConn.setRequestProperty("Authorization", "Bearer " + token);

            String jsonAnswer = "{\"answer\":\"" + answer.replace("\"", "\\\"") + "\"}";

            OutputStream os = answerConn.getOutputStream();
            os.write(jsonAnswer.getBytes());
            os.flush();
            os.close();

            BufferedReader answerReader = new BufferedReader(new InputStreamReader(answerConn.getInputStream()));
            String result = answerReader.lines().collect(Collectors.joining());
            answerReader.close();

            JOptionPane.showMessageDialog(this, result, "Ответ отправлен", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при участии в опросе: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Вспомогательный метод для извлечения строки из JSON
    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    // Вспомогательный метод для извлечения массива строк из JSON
    private List<String> extractArray(String json, String key) {
        List<String> values = new ArrayList<>();
        String pattern = "\"" + key + "\":[";
        int start = json.indexOf(pattern);
        if (start == -1) return values;
        start += pattern.length();
        int end = json.indexOf("]", start);
        if (end == -1) return values;
        String arrayContent = json.substring(start, end);
        String[] items = arrayContent.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"") && item.endsWith("\"")) {
                values.add(item.substring(1, item.length() - 1));
            }
        }
        return values;
    }
}
