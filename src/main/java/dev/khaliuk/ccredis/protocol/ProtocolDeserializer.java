package dev.khaliuk.ccredis.protocol;

import dev.khaliuk.ccredis.exception.EndOfStreamException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ProtocolDeserializer {
    public Pair<String, Long> parseInput(DataInputStream inputStream) {
        try {
            char c = (char) inputStream.readByte();

            var parsedResult = switch (c) {
                case '*' -> parseArray(inputStream);
                case '$' -> parseBulkString(inputStream);
                case '+' -> parseSimpleString(inputStream);
                default -> throw new RuntimeException("Unknown character: " + c);
            };
            return Pair.of(parsedResult.getLeft().trim(), parsedResult.getRight() + 1);
        } catch (EOFException e) {
            throw new EndOfStreamException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String parseRdbFile(DataInputStream inputStream) throws IOException {
        char c = (char) inputStream.readByte();
        if (c != '$') {
            throw new RuntimeException("Unexpected start of RDB file string: " + c);
        }
        int stringLength = parseDigits(inputStream).getLeft();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            stringBuilder.append((char) inputStream.readByte());
        }
        return stringBuilder.toString();
    }

    private Pair<String, Long> parseArray(DataInputStream inputStream) throws IOException {
        Pair<Integer, Long> parsedResult = parseDigits(inputStream);
        return IntStream.range(0, parsedResult.getLeft())
                .mapToObj(i -> parseInput(inputStream))
                .reduce(Pair.of("", parsedResult.getRight()),
                        (pair1, pair2) -> Pair.of(
                                pair1.getLeft() + " " + pair2.getLeft(),
                                pair1.getRight() + pair2.getRight()));
    }

    private Pair<String, Long> parseBulkString(DataInputStream inputStream) throws IOException {
        Pair<Integer, Long> parsedResult = parseDigits(inputStream);
        long bytesCounter = parsedResult.getRight();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < parsedResult.getLeft(); i++) {
            stringBuilder.append((char) inputStream.readByte());
            bytesCounter++;
        }
        // TODO: check whether it's expected terminating symbol
        inputStream.readByte(); // skip terminating '\r'
        inputStream.readByte(); // skip terminating '\n'
        bytesCounter += 2;
        return Pair.of(stringBuilder.toString(), bytesCounter);
    }

    private Pair<String, Long> parseSimpleString(DataInputStream inputStream) throws IOException {
        long bytesCount = 0L;
        char c = (char) inputStream.readByte();
        bytesCount++;
        List<Character> characters = new ArrayList<>();
        while (c != '\r') {
            characters.add(c);
            c = (char) inputStream.readByte();
            bytesCount++;
        }
        // TODO: check whether it's expected terminating symbol
        inputStream.readByte(); // skip '\n' after '\r'
        bytesCount++;

        StringBuilder stringBuilder = new StringBuilder();
        characters.forEach(stringBuilder::append);
        return Pair.of(stringBuilder.toString(), bytesCount);
    }

    private Pair<Integer, Long> parseDigits(DataInputStream inputStream) throws IOException {
        Pair<String, Long> parsedResult = parseSimpleString(inputStream);
        return Pair.of(Integer.parseInt(parsedResult.getLeft()), parsedResult.getRight());
    }
}
