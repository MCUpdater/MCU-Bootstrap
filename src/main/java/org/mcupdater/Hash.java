package org.mcupdater;

public class Hash {
    private HashEnum type;
    private String value;

    public Hash(HashEnum type, String value) {
        this.setType(type);
        this.setValue(value);
    }

    public HashEnum getType() {
        return type;
    }

    public void setType(HashEnum type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
