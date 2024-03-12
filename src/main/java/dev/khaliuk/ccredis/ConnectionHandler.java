package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.exception.EndOfStreamException;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// TODO: event loop
public class ConnectionHandler extends Thread {
    private final Socket socket;
    private final ProtocolDeserializer protocolDeserializer;
    private final CommandHandler commandHandler;

    public ConnectionHandler(Socket socket, ProtocolDeserializer protocolDeserializer, CommandHandler commandHandler) {
        this.socket = socket;
        this.protocolDeserializer = protocolDeserializer;
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             OutputStream outputStream = socket.getOutputStream()) {

            while (true) {
                String parsedCommand = protocolDeserializer.parseInput(inputStream);
                String response = commandHandler.handle(parsedCommand);
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