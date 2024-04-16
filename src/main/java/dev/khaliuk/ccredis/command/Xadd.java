package dev.khaliuk.ccredis.command;

import dev.khaliuk.ccredis.config.Logger;
import dev.khaliuk.ccredis.config.ObjectFactory;
import dev.khaliuk.ccredis.protocol.ValueType;
import dev.khaliuk.ccredis.storage.Storage;
import dev.khaliuk.ccredis.storage.StreamRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Xadd extends AbstractHandler {
    private static final Logger LOGGER = new Logger(Xadd.class);

    private static final String DOUBLE_ZERO_ID_ERROR_MESSAGE =
        "ERR The ID specified in XADD must be greater than 0-0";
    private static final String INVALID_ID_ERROR_MESSAGE =
        "ERR The ID specified in XADD is equal or smaller than the target stream top item";

    public Xadd(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    public byte[] handle(String[] arguments) {
        if (arguments.length < 5) {
            throw new IllegalArgumentException("XADD requires at least 5 arguments, got " + arguments.length);
        }

        String streamKey = arguments[1];
        String streamId = arguments[2];

        List<StreamRecord> existingStream =
            Optional.ofNullable(Storage.get(streamKey))
                .filter(r -> r.valueType() == ValueType.STREAM)
                .map(r -> (List<StreamRecord>) r.value())
                .orElseGet(ArrayList::new);

        String latestId = existingStream.reversed().stream()
            .map(StreamRecord::id)
            .findFirst()
            .orElse(null);

        try {
            streamId = validateStreamId(streamId, latestId);
        } catch (InvalidIdException e) {
            return objectFactory.getProtocolSerializer().simpleError(e.getMessage());
        }

        List<Pair<String, String>> streamValues = new ArrayList<>();

        for (int i = 3; i < arguments.length - 1; i += 2) {
            streamValues.add(Pair.of(arguments[i], arguments[i + 1]));
        }

        StreamRecord newStreamRecord = new StreamRecord(streamId, streamValues);
        existingStream.add(newStreamRecord);
        Storage.put(streamKey, ValueType.STREAM, existingStream);

        return objectFactory.getProtocolSerializer().bulkString(streamId);
    }

    private String validateStreamId(String newId, String existingId) {
        long newMillisecondsTime;
        long newSequenceNumber;

        if (newId.equals("*")) {
            newMillisecondsTime = Instant.now().toEpochMilli();
            newSequenceNumber = generateNewSequenceNumberWithNewMillis(existingId, newMillisecondsTime);
        } else {
            String millisecondsString = newId.substring(0, newId.indexOf("-"));
            newMillisecondsTime = Long.parseLong(millisecondsString);

            String sequenceString = newId.substring(newId.indexOf("-") + 1);
            newSequenceNumber = existingId == null ?
                generateNewSequenceNumber(sequenceString, newMillisecondsTime) :
                generateSequenceNumberWithExistingStream(existingId, newMillisecondsTime, sequenceString);
        }

        return String.format("%s-%s", newMillisecondsTime, newSequenceNumber);
    }

    private long generateNewSequenceNumberWithNewMillis(String existingId, long newMillisecondsTime) {
        if (existingId != null) {
            long existingMillisecondsTime = Long.parseLong(existingId.substring(0, existingId.indexOf("-")));
            if (existingMillisecondsTime == newMillisecondsTime) {
                return Long.parseLong(existingId.substring(existingId.indexOf("-") + 1));
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private long generateNewSequenceNumber(String sequenceString, long newMillisecondsTime) {
        if (sequenceString.equals("*")) {
            return newMillisecondsTime == 0 ? 1 : 0;
        } else {
            long newSequenceNumber = Long.parseLong(sequenceString);

            if (newMillisecondsTime == 0 && newSequenceNumber == 0) {
                throw new InvalidIdException(DOUBLE_ZERO_ID_ERROR_MESSAGE);
            }

            return newSequenceNumber;
        }
    }

    private long generateSequenceNumberWithExistingStream(String existingId, long newMillisecondsTime, String sequenceString) {
        long existingMillisecondsTime = Long.parseLong(existingId.substring(0, existingId.indexOf("-")));
        long existingSequenceNumber = Long.parseLong(existingId.substring(existingId.indexOf("-") + 1));

        if (sequenceString.equals("*")) {
            if (newMillisecondsTime < existingMillisecondsTime) {
                throw new InvalidIdException(INVALID_ID_ERROR_MESSAGE);
            } else if (newMillisecondsTime > existingMillisecondsTime) {
                return 0;
            } else {
                return existingSequenceNumber + 1;
            }
        } else {
            long newSequenceNumber = Long.parseLong(sequenceString);

            if (newMillisecondsTime == 0 && newSequenceNumber == 0) {
                throw new InvalidIdException(DOUBLE_ZERO_ID_ERROR_MESSAGE);
            }

            if (newMillisecondsTime < existingMillisecondsTime) {
                throw new InvalidIdException(INVALID_ID_ERROR_MESSAGE);
            }

            if (newMillisecondsTime == existingMillisecondsTime && newSequenceNumber <= existingSequenceNumber) {
                throw new InvalidIdException(INVALID_ID_ERROR_MESSAGE);
            }

            return newSequenceNumber;
        }
    }

    private static class InvalidIdException extends RuntimeException {
        private InvalidIdException(String message) {
            super(message);
        }
    }
}
