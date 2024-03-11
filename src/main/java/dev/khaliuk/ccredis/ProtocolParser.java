package dev.khaliuk.ccredis;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProtocolParser {
    private ProtocolParser() {}

    public static String parseInput(DataInputStream inputStream) {
        try {
            char c = (char) inputStream.readByte();
            return switch (c) {
                // TODO: extract character literals into constants
                case '*' -> parseArray(inputStream);
                case '$' -> parseString(inputStream);
                default -> throw new RuntimeException("Unknown character: " + c);
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseArray(DataInputStream inputStream) throws IOException {
        int arraySize = parseDigits(inputStream);
        return IntStream.range(0, arraySize)
                .mapToObj(i -> parseInput(inputStream))
                .collect(Collectors.joining(" "));
    }

    private static String parseString(DataInputStream inputStream) throws IOException {
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

    private static int parseDigits(DataInputStream inputStream) throws IOException {
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
        return Integer.parseInt(stringBuilder.toString());
    }
}
