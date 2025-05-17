package server;

import db.DataBase;

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
                Socket clientSocket = serverSocket.accept();  // Ожидаем подключения клиента
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

                // Обработка каждого клиента в отдельном потоке
                executorService.submit(new UserHandler(clientSocket, dbManager));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();  // Завершаем работу потока
        }
    }
}
