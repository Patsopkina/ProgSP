package server;

import dto.AuthRequest;
import dto.AuthResponse;
import  dto.KitchenStatusRequest;
import  dto.KitchenStatusResponse;
import java.io.*;
import java.net.Socket;

public class UserSender {
    public static AuthResponse sendAuthRequest(String command, String login, String password) {
        try (Socket socket = new Socket("localhost", 8080)) {  // Создаём сокет для подключения к серверу
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Формируем запрос
            AuthRequest request = new AuthRequest(command, login, password);
            out.writeObject(request);
            out.flush();

            // Получаем ответ от сервера
            Object responseObj = in.readObject();
            if (responseObj instanceof AuthResponse response) {
                return response;  // Если получили объект AuthResponse, возвращаем его
            } else {
                return new AuthResponse(false, "Неверный ответ сервера", null);  // В случае ошибки
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new AuthResponse(false, "Ошибка соединения", null);  // Если ошибка при соединении
        }
    }
}
