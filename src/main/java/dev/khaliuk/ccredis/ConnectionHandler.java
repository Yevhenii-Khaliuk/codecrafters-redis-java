package dev.khaliuk.ccredis;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// TODO: event loop
public class ConnectionHandler extends Thread {
    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             OutputStream outputStream = socket.getOutputStream()) {

            while (true) {
                byte[] bytes = new byte[256];
                int n = inputStream.read(bytes);

                if (n == -1) {
                    break;
                }

                System.out.printf("%s received %s bytes: %s%n", getName(), n, new String(bytes));

                outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.printf("%s socket closed%n", getName());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
