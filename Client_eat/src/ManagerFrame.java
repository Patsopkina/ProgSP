import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerFrame extends JFrame {
    private String token;

    public ManagerFrame(String token) {
        this.token = token;
        setTitle("Менеджер - Корпоративное питание");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton viewMenuButton = new JButton("Просмотреть меню");
        JButton createSurveyButton = new JButton("Создать опрос");
        JButton updateMenuItemButton = new JButton("Обновить блюдо");
        JButton createDietButton = new JButton("Создать диету");
        JButton assignMenuToDietButton = new JButton("Назначить блюдо в диету");

        setLayout(new GridLayout(5, 1, 10, 10));
        add(viewMenuButton);
        add(createSurveyButton);
        add(updateMenuItemButton);
        add(createDietButton);
        add(assignMenuToDietButton);

        viewMenuButton.addActionListener(e -> viewMenu());
        createSurveyButton.addActionListener(e -> createSurvey());
        updateMenuItemButton.addActionListener(e -> updateMenuItem());
        createDietButton.addActionListener(e -> createDiet());
        assignMenuToDietButton.addActionListener(e -> assignMenuToDiet());

        setVisible(true);
    }

    private String sendGetRequest(String endpoint) throws IOException {
        URL url = new URL("http://localhost:8080" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned: " + conn.getResponseCode());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return in.lines().collect(Collectors.joining());
        }
    }

    private String sendPostRequest(String endpoint, String body) throws IOException {
        URL url = new URL("http://localhost:8080" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned: " + conn.getResponseCode());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return in.lines().collect(Collectors.joining());
        }
    }

    private void viewMenu() {
        try {
            String response = sendGetRequest("/menu");
            JOptionPane.showMessageDialog(this, response, "Меню", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка получения меню: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createSurvey() {
        String question = JOptionPane.showInputDialog(this, "Введите вопрос для опроса:");
        if (question == null || question.trim().isEmpty()) return;

        List<String> options = new ArrayList<>();
        while (true) {
            String option = JOptionPane.showInputDialog(this, "Введите вариант ответа (или оставьте пустым для завершения):");
            if (option == null || option.trim().isEmpty()) break;
            options.add(option);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"question\":\"").append(question).append("\",\"options\":[");
        for (int i = 0; i < options.size(); i++) {
            json.append("\"").append(options.get(i)).append("\"");
            if (i < options.size() - 1) json.append(",");
        }
        json.append("]}");

        try {
            sendPostRequest("/surveys", json.toString());
            JOptionPane.showMessageDialog(this, "Опрос успешно создан.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка создания опроса: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenuItem() {
        try {
            String response = sendGetRequest("/menu");
            String[] items = response.split("},");

            StringBuilder list = new StringBuilder("Выберите блюдо по ID:\n");
            for (String item : items) {
                String id = item.contains("\"id\":") ? item.split("\"id\":")[1].split(",")[0].trim() : "";
                String name = item.contains("\"name\":\"") ? item.split("\"name\":\"")[1].split("\"")[0] : "";
                list.append("ID: ").append(id).append(", Название: ").append(name).append("\n");
            }

            String idInput = JOptionPane.showInputDialog(this, list.toString());
            if (idInput == null || idInput.trim().isEmpty()) return;
            int id = Integer.parseInt(idInput.trim());

            JTextField nameField = new JTextField();
            JTextField descField = new JTextField();
            JTextField priceField = new JTextField();
            Object[] message = {
                    "Название:", nameField,
                    "Описание:", descField,
                    "Цена:", priceField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Введите данные блюда", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String name = nameField.getText();
            String description = descField.getText();
            double price = Double.parseDouble(priceField.getText());

            StringBuilder json = new StringBuilder();
            json.append("{\"id\":").append(id)
                    .append(",\"name\":\"").append(name)
                    .append("\",\"description\":\"").append(description)
                    .append("\",\"price\":").append(price)
                    .append("}");

            sendPostRequest("/menu/update", json.toString());
            JOptionPane.showMessageDialog(this, "Блюдо обновлено.");
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка обновления блюда: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDiet() {
        String name = JOptionPane.showInputDialog(this, "Введите название диеты:");
        if (name == null || name.trim().isEmpty()) return;

        String json = "{\"name\":\"" + name + "\"}";

        try {
            sendPostRequest("/diets", json);
            JOptionPane.showMessageDialog(this, "Диета успешно создана.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка создания диеты: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignMenuToDiet() {
        try {
            String menuIdStr = JOptionPane.showInputDialog(this, "Введите ID блюда:");
            String dietIdStr = JOptionPane.showInputDialog(this, "Введите ID диеты:");
            if (menuIdStr == null || dietIdStr == null) return;

            int menuId = Integer.parseInt(menuIdStr.trim());
            int dietId = Integer.parseInt(dietIdStr.trim());

            String json = "{\"menuItemId\":" + menuId + ",\"dietId\":" + dietId + "}";

            sendPostRequest("/diets/assign", json);
            JOptionPane.showMessageDialog(this, "Блюдо назначено в диету.");
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка назначения блюда в диету: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
