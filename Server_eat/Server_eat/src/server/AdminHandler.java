package server;

import db.DataBase;
import dto.UserRequest;
import dto.UserResponse;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class AdminHandler implements Runnable {
    private final Socket socket;
    private final DataBase db;

    public AdminHandler(Socket socket, DataBase db) {
        this.socket = socket;
        this.db = db;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Object obj = in.readObject();
            if (!(obj instanceof UserRequest request)) return;

            UserResponse response;
            switch (request.getCommand().toLowerCase()) {
                case "get_all_users" -> {
                    List<User> users = db.getUsers();
                    response = new UserResponse(true, "Список пользователей", users);
                }
                case "add_user" -> {
                    if (db.doesLoginExist(request.getLogin())) {
                        response = new UserResponse(false, "Пользователь уже существует");
                    } else {
                        db.insertUserWithRole(request.getLogin(), request.getPassword(), request.getRole());
                        response = new UserResponse(true, "Пользователь добавлен");
                    }
                }
                case "delete_user" -> {
                    db.deleteUserById(request.getUserId());
                    response = new UserResponse(true, "Пользователь удалён");
                }
                case "update_role" -> {
                    db.updateUserRole(request.getUserId(), request.getRole());
                    response = new UserResponse(true, "Роль обновлена");
                }
                case "set_blocked" -> {
                    db.setUserBlocked(request.getLogin(), request.isBlock() ? 1 : 0);
                    response = new UserResponse(true, "Статус блокировки обновлён");
                }
                
                default -> response = new UserResponse(false, "Неизвестная команда");
            }

            out.writeObject(response);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
