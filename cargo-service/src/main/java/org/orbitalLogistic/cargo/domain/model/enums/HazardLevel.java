package org.orbitalLogistic.cargo.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HazardLevel {
    NONE, LOW, MEDIUM, HIGH;

    @JsonCreator
    public static HazardLevel from(String value) {
        if (value == null) return null;
        String s = value.trim().toUpperCase();
        for (HazardLevel t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown HazardLevel value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
