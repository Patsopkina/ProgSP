package server;

import db.DataBase;
import dto.KitchenStatusRequest;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MainServer {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 10;

    public static void main(String[] args) {
        DataBase dbManager = new DataBase();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен и слушает порт " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

                executorService.submit(() -> {
                    try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                         ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                        System.out.println("1 ");
                        Object requestObj = in.readObject();

                        if (requestObj instanceof KitchenStatusRequest) {
                            System.out.println("2 ");
                            new KitchenStatusHandler(clientSocket, in, out).run();
                        } else {
                            System.out.println("3 ");
                            new UserHandler(clientSocket, dbManager, out, in).run();

                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при запуске сервера", e);
        }
    }
}

