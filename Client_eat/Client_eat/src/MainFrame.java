import db.DataBase;
import model.User;
import model.Diet;
import model.MenuItem;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import dto.UserRequest;
import dto.UserResponse;
import server.KitchenStatusSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainFrame extends JFrame {
    public MainFrame(String title, String message) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel(message, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        add(welcomeLabel);
    }
}
class AdminFrame extends MainFrame {
    public AdminFrame() {
        super("Окно администратора", "Добро пожаловать, Администратор!");

        JButton manageUsersButton = new JButton("Управление пользователями");
        manageUsersButton.setFont(new Font("Serif", Font.PLAIN, 18));

        manageUsersButton.addActionListener(e -> {
            UserManagement userManagementWindow = new UserManagement();
            userManagementWindow.setVisible(true);
        });
        JPanel panel = new JPanel();
        panel.add(manageUsersButton);


        getContentPane().add(panel, BorderLayout.CENTER);
    }
}

class ManagerFrame extends MainFrame {
    public ManagerFrame() {
        super("Окно менеджера", "Добро пожаловать, Менеджер!");

        // Кнопка управления меню
        JButton manageMenuButton = new JButton("Управление меню");
        manageMenuButton.setFont(new Font("Serif", Font.PLAIN, 18));
        manageMenuButton.addActionListener(e -> {
            MenuManagement menuWindow = new MenuManagement();
            menuWindow.setVisible(true);
        });

        // Кнопка создания рациона
        JButton createDietButton = new JButton("Создать рацион");
        createDietButton.setFont(new Font("Serif", Font.PLAIN, 18)); // исправлено!
        createDietButton.addActionListener(e -> {
            DietManagement dietWindow = new DietManagement();
            dietWindow.setVisible(true);
        });
        JButton viewDietsButton = new JButton("Просмотр рационов");
        viewDietsButton.addActionListener(e -> {
            DietViewer viewer = new DietViewer();
            viewer.setVisible(true);
        });
        JButton openKitchenButton = new JButton("Открыть кухню");
        JButton closeKitchenButton = new JButton("Закрыть кухню");

        openKitchenButton.setFont(new Font("Serif", Font.PLAIN, 18));
        closeKitchenButton.setFont(new Font("Serif", Font.PLAIN, 18));

        // Обработчики для кнопок открытия и закрытия кухни
        openKitchenButton.addActionListener(e -> handleKitchenStatusRequest("OPEN_KITCHEN"));
        closeKitchenButton.addActionListener(e -> handleKitchenStatusRequest("CLOSE_KITCHEN"));

        // Общая панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.add(manageMenuButton);
        buttonPanel.add(createDietButton);
        buttonPanel.add(viewDietsButton);
        buttonPanel.add(openKitchenButton);
        buttonPanel.add(closeKitchenButton);
        // Добавление панели в окно
        getContentPane().add(buttonPanel, BorderLayout.CENTER);
    }
    private void handleKitchenStatusRequest(String commandType) {
        KitchenStatusSender.sendKitchenStatusRequest(commandType);  // Отправляем запрос на сервер
    }
}

class EmployeeFrame extends MainFrame {
    public EmployeeFrame() {
        super("Окно сотрудника", "Добро пожаловать, Сотрудник!");
    }
}


