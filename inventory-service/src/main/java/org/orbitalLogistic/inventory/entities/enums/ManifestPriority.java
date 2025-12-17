package org.orbitalLogistic.inventory.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ManifestPriority {
    LOW, NORMAL, HIGH, CRITICAL;

    @JsonCreator
    public static ManifestPriority from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (ManifestPriority t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown ManifestPriority value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

