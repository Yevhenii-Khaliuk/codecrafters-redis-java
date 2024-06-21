package dev.khaliuk.ccredis.protocol;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ProtocolSerializer {
    private static final String CRLF_TERMINATOR = "\r\n";

    public byte[] simpleString(String value) {
        return ("+" + value + CRLF_TERMINATOR).getBytes();
    }

    public byte[] simpleError(String value) {
        return ("-" + value + CRLF_TERMINATOR).getBytes();
    }

    public byte[] bulkString(String value) {
        if (value == null) {
            return ("$-1" + CRLF_TERMINATOR).getBytes();
        }
        return ("$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR).getBytes();
    }

    public byte[] array(List values) {
        byte[] response = ("*" + values.size() + CRLF_TERMINATOR).getBytes();

        List<byte[]> serializedElements = new ArrayList<>();

        for (Object value : values) {
            if (value instanceof String stringValue) {
                serializedElements.add(bulkString(stringValue));
            } else if (value instanceof List listValue) {
                serializedElements.add(array(listValue));
            } else {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }
        }

        for (byte[] element : serializedElements) {
            response = ArrayUtils.addAll(response, element);
        }

        return response;
    }

    public byte[] arrayOfSerialized(List<byte[]> elements) {
        var response = ("*" + elements.size() + CRLF_TERMINATOR).getBytes();

        for (byte[] element : elements) {
            response = ArrayUtils.addAll(response, element);
        }

        return response;
    }

    public byte[] integer(Long value) {
        return (":" + value + CRLF_TERMINATOR).getBytes();
    }
}
