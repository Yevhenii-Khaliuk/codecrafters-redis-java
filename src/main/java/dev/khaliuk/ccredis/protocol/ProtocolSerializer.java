package dev.khaliuk.ccredis.protocol;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ProtocolSerializer {
    private static final String CRLF_TERMINATOR = "\r\n";

    public byte[] simpleString(String value) {
        return ("+" + value + CRLF_TERMINATOR).getBytes();
    }

    public byte[] bulkString(String value) {
        if (value == null) {
            return ("$-1" + CRLF_TERMINATOR).getBytes();
        }
        return ("$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR).getBytes();
    }

    public byte[] array(List<String> values) {
        byte[] response = ("*" + values.size() + CRLF_TERMINATOR).getBytes();
        List<byte[]> bulkStrings = values.stream()
                .map(this::bulkString)
                .toList();
        for (byte[] bulkString : bulkStrings) {
            response = ArrayUtils.addAll(response, bulkString);
        }
        return response;
    }
}
