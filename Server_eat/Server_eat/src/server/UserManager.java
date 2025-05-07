package server;

import dto.UserRequest;
import dto.UserResponse;
import db.DataBase;

public class UserManager {
    private final DataBase db;

    public UserManager(DataBase db) {
        this.db = db;  // Используем существующий экземпляр DataBase
    }

    public UserResponse handleRequest(UserRequest request) {
        UserResponse response = new UserResponse();  // Создаём пустой ответ

        switch (request.getCommand()) {
            case "ADD_USER":
                addUser(request, response);
                break;
            case "UPDATE_ROLE":
                updateUserRole(request, response);
                break;
            case "BLOCK_USER":
                blockUser(request, response);
                break;
            case "UNBLOCK_USER":
                unblockUser(request, response);
                break;
            case "DELETE_USER":
                deleteUser(request, response);
                break;
            default:
                response.setSuccess(false);
                response.setMessage("Неизвестная команда.");
        }

        return response;
    }

    private void addUser(UserRequest request, UserResponse response) {
        String login = request.getLogin();
        String password = request.getPassword();
        String role = request.getRole();

        boolean userExists = db.doesLoginExist(login);

        if (userExists) {
            response.setSuccess(false);
            response.setMessage("Пользователь с таким логином уже существует.");
        } else {
            db.insertUserWithRole(login, password, role);
            response.setSuccess(true);
            response.setMessage("Пользователь добавлен успешно.");
        }
    }

    private void updateUserRole(UserRequest request, UserResponse response) {
        int userId = request.getUserId();
        String newRole = request.getRole();

        db.updateUserRole(userId, newRole);
        response.setSuccess(true);
        response.setMessage("Роль пользователя обновлена.");
    }

    private void blockUser(UserRequest request, UserResponse response) {
        int userId = request.getUserId();

        db.setUserBlocked(userId, 1);  // Блокируем пользователя
        response.setSuccess(true);
        response.setMessage("Пользователь заблокирован.");
    }

    private void unblockUser(UserRequest request, UserResponse response) {
        int userId = request.getUserId();

        db.setUserBlocked(userId, 0);  // Разблокируем пользователя
        response.setSuccess(true);
        response.setMessage("Пользователь разблокирован.");
    }

    private void deleteUser(UserRequest request, UserResponse response) {
        int userId = request.getUserId();

        db.deleteUserById(userId);
        response.setSuccess(true);
        response.setMessage("Пользователь удален.");
    }
}
