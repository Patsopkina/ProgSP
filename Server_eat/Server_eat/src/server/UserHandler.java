package server;

import db.DataBase;
import dto.AuthRequest;
import dto.AuthResponse;

import java.io.*;
import java.net.Socket;


public class UserHandler implements Runnable {
    private final Socket socket;
    private final DataBase dbManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public UserHandler(Socket socket, DataBase dbManager, ObjectOutputStream out,ObjectInputStream in) {
        this.socket = socket;
        this.dbManager = dbManager;
        this.out= out;
        this.in=in;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        ) {
            Object obj = in.readObject();

            if (obj instanceof AuthRequest authRequest) {
                AuthResponse response;

                switch (authRequest.getCommand().toLowerCase()) {
                    case "login" -> {
                        boolean result = AuthManager.login(dbManager, authRequest.getLogin(), authRequest.getPassword());
                        String role = result ? dbManager.getUserRoleByLogin(authRequest.getLogin()): null;
                        response = new AuthResponse(result, result ? "Успешный вход" : "Неверный логин или пароль", role);
                    }
                    case "register" -> {
                        boolean result = AuthManager.register(dbManager, authRequest.getLogin(), authRequest.getPassword());
                        String role = result ? "EMPLOYEE" : null;
                        response = new AuthResponse(result, result ? "Регистрация прошла успешно" : "Логин уже существует", role);
                    }
                    default -> response = new AuthResponse(false, "Неизвестная команда", null);
                }

                out.writeObject(response);
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
