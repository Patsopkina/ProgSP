package server;

import dto.KitchenStatusRequest;
import dto.KitchenStatusResponse;
import java.io.*;
import java.net.*;

public class KitchenStatusHandler implements Runnable {
    private Socket socket;
 private ObjectOutputStream out;
 private ObjectInputStream in;
    public KitchenStatusHandler(Socket socket, ObjectInputStream in,ObjectOutputStream out ) {
        this.socket = socket;
        this.out= out;
        this.in=in;
    }


    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Читаем запрос от клиента
            Object requestObj = in.readObject();
            if (requestObj instanceof KitchenStatusRequest request) {
                String commandType = request.getCommandType();
                KitchenStatusResponse response;

                // Обработка команд
                if ("OPEN_KITCHEN".equals(commandType)) {
                    response = new KitchenStatusResponse(true, "Кухня открыта!");
                } else if ("CLOSE_KITCHEN".equals(commandType)) {
                    response = new KitchenStatusResponse(true, "Кухня закрыта!");
                } else {
                    response = new KitchenStatusResponse(false, "Неизвестная команда.");
                }

                // Отправляем ответ обратно клиенту
                out.writeObject(response);
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
