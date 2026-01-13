package org.orbitalLogistic.spacecraft.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SpacecraftStatus {
    DOCKED, IN_TRANSIT, MAINTENANCE, DECOMMISSIONED;

    @JsonCreator
    public static SpacecraftStatus from(String value) {
        if (value == null) return null;
        String s = value.trim().toUpperCase();
        for (SpacecraftStatus t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown SpacecraftStatus value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}

