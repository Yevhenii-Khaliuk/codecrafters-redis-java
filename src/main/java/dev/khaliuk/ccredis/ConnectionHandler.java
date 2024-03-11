package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.exception.EndOfStreamException;

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
                String parsedCommand = ProtocolParser.parseInput(inputStream);
                String response = CommandHandler.handle(parsedCommand);
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                // TODO: handle end of input
            }

        } catch (EndOfStreamException e) {
            System.out.println("End of input stream reached");
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
