package org.orbitalLogistic.spacecraft.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SpacecraftClassification {
    CARGO_HAULER, PERSONNEL_TRANSPORT, SCIENCE_VESSEL;

    @JsonCreator
    public static SpacecraftClassification from(String value) {
        if (value == null ) return null;
        String s = value.trim().toUpperCase();
        for (SpacecraftClassification t : values()) {
            if (t.name().equals(s)) return t;
        }
        throw new IllegalArgumentException("Unknown SpacecraftClassification value: '" + value + "'");
    }

    @JsonValue
    public String toValue() {return name();}
}

