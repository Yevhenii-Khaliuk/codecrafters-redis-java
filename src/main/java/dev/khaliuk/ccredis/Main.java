package dev.khaliuk.ccredis;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        Socket clientSocket = null;
        int port = 6379;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            OutputStream outputStream = clientSocket.getOutputStream();

            while (true) {
                byte[] bytes = new byte[256];
                int n = inputStream.read(bytes);

                if (n == -1) {
                    break;
                }

                System.out.printf("received %s bytes: %s%n", n, new String(bytes));

                outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
