package server;

import db.DataBase;

import java.io.*;
import java.net.*;

public class UserHandler implements Runnable {
    private final Socket socket;
    private final DataBase dbManager;  // Добавляем DBManager

    // Конструктор ClientHandler теперь принимает dbManager
    public UserHandler(Socket socket, DataBase dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
    }

    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            String command = in.readLine();
            if (command != null) {
                String[] parts = command.split(":", 3);
                if (parts.length != 3) {
                    out.write("INVALID_COMMAND");
                    out.newLine();
                    out.flush();
                    return;
                }

                String type = parts[0];
                String login = parts[1];
                String password = parts[2];


                boolean success = switch (type) {
                    case "REGISTER" -> AuthManager.register(dbManager, login, password);  // Передаем dbManager
                    case "LOGIN" -> AuthManager.login(dbManager, login, password);  // Передаем dbManager
                    default -> false;
                };

                if (success) {
                    out.write("SUCCESS");
                } else {
                    if ("LOGIN".equals(type)) {
                        out.write("FAILURE: Неверный логин или пароль");
                    } else {
                        out.write("FAILURE");
                    }
                }
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}