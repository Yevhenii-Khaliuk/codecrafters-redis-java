package dev.khaliuk.ccredis;

import dev.khaliuk.ccredis.command.CommandFactory;
import dev.khaliuk.ccredis.command.Handler;
import dev.khaliuk.ccredis.command.Multi;
import dev.khaliuk.ccredis.command.Psync;
import dev.khaliuk.ccredis.command.Write;
import dev.khaliuk.ccredis.config.ApplicationProperties;
import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.exception.EndOfStreamException;
import dev.khaliuk.ccredis.protocol.ProtocolDeserializer;
import dev.khaliuk.ccredis.replica.CommandReplicator;
import dev.khaliuk.ccredis.replica.ReplicaClient;
import org.apache.commons.lang3.tuple.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionHandler extends Thread {
    private static final Logger LOGGER = new Logger(ConnectionHandler.class);

    private final Socket socket;
    private final ProtocolDeserializer protocolDeserializer;
    private final CommandFactory commandFactory;
    private final CommandReplicator commandReplicator;
    private final ApplicationProperties applicationProperties;

    private boolean isReplicaSocket;

    public ConnectionHandler(Socket socket, ObjectFactory objectFactory) {
        this.socket = socket;
        protocolDeserializer = objectFactory.getProtocolDeserializer();
        commandFactory = objectFactory.getCommandFactory();
        commandReplicator = objectFactory.getCommandReplicator();
        applicationProperties = objectFactory.getApplicationProperties();
    }

    @Override
    public void run() {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            OutputStream outputStream = socket.getOutputStream();

            while (true) {
                Pair<String, Long> stringLongPair = protocolDeserializer.parseInput(inputStream);
                String commandString = stringLongPair.getLeft();
                LOGGER.log("Got command: " + commandString);
                String[] arguments = commandString.split(" ");
                String command = arguments[0].toUpperCase();
                Handler handler = commandFactory.getCommandHandler(command);
                byte[] response = handler.handle(arguments);

                if (handler instanceof Psync) {
                    isReplicaSocket = true;
                    applicationProperties.addReplica(new ReplicaClient(socket));
                    LOGGER.log("Replica with port %s has been added%n".formatted(socket.getPort()));
                    outputStream.write(response);
                    outputStream.flush();
                    return; // we want the loop to break here and consequently the thread to stop
                }

                if (handler instanceof Write) {
                    LOGGER.log("Start replicate command: " + commandString);
                    commandReplicator.replicateWriteCommand(commandString);
                    LOGGER.log("Command is sent for replication");
                }

                if (handler instanceof Multi multi) {
                    while (true) {
                        outputStream.write(response);
                        outputStream.flush();

                        var parsedInput = protocolDeserializer.parseInput(inputStream);
                        var commandWithArguments = parsedInput.getLeft().split(" ");

                        if (commandWithArguments[0].equalsIgnoreCase("EXEC")) {
                            response = multi.executeCommands();
                            break;
                        } else {
                            response = multi.enqueueCommand(commandWithArguments);
                        }
                    }
                }

                LOGGER.log("End handling with response: " + new String(response));
                outputStream.write(response);
                outputStream.flush();
            }

        } catch (EndOfStreamException e) {
            LOGGER.log("End of input stream reached");
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        } finally {
            if (!isReplicaSocket) {
                try {
                    socket.close();
                    LOGGER.log(String.format("%s socket closed%n", getName()));
                } catch (IOException e) {
                    LOGGER.log("IOException: " + e.getMessage());
                }
            }
        }
    }
}
