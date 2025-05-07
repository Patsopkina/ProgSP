package server;

import dto.KitchenStatusRequest;
import dto.KitchenStatusResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

 public class KitchenStatusSender{
    public static KitchenStatusResponse sendKitchenStatusRequest(String commandType) {
        try (Socket socket = new Socket("localhost", 8080)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Формируем запрос на изменение статуса кухни
            KitchenStatusRequest request = new KitchenStatusRequest(commandType);
            out.writeObject(request);
            out.flush();

            // Получаем ответ от сервера
            Object responseObj = in.readObject();
            if (responseObj instanceof KitchenStatusResponse response) {
                return response;  // Возвращаем статус кухни
            } else {
                return new KitchenStatusResponse(false, "Неверный ответ сервера");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new KitchenStatusResponse(false, "Ошибка соединения");
        }
    }
}
