package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.replica.ReplicaClient;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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

        Stream<CompletableFuture<Boolean>> futures = replicas.stream()
                .map(replica -> mapToFuture(replica, timeoutMillis));

        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        int ackNumber = acknowledgedReplicasNumber.intValue();
        acknowledgedReplicasNumber.set(0);
        LOGGER.log("Counter after all futures completed: " + ackNumber);
        return objectFactory.getProtocolSerializer().integer(ackNumber == 0 ? replicas.size() : ackNumber);
    }

    private CompletableFuture<Boolean> mapToFuture(ReplicaClient replica, long timeoutMillis) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> getAcknowledgement(replica))
                .whenComplete((result, error) -> {
                    if (Boolean.TRUE.equals(result)) {
                        acknowledgedReplicasNumber.incrementAndGet();
                    }
                });
        if (timeoutMillis > 0) {
            future.completeOnTimeout(false, timeoutMillis, TimeUnit.MILLISECONDS);
        }
        return future;
    }

    private boolean getAcknowledgement(ReplicaClient replica) {
        LOGGER.log("Start sending ack request");
        byte[] command = objectFactory.getProtocolSerializer().array(List.of("REPLCONF", "GETACK", "*"));
        try {
            byte[] response = replica.sendAndAwaitResponse(command);
            LOGGER.log("Got response: " + Arrays.toString(response));
            // TODO: check offset here and return 'false' if offset is not the same as the leader's
            return true;
        } catch (Exception e) {
            LOGGER.log("Acknowledgment failed: " + e.getMessage());
            return false;
        }
    }
}
