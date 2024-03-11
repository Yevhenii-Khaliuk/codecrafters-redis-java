package dev.khaliuk.ccredis;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 6379;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            System.out.println("Server has started on port: " + port);

            while (true) {
                new ConnectionHandler(serverSocket.accept()).start();
            }
        }
    }
}
