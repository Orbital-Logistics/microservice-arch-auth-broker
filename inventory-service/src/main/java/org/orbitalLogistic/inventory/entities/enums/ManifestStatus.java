package org.orbitalLogistic.inventory.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ManifestStatus {
    PENDING, LOADED, IN_TRANSIT, UNLOADED;

    @JsonCreator
    public static ManifestStatus from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (ManifestStatus t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown ManifestStatus value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

