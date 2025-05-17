package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserSender {
    public static String sendCommand(String command) {
        try (Socket socket = new Socket("localhost", 8080)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(command);  // Отправка команды на сервер
            String response = in.readLine();  // Получаем ответ от сервера
            System.out.println(response);
            System.out.println(in.lines().toString());
            System.out.println(out.toString());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка соединения";
        }
    }
}


