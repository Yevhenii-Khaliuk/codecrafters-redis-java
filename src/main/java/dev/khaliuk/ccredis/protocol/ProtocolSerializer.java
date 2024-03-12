package dev.khaliuk.ccredis.protocol;

public class ProtocolSerializer {
    private static final String CRLF_TERMINATOR = "\r\n";

    public String bulkString(String value) {
        if (value == null) {
            return "$-1" + CRLF_TERMINATOR;
        }
        return "$" + value.length() + CRLF_TERMINATOR + value + CRLF_TERMINATOR;
    }

    public String simpleString(String value) {
        return "+" + value + CRLF_TERMINATOR;
    }
}
