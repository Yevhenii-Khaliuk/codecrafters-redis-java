package dev.khaliuk.ccredis.protocol;

public class ProtocolSerializer {
    private static final String CRLF_TERMINATOR = "\r\n";

    private ProtocolSerializer() {
    }

    public static String bulkString(String value) {
        if (value == null) {
            return "$-1" + CRLF_TERMINATOR;
        }
        return "$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR;
    }

    public static String simpleString(String value) {
        return "+" + value + CRLF_TERMINATOR;
    }
}
