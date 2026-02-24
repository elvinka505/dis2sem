package ru.itis.dis403.lab1_01;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    final static Logger logger = LogManager.getLogger(HttpServer.class);

    public static void main(String[] args) {
        new Application().init();
        RequestHandler requestHandler = new RequestHandler();
        logger.info("start HttpServer");

        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> requestHandler.handle(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.atError().withThrowable(e);
            throw new RuntimeException(e);
        }
    }
}