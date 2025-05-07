package server;

import com.sun.net.httpserver.HttpServer;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer {
    public static void main(String[] args) {
        try {
            int port = 8080; // порт сервера
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Регистрируем обработчики по путям
            server.createContext("/login", new LoginHandler());
            server.createContext("/register",new RegisterHandle());
            server.createContext("/admin", new AdminHandler());
            server.createContext("/employee", new EmployeeHandler());
            server.createContext("/manager", new ManagerHandler());

            server.setExecutor(null); // создаёт дефолтный пул потоков
            server.start();

            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
