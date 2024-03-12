package dev.khaliuk.ccredis.protocol;

import dev.khaliuk.ccredis.exception.EndOfStreamException;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProtocolDeserializer {
    public String parseInput(DataInputStream inputStream) {
        try {
            char c = (char) inputStream.readByte();
            return switch (c) {
                // TODO: extract character literals into constants
                case '*' -> parseArray(inputStream);
                case '$' -> parseBulkString(inputStream);
                case '+' -> parseSimpleString(inputStream);
                default -> throw new RuntimeException("Unknown character: " + c);
            };
        } catch (EOFException e) {
            throw new EndOfStreamException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseArray(DataInputStream inputStream) throws IOException {
        int arraySize = parseDigits(inputStream);
        return IntStream.range(0, arraySize)
                .mapToObj(i -> parseInput(inputStream))
                .collect(Collectors.joining(" "));
    }

    private String parseBulkString(DataInputStream inputStream) throws IOException {
        int stringLength = parseDigits(inputStream);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            stringBuilder.append((char) inputStream.readByte());
        }
        // TODO: check whether it's expected terminating symbol
        inputStream.readByte(); // skip terminating '\r'
        inputStream.readByte(); // skip terminating '\n'
        return stringBuilder.toString();
    }

    private String parseSimpleString(DataInputStream inputStream) throws IOException {
        char c = (char) inputStream.readByte();
        List<Character> characters = new ArrayList<>();
        while (c != '\r') {
            characters.add(c);
            c = (char) inputStream.readByte();
        }
        // TODO: check whether it's expected terminating symbol
        inputStream.readByte(); // skip '\n' after '\r'

        StringBuilder stringBuilder = new StringBuilder();
        characters.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    private int parseDigits(DataInputStream inputStream) throws IOException {
        return Integer.parseInt(parseSimpleString(inputStream));
    }
}
