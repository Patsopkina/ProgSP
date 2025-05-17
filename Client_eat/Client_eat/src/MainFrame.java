import db.DataBase;
import model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

import model.MenuItem;
import  session.SessionManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;


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

        // Установка меньшего размера окна
        setSize(500, 400); // Например, 500x400 вместо 800x600

        // Кнопки
        JButton manageUsersButton = createStyledButton("Управление пользователями");
        manageUsersButton.addActionListener(e -> {
            UserManagement userManagementWindow = new UserManagement();
            userManagementWindow.setVisible(true);
        });

        JButton viewFeedbackButton = createStyledButton("Просмотр отзывов");
        viewFeedbackButton.addActionListener(e -> {
            FeedbackViewer feedbackViewerWindow = new FeedbackViewer();
            feedbackViewerWindow.setVisible(true);
        });

        JButton viewSurveyResultsButton = createStyledButton("Просмотр результатов опросов");
        viewSurveyResultsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Функция просмотра результатов опросов в разработке.",
                    "Заглушка",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JButton generateReportButton = createStyledButton("Составление отчета");
        generateReportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Функция составления отчета в разработке.",
                    "Заглушка",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Панель кнопок
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        panel.add(manageUsersButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(viewFeedbackButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(viewSurveyResultsButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(generateReportButton);

        getContentPane().add(panel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Serif", Font.PLAIN, 18));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 40));
        return button;
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
        createDietButton.setFont(new Font("Serif", Font.PLAIN, 18));
        createDietButton.addActionListener(e -> {
            DietManagement dietWindow = new DietManagement();
            dietWindow.setVisible(true);
        });

        // Кнопка просмотра рационов
        JButton viewDietsButton = new JButton("Просмотр рационов");
        viewDietsButton.setFont(new Font("Serif", Font.PLAIN, 18));
        viewDietsButton.addActionListener(e -> {
            DietViewer viewer = new DietViewer();
            viewer.setVisible(true);
        });

        // Кнопка закрытия кухни
        JButton closeKitchenButton = new JButton("Закрыть кухню");
        closeKitchenButton.setFont(new Font("Serif", Font.PLAIN, 18));
        closeKitchenButton.addActionListener(e -> {
            KitchenNotifier.closeKitchen();
            JOptionPane.showMessageDialog(this, "Кухня закрыта.");
        });

        // Кнопка открытия кухни
        JButton openKitchenButton = new JButton("Открыть кухню");
        openKitchenButton.setFont(new Font("Serif", Font.PLAIN, 18));
        openKitchenButton.addActionListener(e -> {
            KitchenNotifier.openKitchen();
            JOptionPane.showMessageDialog(this, "Кухня открыта.");
        });

        JButton createSurveyButton = new JButton("Создать опрос");
        createSurveyButton.setFont(new Font("Serif", Font.PLAIN, 18));
        createSurveyButton.addActionListener(e -> {
            ManagerSurvey surveyFrame = new ManagerSurvey(); // отдельное окно для выбора блюд
            surveyFrame.setVisible(true);
        });
        JButton manageSurveysButton = new JButton("Управление опросами");
        manageSurveysButton.setFont(new Font("Serif", Font.PLAIN, 18));
        manageSurveysButton.addActionListener(e -> {
            SurveyViewer managementFrame = new SurveyViewer();
            managementFrame.setVisible(true);
        });

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.add(manageMenuButton);
        buttonPanel.add(createDietButton);
        buttonPanel.add(viewDietsButton);
        buttonPanel.add(closeKitchenButton);
        buttonPanel.add(openKitchenButton);
        buttonPanel.add(createSurveyButton);
        buttonPanel.add(manageSurveysButton);

        getContentPane().add(buttonPanel, BorderLayout.CENTER);
    }
}

class EmployeeFrame extends MainFrame {
    private final String login;
    private User currentUser;

    public EmployeeFrame(String login) {
        super("Окно сотрудника", "Добро пожаловать, Сотрудник!");
        this.login = login;
        DataBase db = new DataBase();
        this.currentUser = db.getUserByLogin(login);

        // Уведомление о статусе кухни
        if (!KitchenNotifier.isKitchenOpen()) {
            JOptionPane.showMessageDialog(this,
                    "Внимание: кухня сейчас закрыта.",
                    "Уведомление",
                    JOptionPane.WARNING_MESSAGE);
        }

        JButton viewMenuButton = new JButton("Просмотр меню");
        viewMenuButton.setFont(new Font("Serif", Font.PLAIN, 18));
        viewMenuButton.addActionListener(e -> {
            EmployeeMenuViewer viewer = new EmployeeMenuViewer();
            viewer.setVisible(true);
        });

        JButton orderButton = new JButton("Оформить заказ");
        orderButton.setFont(new Font("Serif", Font.PLAIN, 18));
        orderButton.addActionListener(e -> {
            if (currentUser != null) {
                MenuOrderViewer orderViewer = new MenuOrderViewer(currentUser);
                orderViewer.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Пользователь не найден!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton viewOrdersButton = new JButton("Просмотр истории заказов");
        viewOrdersButton.setFont(new Font("Serif", Font.PLAIN, 18));
        viewOrdersButton.addActionListener(e -> {
            if (currentUser != null) {
                OrderHistoryViewer orderHistoryViewer = new OrderHistoryViewer(currentUser);
                orderHistoryViewer.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Пользователь не найден!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton editProfileButton = new JButton("Редактировать профиль");
        editProfileButton.setFont(new Font("Serif", Font.PLAIN, 18));
        editProfileButton.addActionListener(e -> {
            new EmployeeProfileEditor(login).setVisible(true);
        });

        JButton feedbackButton = new JButton("Оставить отзыв");
        feedbackButton.setFont(new Font("Serif", Font.PLAIN, 18));
        feedbackButton.addActionListener(e -> {
            FeedbackForm form = new FeedbackForm(login);
            form.setVisible(true);
        });

        // Новая кнопка "Изменить заказ"
        JButton modifyOrderButton = new JButton("Изменить заказ");
        modifyOrderButton.setFont(new Font("Serif", Font.PLAIN, 18));
        modifyOrderButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Функция изменения заказа в разработке.",
                    "Заглушка",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Новая кнопка "Пройти опрос"
        JButton surveyButton = new JButton("Пройти опрос");
        surveyButton.setFont(new Font("Serif", Font.PLAIN, 18));
        surveyButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Функция прохождения опроса в разработке.",
                    "Заглушка",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Панель для кнопок
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 10, 10)); // Вертикальное расположение кнопок
        panel.add(viewMenuButton);
        panel.add(orderButton);
        panel.add(modifyOrderButton);  // новая кнопка
        panel.add(surveyButton);       // новая кнопка
        panel.add(viewOrdersButton);
        panel.add(editProfileButton);
        panel.add(feedbackButton);

        getContentPane().add(panel, BorderLayout.CENTER);
    }
}

class UserManagement extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> roleFilterBox;

    public UserManagement() {
        setTitle("Управление пользователями");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUserManagementComponents();
    }

    private void initUserManagementComponents() {

        // Модель таблицы с колонками
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Поиск по логину:"));
        searchField = new JTextField(15);
        topPanel.add(searchField);

        topPanel.add(new JLabel("Фильтр по роли:"));
        String[] roles = {"ALL", "EMPLOYEE", "ADMIN", "MANAGER"};
        roleFilterBox = new JComboBox<>(roles);
        topPanel.add(roleFilterBox);

        // Таблица
        String[] columnNames = {"ID", "Логин", "Роль", "Блокировка"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Кнопки действий
        JButton addButton = new JButton("Добавить");
        JButton changeRoleButton = new JButton("Изменить роль");
        JButton deleteButton = new JButton("Удалить");
        JButton refreshButton = new JButton("Обновить");
        JButton logoutButton = new JButton("Выход");
        JButton blockButton = new JButton("Заблокировать");
        JButton unblockButton = new JButton("Разблокировать");

        addButton.addActionListener(e -> showAddUserDialog());
        changeRoleButton.addActionListener(e -> changeUserRole());
        deleteButton.addActionListener(e -> deleteUser());
        blockButton.addActionListener(e -> changeBlockStatus(1));
        unblockButton.addActionListener(e -> changeBlockStatus(0));
        refreshButton.addActionListener(e -> loadUsersFromDatabase());
        logoutButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(changeRoleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(blockButton);
        buttonPanel.add(unblockButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

        // Основная компоновка
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();

        // Слушатели для фильтрации
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        roleFilterBox.addActionListener(e -> filter());

        // Первичная загрузка данных
        loadUsersFromDatabase();
    }

    // Метод для добавления пользователя в таблицу
    private void addUserToTable(int id, String username, String role) {
        tableModel.addRow(new Object[]{id, username, role});
    }

    private void loadUsersFromDatabase() {
        DataBase dbManager = new DataBase();
        ArrayList<User> users = dbManager.getUsers();

        tableModel.setRowCount(0); // очистка старых данных
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getId(), user.getLogin(), user.getRole(), user.isBlocked()
            });
        }
    }

    private void showAddUserDialog() {
        DataBase dbManager = new DataBase();
        String previousLogin = "";

        while (true) {
            JTextField loginField = new JTextField(previousLogin);
            JPasswordField passwordField = new JPasswordField();
            String[] roles = {"EMPLOYEE", "ADMIN", "MANAGER"};
            JComboBox<String> roleBox = new JComboBox<>(roles);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Логин:"));
            panel.add(loginField);
            panel.add(new JLabel("Пароль:"));
            panel.add(passwordField);
            panel.add(new JLabel("Роль:"));
            panel.add(roleBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Добавить пользователя",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                break;
            }

            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            previousLogin = login; // сохраняем введённый логин

            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Логин и пароль не должны быть пустыми.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (dbManager.doesLoginExist(login)) {
                JOptionPane.showMessageDialog(this, "Пользователь с таким логином уже существует. Введите другой логин.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            dbManager.insertUserWithRole(login, password, role);
            loadUsersFromDatabase();
            break;
        }
    }

    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя из таблицы.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 2);

        String[] roles = {"EMPLOYEE", "ADMIN", "MANAGER"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(currentRole);

        int result = JOptionPane.showConfirmDialog(this, roleBox, "Выберите новую роль",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newRole = (String) roleBox.getSelectedItem();

            if (!newRole.equals(currentRole)) {
                DataBase dbManager = new DataBase();
                dbManager.updateUserRole(userId, newRole);
                loadUsersFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Роль не изменилась.", "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя для удаления.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить пользователя?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DataBase dbManager = new DataBase();
            dbManager.deleteUserById(userId);
            loadUsersFromDatabase();
        }
    }

    private void filter() {
        RowFilter<DefaultTableModel, Object> loginFilter = null;
        RowFilter<DefaultTableModel, Object> roleFilter = null;

        String searchText = searchField.getText().trim();
        String selectedRole = (String) roleFilterBox.getSelectedItem();

        if (!searchText.isEmpty()) {
            loginFilter = RowFilter.regexFilter("(?i)" + searchText, 1);
        }

        if (!"ALL".equals(selectedRole)) {
            roleFilter = RowFilter.regexFilter("^" + selectedRole + "$", 2);
        }

        ArrayList<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (loginFilter != null) filters.add(loginFilter);
        if (roleFilter != null) filters.add(roleFilter);

        RowFilter<DefaultTableModel, Object> combinedFilter = filters.isEmpty()
                ? null
                : RowFilter.andFilter(filters);

        sorter.setRowFilter(combinedFilter);
    }

    private void changeBlockStatus(int block) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            String login = (String) tableModel.getValueAt(selectedRow, 1);
            String role = (String) tableModel.getValueAt(selectedRow, 2);

            if (!role.equals("EMPLOYEE") && !role.equals("MANAGER")) {
                JOptionPane.showMessageDialog(this, "Можно блокировать только пользователей с ролями CLIENT и MANAGER.");
                return;
            }

            DataBase db = new DataBase();
            db.setUserBlocked(login, block);
            loadUsersFromDatabase();
        } else {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите пользователя.");
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
        ArrayList<String> unavailableItems = new ArrayList<>();

        for (int i = 0; i < selectedListModel.size(); i++) {
            String display = selectedListModel.get(i);

            if (display.endsWith("(Уникальное)")) {
                String name = display.replace(" (Уникальное)", "");
                for (MenuItem item : customItems) {
                    if (item.getName().equals(name)) {
                        if (!item.isAvailable()) {
                            unavailableItems.add(item.getName());
                        } else {
                            selectedItems.add(item);
                        }
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
                            if (!item.isAvailable()) {
                                unavailableItems.add(item.getName());
                            } else {
                                selectedItems.add(item);
                            }
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Показываем список недоступных блюд, если они есть
        if (!unavailableItems.isEmpty()) {
            StringBuilder message = new StringBuilder("Следующие блюда недоступны и не могут быть добавлены в рацион:\n");
            for (String name : unavailableItems) {
                message.append("• ").append(name).append("\n");
            }
            JOptionPane.showMessageDialog(dialog, message.toString(), "Недоступные блюда", JOptionPane.WARNING_MESSAGE);
            return; // Прерываем сохранение
        }

        diet.setMenuItems(selectedItems);

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
class KitchenNotifier {

    private static final DataBase db = new DataBase();

    public static void closeKitchen() {
        db.updateKitchenStatus(false);
        System.out.println("Кухня закрыта. Пользователи уведомлены.");
        // Можно добавить логирование, отправку уведомлений и т.д.
    }

    public static void openKitchen() {
        db.updateKitchenStatus(true);
        System.out.println("Кухня открыта. Пользователи уведомлены.");
    }
    public static boolean isKitchenOpen() {
        return db.isKitchenOpen();
    }
}
class ManagerSurvey extends JFrame {
    private JPanel checkboxPanel;
    private ArrayList<JCheckBox> checkBoxes;
    private ArrayList<MenuItem> allItems;
    private JButton saveButton, addButton, editButton, renameButton;
    private JTextField surveyNameField;
    private Survey currentSurvey;

    public ManagerSurvey() {
        setTitle("Создание опроса: Нелюбимые блюда");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Инициализация нового опроса
        this.currentSurvey = new Survey();
        this.currentSurvey.setName("Новый опрос");

        // Панель с названием опроса
        JPanel namePanel = new JPanel(new BorderLayout());
        surveyNameField = new JTextField(currentSurvey.getName());
        namePanel.add(new JLabel("Название опроса:"), BorderLayout.WEST);
        namePanel.add(surveyNameField, BorderLayout.CENTER);
        add(namePanel, BorderLayout.NORTH);

        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadMenuItems();

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Сохранить опрос");
        addButton = new JButton("Добавить блюдо");
        buttonPanel.add(saveButton);
        buttonPanel.add(addButton);

        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveSurvey());
        addButton.addActionListener(e -> showAddDishDialog());
    }

    public ManagerSurvey(Survey survey) {
        setTitle("Редактирование опроса: " + survey.getName());
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        this.currentSurvey = survey;

        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadMenuItems();

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Сохранить опрос");
        renameButton = new JButton("Переименовать опрос");
        addButton = new JButton("Добавить блюдо");

        buttonPanel.add(saveButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel namePanel = new JPanel(new BorderLayout());
        surveyNameField = new JTextField(survey.getName());
        namePanel.add(new JLabel("Название опроса:"), BorderLayout.WEST);
        namePanel.add(surveyNameField, BorderLayout.CENTER);
        add(namePanel, BorderLayout.NORTH);

        saveButton.addActionListener(e -> saveSurvey());
        renameButton.addActionListener(e -> renameSurvey());
        addButton.addActionListener(e -> showAddDishDialog());
    }

    private void loadMenuItems() {
        DataBase db = new DataBase();
        allItems = db.getAllMenuItems();
        checkBoxes = new ArrayList<>();
        checkboxPanel.removeAll();

        // Загружаем все блюда
        for (MenuItem item : allItems) {
            JCheckBox checkBox = new JCheckBox(item.getName() + " (ID: " + item.getId() + ")");
            checkBoxes.add(checkBox);
            checkboxPanel.add(checkBox);
        }

        // Если существует текущий опрос, то загружаем связанные блюда
        if (currentSurvey != null) {
            ArrayList<MenuItem> surveyItems = db.getSurveyItemsBySurveyId(currentSurvey.getId());

            // Отметить блюда, которые уже добавлены в опрос
            for (JCheckBox checkBox : checkBoxes) {
                int itemId = Integer.parseInt(checkBox.getText().split(": ")[1].replace(")", ""));
                for (MenuItem item : surveyItems) {
                    if (item.getId() == itemId) {
                        checkBox.setSelected(true);
                        break;
                    }
                }
            }
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    private ArrayList<MenuItem> getSelectedMenuItems() {
        ArrayList<MenuItem> selected = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selected.add(allItems.get(i));
            }
        }
        return selected;
    }

    private void saveSurvey() {
        // Проверка инициализации поля
        if (surveyNameField == null) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка инициализации формы. Поле названия не найдено.",
                    "Системная ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Получение и валидация названия
        String surveyName = surveyNameField.getText().trim();
        if (surveyName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Название опроса не может быть пустым.",
                    "Ошибка ввода",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Проверка выбранных пунктов
        ArrayList<MenuItem> selectedItems = getSelectedMenuItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Вы не выбрали ни одного блюда.",
                    "Ошибка выбора",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Проверка инициализации currentSurvey
        if (currentSurvey == null) {
            currentSurvey = new Survey(); // Создаем новый, если не был инициализирован
        }

        try {
            DataBase db = new DataBase();
            boolean success;

            // Определяем, новый это опрос или существующий
            if (currentSurvey.getId() <= 0) { // Новый опрос
                success = db.insertSurvey(surveyName, selectedItems);
            } else { // Редактирование существующего
                success = db.updateSurvey(currentSurvey.getId(), surveyName, selectedItems);
            }

            // Обработка результата
            if (success) {
                currentSurvey.setName(surveyName);
                currentSurvey.setItems(selectedItems);

                JOptionPane.showMessageDialog(this,
                        "Опрос успешно сохранён.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose(); // Закрываем окно после успешного сохранения
            } else {
                throw new Exception("Не удалось сохранить опрос в базу данных");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при сохранении опроса: " + ex.getMessage(),
                    "Ошибка базы данных",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Логируем для отладки
        }
    }
    private void renameSurvey() {
        String newName = JOptionPane.showInputDialog(this, "Введите новое название опроса:", surveyNameField.getText());
        if (newName != null && !newName.trim().isEmpty()) {
            surveyNameField.setText(newName);
            currentSurvey.setName(newName);  // Обновляем название в объекте
            DataBase db = new DataBase();
            boolean success = db.updateSurvey(currentSurvey.getId(), newName, new ArrayList<>(currentSurvey.getItems()));

            if (success) {
                JOptionPane.showMessageDialog(this, "Название опроса успешно обновлено.");
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении названия.");
            }
        }
    }


    private void showAddDishDialog() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название блюда:"));
        panel.add(nameField);
        panel.add(new JLabel("Описание:"));
        panel.add(descField);
        panel.add(new JLabel("Цена:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить новое блюдо",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String description = descField.getText();
                double price = Double.parseDouble(priceField.getText());

                MenuItem item = new MenuItem();
                item.setName(name);
                item.setDescription(description);
                item.setPrice(price);
                item.setAvailable(true);

                DataBase db = new DataBase();
                db.insertMenuItem(item);

                loadMenuItems();
                JOptionPane.showMessageDialog(this, "Блюдо добавлено.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Некорректная цена.");
            }
        }
    }
}
class SurveyViewer extends JFrame {
    private JList<String> surveyList;
    private DefaultListModel<String> surveyListModel;
    private JButton refreshButton, editButton, deleteButton;
    private ArrayList<Survey> allSurveys;
    private final DataBase db = new DataBase();

    public SurveyViewer() {
        setTitle("Управление опросами");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadSurveys();
    }

    private void initComponents() {
        surveyListModel = new DefaultListModel<>();
        surveyList = new JList<>(surveyListModel);
        surveyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        surveyList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Создаем кнопки (теперь refreshButton идет первой)
        refreshButton = new JButton("Обновить");
        editButton = new JButton("Редактировать блюда");
        deleteButton = new JButton("Удалить");

        // Устанавливаем шрифты
        Font buttonFont = new Font("Serif", Font.PLAIN, 14);
        refreshButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);

        // Добавляем обработчики событий
        refreshButton.addActionListener(e -> loadSurveys());
        editButton.addActionListener(e -> openSurveyEditor());
        deleteButton.addActionListener(e -> deleteSelectedSurvey());

        // Создаем панель для кнопок (refreshButton теперь первая)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Добавляем компоненты на форму
        add(new JScrollPane(surveyList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSurveys() {
        surveyListModel.clear();
        allSurveys = db.getAllSurveys();

        if (allSurveys.isEmpty()) {
            surveyListModel.addElement("Нет доступных опросов");
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            for (Survey survey : allSurveys) {
                surveyListModel.addElement(survey.getName());
            }
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private void openSurveyEditor() {
        int index = surveyList.getSelectedIndex();
        if (index < 0 || index >= allSurveys.size()) {
            JOptionPane.showMessageDialog(this,
                    "Выберите опрос для редактирования",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Survey selectedSurvey = allSurveys.get(index);
        new ManagerSurvey(selectedSurvey).setVisible(true);
    }

    private void deleteSelectedSurvey() {
        int index = surveyList.getSelectedIndex();
        if (index < 0 || index >= allSurveys.size()) {
            JOptionPane.showMessageDialog(this,
                    "Сначала выберите опрос",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Survey survey = allSurveys.get(index);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить опрос \"" + survey.getName() + "\"?\nЭто действие нельзя отменить.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (db.deleteSurvey(survey.getId())) {
                allSurveys.remove(index);
                surveyListModel.remove(index);

                if (allSurveys.isEmpty()) {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Не удалось удалить опрос",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
class EmployeeMenuViewer extends JFrame {

    private JPanel itemsPanel;

    public EmployeeMenuViewer() {
        setTitle("Просмотр меню");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);

        add(new JLabel("Меню доступных и недоступных блюд:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadMenuItems();

        setVisible(true);
    }

    private void loadMenuItems() {
        DataBase db = new DataBase();

        // Загружаем диеты с блюдами
        ArrayList<Diet> diets = db.getAllDiets();

        // Панель для диет
        JPanel dietPanelContainer = new JPanel();
        dietPanelContainer.setLayout(new BoxLayout(dietPanelContainer, BoxLayout.Y_AXIS));

        // Панель для отдельных блюд
        JPanel individualItemsPanel = new JPanel();
        individualItemsPanel.setLayout(new BoxLayout(individualItemsPanel, BoxLayout.Y_AXIS));
        individualItemsPanel.setBorder(BorderFactory.createTitledBorder("Отдельные блюда"));

        // Получаем все блюда, чтобы отображать их отдельно
        ArrayList<MenuItem> allMenuItems = db.getAllMenuItems();

        // Отображаем диеты
        for (Diet diet : diets) {
            JPanel dietPanel = new JPanel(new BorderLayout());
            dietPanel.setBorder(BorderFactory.createTitledBorder(diet.getName())); // Название рациона

            // Панель для блюд рациона
            JPanel dietItemsPanel = new JPanel();
            dietItemsPanel.setLayout(new BoxLayout(dietItemsPanel, BoxLayout.Y_AXIS));

            // Получаем блюда, связанные с этим рационом
            for (MenuItem item : diet.getMenuItems()) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                JLabel nameLabel = new JLabel(item.getName() + " - " + item.getDescription() + " | " + item.getPrice() + " руб.");
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                if (!item.isAvailable()) {
                    nameLabel.setForeground(Color.GRAY);
                    nameLabel.setText(nameLabel.getText() + " (недоступно)");
                }

                itemPanel.add(nameLabel, BorderLayout.CENTER);
                dietItemsPanel.add(itemPanel);
            }

            dietPanel.add(dietItemsPanel, BorderLayout.CENTER);
            dietPanelContainer.add(dietPanel);
        }

        // Отображаем все блюда как отдельные элементы
        for (MenuItem item : allMenuItems) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JLabel nameLabel = new JLabel(item.getName() + " - " + item.getDescription() + " | " + item.getPrice() + " руб.");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            if (!item.isAvailable()) {
                nameLabel.setForeground(Color.GRAY);
                nameLabel.setText(nameLabel.getText() + " (недоступно)");
            }

            itemPanel.add(nameLabel, BorderLayout.CENTER);
            individualItemsPanel.add(itemPanel);
        }

        // Добавляем панели с диетами и отдельными блюдами в основной контейнер
        itemsPanel.add(dietPanelContainer, BorderLayout.NORTH);
        itemsPanel.add(individualItemsPanel, BorderLayout.CENTER);

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

}
class FeedbackForm extends JFrame {

    private final String employeeLogin; // Сохраняем логин как поле

    public FeedbackForm(String employeeLogin) {
        this.employeeLogin = employeeLogin; // <-- сохраняем логин

        setTitle("Оставить отзыв");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea textArea = new JTextArea(8, 30);
        JButton submitButton = new JButton("Отправить");

        submitButton.addActionListener(e -> {
            String message = textArea.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Отзыв не может быть пустым.");
                return;
            }

            System.out.println("Отправка отзыва от пользователя: " + employeeLogin); // Для отладки

            DataBase.getInstance().submitFeedback(employeeLogin, message);
            JOptionPane.showMessageDialog(this, "Спасибо за ваш отзыв!");
            dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);

        add(panel);
    }
}

class FeedbackViewer extends JFrame {

    private JTextArea feedbackArea;

    public FeedbackViewer() {
        setTitle("Отзывы сотрудников");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setFont(new Font("Serif", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JButton refreshButton = new JButton("Обновить отзывы");
        refreshButton.addActionListener(e -> loadFeedback());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        add(panel);

        loadFeedback(); // Загрузка отзывов при открытии окна
    }

    private void loadFeedback() {
        ArrayList<Feedback> feedbacks = DataBase.getInstance().getAllFeedback();
        feedbackArea.setText("");

        if (feedbacks.isEmpty()) {
            feedbackArea.setText("Пока нет отзывов.");
        } else {
            for (Feedback feedback : feedbacks) {
                feedbackArea.append(feedback.toString() + "\n\n");
            }
        }
    }
}
class EmployeeProfileEditor extends JFrame {
    private final String currentLogin;

    public EmployeeProfileEditor(String currentLogin) {
        this.currentLogin = currentLogin;

        setTitle("Редактирование профиля");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Поля для ввода
        JTextField loginField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        // Панель с полями
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Новый логин (необязательно):"));
        formPanel.add(loginField);

        formPanel.add(new JLabel("Новый пароль (необязательно):"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Повторите пароль:"));
        formPanel.add(confirmField);

        JButton updateButton = new JButton("Обновить");
        formPanel.add(new JLabel()); // Пустая ячейка
        formPanel.add(updateButton);

        add(formPanel);

        updateButton.addActionListener(e -> {
            String newLogin = loginField.getText().trim();
            String newPassword = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmField.getPassword()).trim();

            // Проверка пароля
            if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Пароли не совпадают!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sessionToken = SessionManager.createSession(currentLogin);
            boolean updated = DataBase.getInstance().updateEmployeeCredentials(currentLogin, newLogin, newPassword, sessionToken);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Данные успешно обновлены!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
class MenuOrderViewer extends JFrame {
    private JPanel itemsPanel;
    private ArrayList<OrderItem> selectedItems;
    private JButton orderButton;
    private JComboBox<String> timeComboBox;
    private User currentUser;

    public MenuOrderViewer(User currentUser) {
        setTitle("Оформление заказа");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        this.currentUser = currentUser;

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);

        // Панель для выбора времени приготовления и кнопки заказа
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel timePanel = new JPanel();
        timePanel.add(new JLabel("Время на приготовление: "));
        timeComboBox = new JComboBox<>(new String[]{"1 час", "3 часа", "5 часов"});
        timePanel.add(timeComboBox);

        bottomPanel.add(timePanel, BorderLayout.NORTH);

        // Создаем и добавляем кнопку заказа
        orderButton = new JButton("Оформить заказ");
        orderButton.setFont(new Font("Serif", Font.PLAIN, 16));
        bottomPanel.add(orderButton, BorderLayout.SOUTH);

        add(new JLabel("Меню доступных и недоступных блюд:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        selectedItems = new ArrayList<>();
        loadMenuItems();

        orderButton.addActionListener(e -> {
            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(MenuOrderViewer.this,
                        "Выберите хотя бы одно блюдо для заказа.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            } else {
                Order order = createOrder(currentUser, selectedItems);
                saveOrderToDatabase(order);
                JOptionPane.showMessageDialog(MenuOrderViewer.this,
                        "Ваш заказ оформлен!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                selectedItems.clear();
                itemsPanel.removeAll();
                loadMenuItems();
            }
        });
    }

    public void loadMenuItems() {
        DataBase db = new DataBase();
        ArrayList<Diet> diets = db.getAllDiets();

        JPanel dietPanelContainer = new JPanel();
        dietPanelContainer.setLayout(new BoxLayout(dietPanelContainer, BoxLayout.Y_AXIS));

        JPanel individualItemsPanel = new JPanel();
        individualItemsPanel.setLayout(new BoxLayout(individualItemsPanel, BoxLayout.Y_AXIS));
        individualItemsPanel.setBorder(BorderFactory.createTitledBorder("Отдельные блюда"));

        ArrayList<MenuItem> allMenuItems = db.getAllMenuItems();

        for (Diet diet : diets) {
            JPanel dietPanel = new JPanel(new BorderLayout());
            dietPanel.setBorder(BorderFactory.createTitledBorder(diet.getName()));

            JPanel dietItemsPanel = new JPanel();
            dietItemsPanel.setLayout(new BoxLayout(dietItemsPanel, BoxLayout.Y_AXIS));

            for (MenuItem item : diet.getMenuItems()) {
                dietItemsPanel.add(createMenuItemPanel(item));
            }

            dietPanel.add(dietItemsPanel, BorderLayout.CENTER);
            dietPanelContainer.add(dietPanel);
        }

        for (MenuItem item : allMenuItems) {
            individualItemsPanel.add(createMenuItemPanel(item));
        }

        itemsPanel.removeAll(); // Очистка перед добавлением
        itemsPanel.setLayout(new BorderLayout());
        itemsPanel.add(dietPanelContainer, BorderLayout.NORTH);
        itemsPanel.add(individualItemsPanel, BorderLayout.CENTER);

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createMenuItemPanel(MenuItem item) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel nameLabel = new JLabel(item.getName() + " - " + item.getDescription() + " | " + item.getPrice() + " руб.");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Добавляем JSpinner для выбора количества
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1)); // От 1 до 10

        JCheckBox checkBox = new JCheckBox("Выбрать");
        checkBox.setEnabled(item.isAvailable());

        checkBox.addActionListener(e -> {
            if (checkBox.isSelected()) {
                // Добавляем OrderItem с количеством, выбранным в JSpinner
                selectedItems.add(new OrderItem(item, (Integer) quantitySpinner.getValue()));
            } else {
                // Убираем OrderItem при снятии выбора
                selectedItems.removeIf(orderItem -> orderItem.getMenuItem().getId() == item.getId());
            }
        });

        if (!item.isAvailable()) {
            nameLabel.setForeground(Color.GRAY);
            nameLabel.setText(nameLabel.getText() + " (недоступно)");
            checkBox.setEnabled(false);
        }

        JPanel itemContainer = new JPanel(new BorderLayout());
        itemContainer.add(nameLabel, BorderLayout.CENTER);
        itemContainer.add(checkBox, BorderLayout.EAST);

        // Добавляем количество с JSpinner
        JPanel quantityPanel = new JPanel(new BorderLayout());
        quantityPanel.add(new JLabel("Количество: "), BorderLayout.WEST);
        quantityPanel.add(quantitySpinner, BorderLayout.CENTER);

        JPanel container = new JPanel(new BorderLayout());
        container.add(itemContainer, BorderLayout.NORTH);
        container.add(quantityPanel, BorderLayout.SOUTH);

        itemPanel.add(container, BorderLayout.CENTER);
        return itemPanel;
    }

    // Метод, который теперь принимает объект User
    private Order createOrder(User user, ArrayList<OrderItem> selectedItems) {
        Order order = new Order();
        order.setUser(user); // Устанавливаем текущего пользователя в заказ
        order.setStatus("PENDING"); // Статус заказа

        // Преобразуем LocalDateTime в Timestamp для использования в базе данных
        Timestamp orderTimestamp = Timestamp.valueOf(LocalDateTime.now());
        order.setOrderTime(orderTimestamp); // Устанавливаем время заказа

        // Добавляем время на приготовление
        String selectedTime = (String) timeComboBox.getSelectedItem(); // Получаем выбранное время
        int preparationTimeHours = 0;

        // Преобразуем строку времени в количество часов
        if (selectedTime != null && selectedTime.contains("час")) {
            preparationTimeHours = Integer.parseInt(selectedTime.split(" ")[0]); // Извлекаем количество часов
        }

        // Рассчитываем время на приготовление
        long preparationMillis = orderTimestamp.getTime() + (preparationTimeHours * 60 * 60 * 1000L);
        Timestamp preparationTimestamp = new Timestamp(preparationMillis);
        order.setPreparationTime(preparationTimestamp); // Устанавливаем время на приготовление

        // Устанавливаем выбранные блюда (OrderItems)
        order.setOrderedItems(new ArrayList<>(selectedItems));

        return order;
    }

    private void saveOrderToDatabase(Order order) {
        DataBase db = new DataBase();
        db.saveOrder(order); // Внутри сохранит и order_items
    }

    public ArrayList<OrderItem> getSelectedItems() {
        return selectedItems;
    }
}
class OrderHistoryViewer extends JFrame {
    private User currentUser;
    private OrderHistory orderHistory;

    public OrderHistoryViewer(User user) {
        this.currentUser = user;
        this.orderHistory = getOrderHistoryForUser(user); // Получаем историю заказов для пользователя

        setTitle("История заказов - " + currentUser.getLogin());
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Панель для отображения заказов
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Получаем строковое представление истории заказов
        String ordersText = orderHistory != null ? orderHistory.displayOrders() : "Нет заказов.";
        textArea.setText(ordersText);

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Метод для получения истории заказов для текущего пользователя
    private OrderHistory getOrderHistoryForUser(User user) {
        DataBase db = new DataBase();
        return db.getOrderHistoryByUser(user);
    }
}






