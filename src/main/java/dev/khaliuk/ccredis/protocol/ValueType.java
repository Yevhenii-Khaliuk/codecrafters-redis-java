package dev.khaliuk.ccredis.protocol;

public enum ValueType {
    NONE("none"),
    STRING("string");

    private final String display;

    ValueType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
