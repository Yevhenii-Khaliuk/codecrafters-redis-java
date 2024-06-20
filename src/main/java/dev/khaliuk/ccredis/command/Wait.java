package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.replica.ReplicaClient;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Wait extends AbstractHandler {
    private static final Logger LOGGER = new Logger(Wait.class);

    private final AtomicInteger acknowledgedReplicasNumber = new AtomicInteger();

    public Wait(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        if (!(NumberUtils.isDigits(arguments[1]) && NumberUtils.isDigits(arguments[2]))) {
            throw new IllegalArgumentException(String.format("Digits expected: %s, %s", arguments[1], arguments[2]));
        }
        int expectedReplicasNumber = Integer.parseInt(arguments[1]);
        // TODO: stop waiting for futures, when reached expected replicas number
        long timeoutMillis = Long.parseLong(arguments[2]);
        LOGGER.log("Received WAIT arguments: replicas " + expectedReplicasNumber + ", timeout " + timeoutMillis);
        List<ReplicaClient> replicas = objectFactory.getApplicationProperties().getReplicas();
        LOGGER.log("Found " + replicas.size() + " replicas");

        CompletableFuture[] completableFutures = replicas
            .stream()
            .map(replica -> mapToFuture(replica, timeoutMillis))
            .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(completableFutures).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        long ackNumber = acknowledgedReplicasNumber.intValue();
        acknowledgedReplicasNumber.set(0);
        LOGGER.log("Counter after all futures completed: " + ackNumber);
        return objectFactory.getProtocolSerializer().integer(ackNumber == 0 ? replicas.size() : ackNumber);
    }

    private CompletableFuture<Boolean> mapToFuture(ReplicaClient replica, long timeoutMillis) {
        return CompletableFuture.supplyAsync(() -> getAcknowledgement(replica, timeoutMillis))
            .whenComplete((result, error) -> {
                if (Boolean.TRUE.equals(result)) {
                    acknowledgedReplicasNumber.incrementAndGet();
                }
            });
    }

    private boolean getAcknowledgement(ReplicaClient replica, long timeout) {
        LOGGER.log("Start sending ack request");
        byte[] command = objectFactory.getProtocolSerializer().array(List.of("REPLCONF", "GETACK", "*"));
        try {
            byte[] response = replica.sendAndAwaitResponse(command, timeout);
            LOGGER.log("Got response: " + Arrays.toString(response));
            // TODO: check offset here and return 'false' if offset is not the same as the leader's
            return true;
        } catch (Exception e) {
            LOGGER.log("Acknowledgment failed: " + e.getMessage());
            return false;
        }
    }
}
