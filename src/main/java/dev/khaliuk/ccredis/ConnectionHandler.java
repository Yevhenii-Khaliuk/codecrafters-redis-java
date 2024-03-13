package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.exception.EndOfStreamException;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

// TODO: event loop
public class ConnectionHandler extends Thread {
    private final Socket socket;
    private final ProtocolDeserializer protocolDeserializer;
    private final CommandHandler commandHandler;

    public ConnectionHandler(Socket socket, ObjectFactory objectFactory) {
        this.socket = socket;
        this.protocolDeserializer = objectFactory.getProtocolDeserializer();
        this.commandHandler = objectFactory.getCommandHandler();
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             OutputStream outputStream = socket.getOutputStream()) {

            while (true) {
                String parsedCommand = protocolDeserializer.parseInput(inputStream);
                byte[] response = commandHandler.handle(parsedCommand);
                outputStream.write(response);
                outputStream.flush();
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