class UserManagement extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagement() {
        setTitle("Управление пользователями");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Логин", "Роль", "Заблокирован"}, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");
        JButton updateRoleButton = new JButton("Изменить роль");
        JButton blockButton = new JButton("Блок/Разблок");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateRoleButton);
        buttonPanel.add(blockButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddUserDialog());
        deleteButton.addActionListener(e -> deleteUser());
        updateRoleButton.addActionListener(e -> changeUserRole());
        blockButton.addActionListener(e -> changeBlockStatus());

        loadUsersFromServer();
    }

    private UserResponse sendRequest(UserRequest request) {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            Object responseObj = in.readObject();
            if (responseObj instanceof UserResponse response) {
                return response;
            } else {
                return new UserResponse(false, "Неверный ответ от сервера");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new UserResponse(false, "Ошибка связи с сервером");
        }
    }

    private void loadUsersFromServer() {
        UserRequest request = new UserRequest("get_all_users");
        UserResponse response = sendRequest(request);

        tableModel.setRowCount(0);
        if (response.isSuccess() && response.getUsers() != null) {
            for (User user : response.getUsers()) {
                tableModel.addRow(new Object[]{
                        user.getId(),
                        user.getLogin(),
                        user.getRole(),
                        user.isBlocked() ? "Да" : "Нет"
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage());
        }
    }

    private void showAddUserDialog() {
        JTextField loginField = new JTextField();
        JTextField passwordField = new JTextField();
        String[] roles = {"EMPLOYEE", "MANAGER", "ADMIN"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Логин:"));
        panel.add(loginField);
        panel.add(new JLabel("Пароль:"));
        panel.add(passwordField);
        panel.add(new JLabel("Роль:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить пользователя", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();
            String role = (String) roleBox.getSelectedItem();

            if (!login.isEmpty() && !password.isEmpty()) {
                UserRequest request = new UserRequest("add_user");
                request.setLogin(login);
                request.setPassword(password);
                request.setRole(role);

                UserResponse response = sendRequest(request);
                JOptionPane.showMessageDialog(this, response.getMessage());
                if (response.isSuccess()) {
                    loadUsersFromServer();
                }
            }
        }
    }

    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String[] roles = {"EMPLOYEE", "MANAGER", "ADMIN"};
            String currentRole = (String) tableModel.getValueAt(selectedRow, 2);
            String newRole = (String) JOptionPane.showInputDialog(this, "Новая роль:", "Изменить роль",
                    JOptionPane.QUESTION_MESSAGE, null, roles, currentRole);

            if (newRole != null && !newRole.equals(currentRole)) {
                UserRequest request = new UserRequest("update_role");
                request.setUserId(userId);
                request.setRole(newRole);

                UserResponse response = sendRequest(request);
                JOptionPane.showMessageDialog(this, response.getMessage());
                if (response.isSuccess()) {
                    loadUsersFromServer();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите пользователя");
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Удалить пользователя?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                UserRequest request = new UserRequest("delete_user");
                request.setUserId(userId);

                UserResponse response = sendRequest(request);
                JOptionPane.showMessageDialog(this, response.getMessage());
                if (response.isSuccess()) {
                    loadUsersFromServer();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите пользователя");
        }
    }

    private void changeBlockStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String login = (String) tableModel.getValueAt(selectedRow, 1);
            boolean isBlocked = "Да".equals(tableModel.getValueAt(selectedRow, 3));
            boolean newStatus = !isBlocked;

            UserRequest request = new UserRequest("set_blocked");
            request.setLogin(login);
            request.setBlock(newStatus);

            UserResponse response = sendRequest(request);
            JOptionPane.showMessageDialog(this, response.getMessage());
            if (response.isSuccess()) {
                loadUsersFromServer();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите пользователя");
        }
    }
}
 class MenuManagement extends JFrame {
    private JTable menuTable;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> availabilityFilterBox;

    public MenuManagement() {
        setTitle("Управление меню");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        // Панель поиска и фильтрации
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Поиск по названию:"));
        searchField = new JTextField(15);
        topPanel.add(searchField);

        topPanel.add(new JLabel("Фильтр по доступности:"));
        String[] availabilityOptions = {"ALL", "Доступно", "Недоступно"};
        availabilityFilterBox = new JComboBox<>(availabilityOptions);
        topPanel.add(availabilityFilterBox);

        // Таблица меню
        String[] columnNames = {"ID", "Название", "Описание", "Цена", "Доступно"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        menuTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        menuTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(menuTable);

        // Кнопки действий
        JButton addButton = new JButton("Добавить блюдо");
        JButton editButton = new JButton("Редактировать блюдо");
        JButton deleteButton = new JButton("Удалить блюдо");

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedItem());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();

        // Слушатели
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterMenuItems();
            }

            public void removeUpdate(DocumentEvent e) {
                filterMenuItems();
            }

            public void changedUpdate(DocumentEvent e) {
                filterMenuItems();
            }
        });

        availabilityFilterBox.addActionListener(e -> filterMenuItems());

        // Загрузка данных
        loadMenuItems();
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JCheckBox availableBox = new JCheckBox("Доступно", true);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Описание:"));
        panel.add(descField);
        panel.add(new JLabel("Цена:"));
        panel.add(priceField);
        panel.add(availableBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить блюдо", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            double price;

            if (name.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Название и описание не могут быть пустыми.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Цена должна быть положительным числом.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean available = availableBox.isSelected();
            MenuItem newItem = new MenuItem(name, desc, price, available);

            DataBase db = new DataBase();
            if (!db.insertMenuItem(newItem)) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении блюда.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            loadMenuItems();
        }
    }

    private void showEditDialog() {
        int selected = menuTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Выберите блюдо для редактирования.");
            return;
        }

        int realRow = menuTable.convertRowIndexToModel(selected);
        int id = (int) model.getValueAt(realRow, 0);
        String name = (String) model.getValueAt(realRow, 1);
        String desc = (String) model.getValueAt(realRow, 2);
        Object value = model.getValueAt(realRow, 3);
        double price;
        if (value instanceof BigDecimal) {
            price = ((BigDecimal) value).doubleValue();
        } else if (value instanceof Number) {
            price = ((Number) value).doubleValue();
        } else {
            price = Double.parseDouble(value.toString());
        }
        boolean available = (boolean) model.getValueAt(realRow, 4);

        JTextField nameField = new JTextField(name);
        JTextField descField = new JTextField(desc);
        JTextField priceField = new JTextField(String.valueOf(price));
        JCheckBox availableBox = new JCheckBox("Доступно", available);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Описание:"));
        panel.add(descField);
        panel.add(new JLabel("Цена:"));
        panel.add(priceField);
        panel.add(availableBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Редактировать блюдо", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newDesc = descField.getText().trim();
            double newPrice;

            if (newName.isEmpty() || newDesc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Название и описание не могут быть пустыми.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                newPrice = Double.parseDouble(priceField.getText().trim());
                if (newPrice <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Цена должна быть положительным числом.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean newAvailable = availableBox.isSelected();
            MenuItem updatedItem = new MenuItem(id, newName, newDesc, newPrice, newAvailable);

            DataBase db = new DataBase();
            if (!db.updateMenuItem(updatedItem)) {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении блюда.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            loadMenuItems();
        }
    }

    private void deleteSelectedItem() {
        int selected = menuTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Выберите блюдо для удаления.");
            return;
        }

        int realRow = menuTable.convertRowIndexToModel(selected);
        int id = (int) model.getValueAt(realRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить это блюдо?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DataBase db = new DataBase();
            if (!db.deleteMenuItemById(id)) {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении блюда.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            loadMenuItems();
        }
    }

    private void filterMenuItems() {
        String searchText = searchField.getText().trim();
        String availability = (String) availabilityFilterBox.getSelectedItem();

        ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1)); // по названию
        }

        if (!"ALL".equals(availability)) {
            boolean required = availability.equals("Доступно");
            filters.add(RowFilter.regexFilter(String.valueOf(required), 4)); // по доступности
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void loadMenuItems() {
        model.setRowCount(0); // очистка модели
        String sql = "SELECT * FROM menu_items";
        DataBase db = new DataBase();
        db.loadTableData(menuTable, sql);
    }
}
class DietManagement extends JFrame {

    private  Diet diet;
    private DefaultListModel<String> existingListModel;
    private DefaultListModel<String> selectedListModel;
    private JList<String> existingList;
    private JList<String> selectedList;
    private ArrayList<MenuItem> allAvailableItems;
    private ArrayList<MenuItem> customItems;

    public DietManagement() {
        setTitle("Создание рациона");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        dietComponents();
    }

    public DietManagement(Diet diet) {
        setTitle("Редактирование рациона: " + diet.getName());
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        existingListModel = new DefaultListModel<>();
        selectedListModel = new DefaultListModel<>();
        existingList = new JList<>(existingListModel);
        selectedList = new JList<>(selectedListModel);

        allAvailableItems = loadMenuItems(existingListModel);
        customItems = new ArrayList<>();

        // Заполнение выбранных блюд из рациона
        for (MenuItem item : diet.getMenuItems()) {
            if (item.getId() == -1) {
                selectedListModel.addElement(item.getName() + " (Уникальное)");
                customItems.add(item);
            } else {
                selectedListModel.addElement(item.getName() + " (ID: " + item.getId() + ")");
            }
        }

        JButton addToDietButton = new JButton("Добавить >>");
        JButton removeFromDietButton = new JButton("<< Удалить");

        setupTransferButtons(existingList, selectedList, existingListModel, selectedListModel,
                addToDietButton, removeFromDietButton);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3));
        centerPanel.add(new JScrollPane(existingList));

        JPanel middleButtons = new JPanel(new GridLayout(2, 1));
        middleButtons.add(addToDietButton);
        middleButtons.add(removeFromDietButton);
        centerPanel.add(middleButtons);

        centerPanel.add(new JScrollPane(selectedList));

        JButton addCustomDishButton = new JButton("Добавить уникальное блюдо");
        addCustomDishButton.addActionListener(e ->
                addCustomDish(this, selectedListModel, customItems)
        );

        JButton saveButton = new JButton("Сохранить изменения");
        saveButton.addActionListener(e -> saveExistingDiet( this,diet, selectedListModel, allAvailableItems, customItems));

        JButton renameButton = new JButton("Переименовать рацион");
        renameButton.addActionListener(e -> renameDiet(this, diet));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(addCustomDishButton);
        bottomPanel.add(renameButton);
        bottomPanel.add(saveButton);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    private void dietComponents() {
        existingListModel = new DefaultListModel<>();
        selectedListModel = new DefaultListModel<>();
        existingList = new JList<>(existingListModel);
        selectedList = new JList<>(selectedListModel);

        allAvailableItems = loadMenuItems(existingListModel);
        customItems = new ArrayList<>();

        JButton addToDietButton = new JButton("Добавить >>");
        JButton removeFromDietButton = new JButton("<< Удалить");

        setupTransferButtons(existingList, selectedList, existingListModel, selectedListModel,
                addToDietButton, removeFromDietButton);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3));
        centerPanel.add(new JScrollPane(existingList));

        JPanel middleButtons = new JPanel(new GridLayout(2, 1));
        middleButtons.add(addToDietButton);
        middleButtons.add(removeFromDietButton);
        centerPanel.add(middleButtons);

        centerPanel.add(new JScrollPane(selectedList));

        JButton addCustomDishButton = new JButton("Добавить уникальное блюдо");
        addCustomDishButton.addActionListener(e ->
                addCustomDish(this, selectedListModel, customItems)
        );

        JButton saveButton = new JButton("Сохранить рацион");
        saveButton.addActionListener(e ->
                saveDiet(this, selectedListModel, allAvailableItems, customItems)
        );

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(addCustomDishButton);
        bottomPanel.add(saveButton);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private ArrayList<MenuItem> loadMenuItems(DefaultListModel<String> model) {

        ArrayList<MenuItem> result = new ArrayList<>();
        ArrayList<MenuItem> allItems = new DataBase().getAllMenuItems();
        for (MenuItem item : allItems) {
            model.addElement(item.getName() + " (ID: " + item.getId() + ")");
            result.add(item);
        }
        return result;
    }

    private void setupTransferButtons(JList<String> source, JList<String> target,
                                      DefaultListModel<String> sourceModel, DefaultListModel<String> targetModel,
                                      JButton addButton, JButton removeButton) {
        addButton.addActionListener(e -> {
            for (String selected : source.getSelectedValuesList()) {
                if (!targetModel.contains(selected)) {
                    targetModel.addElement(selected);
                }
            }
        });

        removeButton.addActionListener(e -> {
            for (String selected : target.getSelectedValuesList()) {
                targetModel.removeElement(selected);
            }
        });
    }

    private void addCustomDish(DietManagement dialog, DefaultListModel<String> selectedListModel,
                               ArrayList<MenuItem> customItems) {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("Название:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Описание:"));
        inputPanel.add(descField);
        inputPanel.add(new JLabel("Цена:"));
        inputPanel.add(priceField);

        int res = JOptionPane.showConfirmDialog(dialog, inputPanel, "Новое блюдо в рацион", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                if (name.isEmpty() || desc.isEmpty()) {
                    throw new IllegalArgumentException("Название и описание не могут быть пустыми.");
                }
                if (price <= 0) {
                    throw new IllegalArgumentException("Цена должна быть положительной.");
                }

                MenuItem tempItem = new MenuItem(-1, name, desc, price, true);
                String display = name + " (Уникальное)";
                if (!selectedListModel.contains(display)) {
                    selectedListModel.addElement(display);
                    customItems.add(tempItem);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Цена должна быть числом.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveDiet(DietManagement dialog, DefaultListModel<String> selectedListModel,
                          ArrayList<MenuItem> allItems, ArrayList<MenuItem> customItems) {
        String dietName = JOptionPane.showInputDialog(dialog, "Введите название рациона:");
        if (dietName == null || dietName.trim().isEmpty()) return;

        Diet diet = new Diet(dietName.trim());

        ArrayList<MenuItem> selectedItems = new ArrayList<>();

        for (int i = 0; i < selectedListModel.size(); i++) {
            String display = selectedListModel.get(i);

            if (display.endsWith("(Уникальное)")) {
                String name = display.replace(" (Уникальное)", "");
                for (MenuItem item : customItems) {
                    if (item.getName().equals(name)) {
                        selectedItems.add(item);
                        break;
                    }
                }
            } else if (display.contains("(ID: ")) {
                try {
                    int idStart = display.lastIndexOf("(ID: ") + 5;
                    int idEnd = display.lastIndexOf(")");
                    int id = Integer.parseInt(display.substring(idStart, idEnd));
                    for (MenuItem item : allItems) {
                        if (item.getId() == id) {
                            selectedItems.add(item);
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Добавляем блюда в рацион (если у класса Diet есть метод setItems или addItem)
        diet.setMenuItems(selectedItems); // <-- добавил недостающий вызов

        boolean success = new DataBase().saveDietWithItems(diet);
        if (success) {
            JOptionPane.showMessageDialog(dialog, "Рацион успешно сохранён.");
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "Ошибка при сохранении рациона.");
        }
    }
    private void saveExistingDiet(DietManagement dialog, Diet diet,
                                  DefaultListModel<String> selectedListModel,
                                  ArrayList<MenuItem> allItems, ArrayList<MenuItem> customItems) {

        ArrayList<MenuItem> selectedItems = new ArrayList<>();
        DataBase db = new DataBase();

        for (int i = 0; i < selectedListModel.size(); i++) {
            String display = selectedListModel.get(i);

            if (display.endsWith("(Уникальное)")) {
                String name = display.replace(" (Уникальное)", "");
                for (MenuItem item : customItems) {
                    if (item.getName().equals(name)) {
                        // Сохраняем в БД, если ещё не сохранено (ID == 0 или -1)
                        if (item.getId() <= 0) {
                            boolean success = db.insertMenuItem(item); // Метод теперь возвращает boolean
                            if (success) {
                                allItems.add(item); // добавим в общий список
                            } else {
                                JOptionPane.showMessageDialog(dialog, "Ошибка при сохранении уникального блюда: " + item.getName());
                                return;
                            }
                        }
                        selectedItems.add(item);
                        break;
                    }
                }
            } else if (display.contains("(ID: ")) {
                try {
                    int idStart = display.lastIndexOf("(ID: ") + 5;
                    int idEnd = display.lastIndexOf(")");
                    int id = Integer.parseInt(display.substring(idStart, idEnd));
                    for (MenuItem item : allItems) {
                        if (item.getId() == id) {
                            selectedItems.add(item);
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        diet.setMenuItems(selectedItems);

        boolean success = db.updateDiet(diet);
        if (success) {
            JOptionPane.showMessageDialog(dialog, "Рацион успешно обновлён.");
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "Ошибка при обновлении рациона.");
        }
    }


    private void renameDiet(DietManagement dialog, Diet diet) {
        String newName = JOptionPane.showInputDialog(dialog, "Введите новое название рациона:", diet.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            diet.setName(newName.trim());
            dialog.setTitle("Редактирование рациона: " + newName.trim());
        }
    }
}
class DietViewer extends JFrame{
    private JList<String> dietList;
    private DefaultListModel<String> dietListModel;
    private JButton editButton, deleteButton;
    private ArrayList<Diet> allDiets;

    public DietViewer() {
        setTitle("Список рационов");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        dietListModel = new DefaultListModel<>();
        dietList = new JList<>(dietListModel);
        allDiets = new DataBase().getAllDiets(); // Предполагается, что есть такой метод

        for (Diet d : allDiets) {
            dietListModel.addElement(d.getName() + " (ID: " + d.getId() + ")");
        }

        editButton = new JButton("Редактировать");
        deleteButton = new JButton("Удалить");

        editButton.addActionListener(e -> editSelectedDiet());
        deleteButton.addActionListener(e -> deleteSelectedDiet());

        JPanel buttons = new JPanel();
        buttons.add(editButton);
        buttons.add(deleteButton);

        add(new JScrollPane(dietList), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    // В классе DietViewer
    private void editSelectedDiet() {
        int index = dietList.getSelectedIndex(); // Получаем индекс выбранного рациона
        if (index != -1) {
            Diet selectedDiet = allDiets.get(index); // Получаем объект рациона по индексу
            DietManagement editor = new DietManagement(selectedDiet); // Открываем редактор с переданным рационом
            editor.setVisible(true); // Делаем окно видимым
        } else {
            JOptionPane.showMessageDialog(this, "Выберите рацион для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void deleteSelectedDiet() {
        int index = dietList.getSelectedIndex();
        if (index != -1) {
            Diet selected = allDiets.get(index);
            int confirm = JOptionPane.showConfirmDialog(this, "Удалить рацион \"" + selected.getName() + "\"?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new DataBase().deleteDiet(selected.getId()); // Предполагается, что такой метод есть
                dietListModel.remove(index);
                allDiets.remove(index);
            }
        }
    }

}